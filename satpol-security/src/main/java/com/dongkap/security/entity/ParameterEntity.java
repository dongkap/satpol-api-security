package com.dongkap.security.entity;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.dongkap.common.utils.SchemaDatabase;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper=false, exclude = { "parameterI18n" })
@ToString(exclude = { "parameterI18n" })
@Entity
@Table(name = "mst_parameter", schema = SchemaDatabase.SECURITY)
public class ParameterEntity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2965292952914303956L;

	@Id
	@Column(name = "parameter_uuid", nullable = false, unique = true)
	private String id;

	@Column(name = "parameter_code", unique = true)
	private String parameterCode;
	
	@OneToMany(mappedBy = "parameter", targetEntity = ParameterI18nEntity.class, fetch = FetchType.LAZY, cascade = { CascadeType.MERGE, CascadeType.PERSIST })
	@Fetch(FetchMode.SELECT)
	private Set<ParameterI18nEntity> parameterI18n = new HashSet<ParameterI18nEntity>();

}