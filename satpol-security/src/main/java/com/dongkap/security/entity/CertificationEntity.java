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
@Table(name = "sec_certification", schema = SchemaDatabase.SECURITY)
public class CertificationEntity extends BaseAuditEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2442773369159964802L;
	
	@Id
	@GenericGenerator(name = "uuid", strategy = "uuid2")
	@GeneratedValue(generator = "uuid")
    @Column(name = "certification_uuid", nullable = false, unique=true)
	private String id;

	@Column(name = "certification_id", nullable = false)
	private String certificationId;
	
	@Column(name = "certification_name")
	private String name;
	
	@Column(name = "certification_issuer")
	private String issuer;
	
	@Column(name = "certification_issue_date")
	private Date issueYear;
	
	@Column(name = "certification_expired_date")
	private Date expiredYear;

	@OneToOne(targetEntity = EmployeeEntity.class, fetch = FetchType.LAZY)
	@JoinColumn(name = "employee_uuid", nullable = false, updatable = false)
	private EmployeeEntity employee;

	@OneToOne(targetEntity = FileMetadataEntity.class, fetch = FetchType.LAZY)
	@JoinColumn(name = "file_metadata_uuid", nullable = true, updatable = false)
	private FileMetadataEntity fileMetadata;

}