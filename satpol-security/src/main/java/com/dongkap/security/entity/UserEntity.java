package com.dongkap.security.entity;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.GenericGenerator;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.dongkap.common.security.UserPrincipal;
import com.dongkap.common.utils.SchemaDatabase;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper=false, exclude={"apps", "roles", "contactUser", "settings"})
@ToString(exclude={"apps", "roles", "contactUser", "settings"})
@Entity
@Table(name = "sec_user", schema = SchemaDatabase.SECURITY)
public class UserEntity extends BaseAuditEntity implements UserDetails, OAuth2User {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2442773369159964802L;
	
	@Id
	@GenericGenerator(name = "uuid", strategy = "uuid2")
	@GeneratedValue(generator = "uuid")
    @Column(name = "user_uuid", nullable = false, unique=true)
	private String id;

	@Column(name = "username", nullable = false, unique = true, length = 25)
	private String username;

	@Column(name = "password", columnDefinition = "default 'N/A'")
	private String password;

	@Column(name = "account_enabled", nullable = false)
	private boolean enabled = true;

	@Column(name = "account_non_expired", nullable = false)
	private boolean accountNonExpired = true;

	@Column(name = "account_non_locked", nullable = false)
	private boolean accountNonLocked = true;

	@Column(name = "credentials_non_expired", nullable = false)
	private boolean credentialsNonExpired = true;

	@Column(name = "email", nullable = true, unique = true)
	private String email;

	@Column(name = "fullname", nullable = false)
	private String fullname;

	@Column(name = "provider", nullable = false)
	private String provider = "ldap";

	@Column(name = "verification_code", nullable = true)
	private String verificationCode;

	@Column(name = "verification_expired", nullable = true)
	private Date verificationExpired;

	@Column(name = "raw", nullable = true)
	private String raw;

	@Column(name = "image", nullable = true)
	private String image;

	@Column(name = "authority_default")
	private String authorityDefault;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "sec_r_user_app",
		schema = SchemaDatabase.SECURITY,
		joinColumns = { 
				@JoinColumn(name = "user_uuid", referencedColumnName = "user_uuid")}, 
		inverseJoinColumns =
				@JoinColumn(name = "app_uuid", referencedColumnName = "app_uuid"))
	private Set<AppEntity> apps = new HashSet<AppEntity>();

	@Transient
	private Map<String, Object> attributes = new HashMap<String, Object>();

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "sec_r_user_role",
		schema = SchemaDatabase.SECURITY,
		joinColumns = { 
				@JoinColumn(name = "user_uuid", referencedColumnName = "user_uuid")}, 
		inverseJoinColumns =
				@JoinColumn(name = "role_uuid", referencedColumnName = "role_uuid"))
	private Set<RoleEntity> roles = new HashSet<RoleEntity>();

	@Override
	@Transient
	public Set<GrantedAuthority> getAuthorities() {
		Set<GrantedAuthority> authorities = new LinkedHashSet<GrantedAuthority>();
		Set<GrantedAuthority> groups = roles.stream().filter(distinctByKey(g -> g.getSysAuth().getCode())).map(RoleEntity::getSysAuth).collect(Collectors.toSet());
		groups.forEach(auth->authorities.add(new SimpleGrantedAuthority(auth.getAuthority())));
		return authorities;
	}

	@Transient
	public static <T> Predicate<T> distinctByKey(
		    Function<? super T, ?> keyExtractor) {
		    Map<Object, Boolean> seen = new ConcurrentHashMap<>(); 
		    return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null; 
	}

	@Override
	@Transient
	public String getName() {
		return this.fullname;
	}

    @Override
	@Transient
    public Map<String, Object> getAttributes() {
        return this.attributes;
    }
	
	@OneToOne(mappedBy = "user", targetEntity = ContactUserEntity.class, fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
	private ContactUserEntity contactUser;
	
	@OneToOne(mappedBy = "user", targetEntity = SettingsEntity.class, fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
	private SettingsEntity settings;

	@Transient
	public UserPrincipal getUserPrincipal() {
		this.attributes.put("image", this.image);
		this.attributes.put("locale", this.getSettings().getLocaleCode());
		this.attributes.put("theme", this.getSettings().getTheme());
		UserPrincipal userPrincipal = new UserPrincipal(this.id, this.username, this.password, this.enabled,
				this.accountNonExpired, this.credentialsNonExpired,
				this.accountNonLocked, getName(), this.email, this.provider,
		this.verificationCode,
				this.verificationExpired,
				this.raw,
				this.authorityDefault, getAuthorities(), this.getAttributes());
		return userPrincipal;
	}

}