package com.dongkap.security.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

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
@EqualsAndHashCode(callSuper=false, exclude={"employee"})
@ToString(exclude={"employee"})
@Entity
@Table(name = "sec_education", schema = SchemaDatabase.SECURITY)
public class EducationEntity extends BaseAuditEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2442773369159964802L;
	
	@Id
	@GenericGenerator(name = "uuid", strategy = "uuid2")
	@GeneratedValue(generator = "uuid")
    @Column(name = "education_uuid", nullable = false, unique=true)
	private String id;
	
	@Column(name = "educational_level", nullable = false)
	private String educationalLevel;

	@Column(name = "school_name")
	private String schoolName;
	
	@Column(name = "degree")
	private String degree;
	
	@Column(name = "study")
	private String study;
	
	@Column(name = "grade")
	private String grade;
	
	@Column(name = "education_start_year")
	private int startYear;
	
	@Column(name = "education_end_year")
	private int endYear;

	@OneToOne(targetEntity = EmployeeEntity.class, fetch = FetchType.LAZY)
	@JoinColumn(name = "employee_uuid", nullable = false, updatable = false)
	private EmployeeEntity employee;

}