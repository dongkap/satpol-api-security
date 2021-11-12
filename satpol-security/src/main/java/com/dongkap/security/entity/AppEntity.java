package com.dongkap.security.entity;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.GenericGenerator;

import com.dongkap.common.utils.SchemaDatabase;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper=false, exclude={"users"})
@ToString(exclude={"users"})
@Entity
@Table(name = "sec_app", schema = SchemaDatabase.SECURITY)
public class AppEntity extends BaseAuditEntity {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1932022761237540822L;

	@Id
	@GenericGenerator(name = "uuid", strategy = "uuid2")
	@GeneratedValue(generator = "uuid")
	@Column(name = "app_uuid", nullable = false, unique = true)
	private String id;

	@Column(name = "app_code", nullable = false, unique = true)
	private String appCode;

	@Column(name = "app_name", nullable = false)
	private String appName;

	@Column(name = "description", nullable = false)
	private String description;

	@ManyToMany(mappedBy = "apps", targetEntity = UserEntity.class, fetch = FetchType.LAZY)
	@Fetch(FetchMode.SELECT)
	private Set<UserEntity> users = new HashSet<UserEntity>();

}