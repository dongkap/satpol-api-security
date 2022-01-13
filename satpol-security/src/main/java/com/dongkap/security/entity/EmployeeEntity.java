package com.dongkap.security.entity;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
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
@EqualsAndHashCode(callSuper=false, exclude={"user", "personalInfo", "contactUser", "corporate", "occupation", "parentEmployee", "childEmployees"})
@ToString(exclude={"user", "personalInfo", "contactUser", "corporate", "occupation", "parentEmployee", "childEmployees"})
@Entity
@Table(name = "sec_employee", schema = SchemaDatabase.SECURITY)
public class EmployeeEntity extends BaseAuditEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2442773369159964802L;
	
	@Id
	@GenericGenerator(name = "uuid", strategy = "uuid2")
	@GeneratedValue(generator = "uuid")
    @Column(name = "employee_uuid", nullable = false, unique=true)
	private String id;

	@Column(name = "id_employee", nullable = false)
	private String idEmployee;
	
	@Column(name = "last_education_degree")
	private String lastEducationDegree;

	@OneToOne(targetEntity = UserEntity.class, fetch = FetchType.LAZY)
	@Fetch(FetchMode.JOIN)
	@JoinColumn(name = "user_uuid", nullable = false, updatable = false)
	private UserEntity user;
	
	@OneToOne(targetEntity = PersonalInfoEntity.class, fetch = FetchType.LAZY)
	@Fetch(FetchMode.JOIN)
	@JoinColumn(name = "user_uuid", nullable = false, referencedColumnName = "user_uuid", insertable = false, updatable = false)
	private PersonalInfoEntity personalInfo;
	
	@OneToOne(targetEntity = ContactUserEntity.class, fetch = FetchType.LAZY)
	@Fetch(FetchMode.JOIN)
	@JoinColumn(name = "user_uuid", nullable = false, referencedColumnName = "user_uuid", insertable = false, updatable = false)
	private ContactUserEntity contactUser;

	@OneToOne(targetEntity = CorporateEntity.class, fetch = FetchType.LAZY)
	@JoinColumn(name = "corporate_uuid", nullable = false)
	private CorporateEntity corporate;

	@OneToOne(targetEntity = OccupationEntity.class, fetch = FetchType.LAZY)
	@JoinColumn(name = "occupation_uuid", nullable = false)
	private OccupationEntity occupation;

	@ManyToOne(targetEntity = EmployeeEntity.class, fetch = FetchType.LAZY)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "parent_uuid", nullable = true, insertable = false, updatable = false)
	private EmployeeEntity parentEmployee;

	@OneToMany(mappedBy = "parentEmployee", fetch = FetchType.EAGER)
	@Fetch(FetchMode.JOIN)
	private Set<EmployeeEntity> childEmployees = new HashSet<EmployeeEntity>();

}