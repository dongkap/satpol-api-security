package com.dongkap.security.entity;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.dongkap.common.utils.SchemaDatabase;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper=false, exclude = { "parameter" })
@ToString(exclude = { "parameter" })
@Entity
@Table(name = "mst_parameter_i18n", schema = SchemaDatabase.SECURITY)
public class ParameterI18nEntity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2965292952914303956L;

	@Id
	@Column(name = "parameter_i18n_uuid", nullable = false, unique = true)
	private String id;

	@Column(name = "parameter_value", nullable = false)
	private String parameterValue;

	@Column(name = "locale_code")
	private String localeCode;

	@ManyToOne(targetEntity = ParameterEntity.class, fetch = FetchType.EAGER, cascade = { CascadeType.MERGE, CascadeType.PERSIST })
	@JoinColumn(name = "parameter_uuid", nullable = false, updatable = false)
	private ParameterEntity parameter;

}