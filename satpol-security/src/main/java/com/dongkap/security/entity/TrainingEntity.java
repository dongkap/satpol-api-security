package com.dongkap.security.entity;

import java.util.Date;

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
@Table(name = "sec_training", schema = SchemaDatabase.SECURITY)
public class TrainingEntity extends BaseAuditEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2442773369159964802L;
	
	@Id
	@GenericGenerator(name = "uuid", strategy = "uuid2")
	@GeneratedValue(generator = "uuid")
    @Column(name = "training_uuid", nullable = false, unique=true)
	private String id;

	@Column(name = "training_code", nullable = false)
	private String code;
	
	@Column(name = "training_name")
	private String name;
	
	@Column(name = "training_start_date")
	private Date startDate;
	
	@Column(name = "training_end_date")
	private Date endDate;

	@OneToOne(targetEntity = EmployeeEntity.class, fetch = FetchType.LAZY)
	@JoinColumn(name = "employee_uuid", nullable = false, updatable = false)
	private EmployeeEntity employee;

}