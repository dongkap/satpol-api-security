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
@EqualsAndHashCode(callSuper=false, exclude={"employees", "occupations"})
@ToString(exclude={"employees", "occupations"})
@Entity
@Table(name = "sec_corporate", schema = SchemaDatabase.SECURITY)
public class CorporateEntity extends BaseAuditEntity {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1932022761237540822L;

	@Id
	@GenericGenerator(name = "uuid", strategy = "uuid2")
	@GeneratedValue(generator = "uuid")
	@Column(name = "corporate_uuid", nullable = false, unique = true)
	private String id;

	@Column(name = "corporate_code", nullable = false, unique = true)
	private String corporateCode;

	@Column(name = "corporate_name", nullable = false)
	private String corporateName;

	@Column(name = "corporate_non_expired", nullable = false)
	private boolean corporateNonExpired = true;

	@Column(name = "email")
	private String email;

	@Column(name = "address")
	private String address;

	@Column(name = "telp_number")
	private String telpNumber;

	@Column(name = "fax_number")
	private String faxNumber;

	@ManyToMany(mappedBy = "corporate", targetEntity = EmployeeEntity.class, fetch = FetchType.LAZY)
	@Fetch(FetchMode.SELECT)
	private Set<EmployeeEntity> employees = new HashSet<EmployeeEntity>();

	@ManyToMany(mappedBy = "corporate", targetEntity = OccupationEntity.class, fetch = FetchType.LAZY)
	@Fetch(FetchMode.SELECT)
	private Set<OccupationEntity> occupations = new HashSet<OccupationEntity>();

}