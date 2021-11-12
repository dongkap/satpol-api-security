package com.dongkap.security.entity;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.security.core.GrantedAuthority;

import com.dongkap.common.utils.SchemaDatabase;
import com.dongkap.dto.security.SystemAuthorityDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper=false, exclude={"roles"})
@ToString(exclude={"roles"})
@Entity
@Table(name = "sec_sys_auth", schema = SchemaDatabase.SECURITY)
public class SystemAuthorityEntity extends BaseAuditEntity implements GrantedAuthority  {


	/**
	 * 
	 */
	private static final long serialVersionUID = -5870155744883664118L;

	@Id
	@GenericGenerator(name = "uuid", strategy = "uuid2")
	@GeneratedValue(generator = "uuid")
	@Column(name = "sys_auth_uuid", nullable = false, unique = true)
	private String id;

	@Column(name = "sys_auth_code", nullable = false, unique = true)
	private String code;

	@Column(name = "sys_auth_name", nullable = false)
	private String name;

	@OneToMany(mappedBy = "sysAuth", targetEntity = RoleEntity.class, fetch = FetchType.EAGER)
	@Fetch(FetchMode.SELECT)
	private Set<RoleEntity> roles = new HashSet<RoleEntity>();

	public SystemAuthorityDto dto(){
		SystemAuthorityDto sysAuthDto = new SystemAuthorityDto();
		sysAuthDto.setId(this.id);
		sysAuthDto.setCode(this.code);
		sysAuthDto.setName(this.name);
		return sysAuthDto;
	}

	@Override
	@Transient
	public String getAuthority() {
		return this.code;
	}
}