package com.dongkap.security.dao.specification;

import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;

import com.dongkap.security.entity.OccupationEntity;


public class OccupationSpecification {
	
	private static final String IS_ACTIVE = "active";

	public static Specification<OccupationEntity> getSelect(final Map<String, Object> keyword) {
		return new Specification<OccupationEntity>() {

			/**
			 * 
			 */
			private static final long serialVersionUID = -637621292944403277L;

			@Override
			public Predicate toPredicate(Root<OccupationEntity> root, CriteriaQuery<?> criteria, CriteriaBuilder builder) {
				Predicate predicate = builder.conjunction();
				if (!keyword.isEmpty()) {
					for(Map.Entry<String, Object> filter : keyword.entrySet()) {
						String key = filter.getKey();
						Object value = filter.getValue();
						if (value != null) {
							switch (key) {
								case "_label" :
								case "name" :
									// builder.upper for PostgreSQL
									predicate.getExpressions().add(builder.like(builder.upper(root.<String>get("name")), String.format("%%%s%%", value.toString().toUpperCase())));
									break;
								case "code" :
									predicate.getExpressions().add(builder.equal(root.get(key), value));
									break;
								case "corporateCode" :
									predicate.getExpressions().add(builder.equal(root.join("corporate").<String>get("corporateCode"), value.toString()));
									break;
							}
						}
					}
				}
				predicate = builder.and(predicate, builder.equal(root.get(IS_ACTIVE), true));
				return predicate;
			}
		};
	}

	public static Specification<OccupationEntity> getDatatable(final Map<String, Object> keyword) {
		return new Specification<OccupationEntity>() {

			/**
			 * 
			 */
			private static final long serialVersionUID = -637621292944403277L;

			@Override
			public Predicate toPredicate(Root<OccupationEntity> root, CriteriaQuery<?> criteria, CriteriaBuilder builder) {
				Predicate predicate = builder.conjunction();
				if (!keyword.isEmpty()) {
					for(Map.Entry<String, Object> filter : keyword.entrySet()) {
						String key = filter.getKey();
						Object value = filter.getValue();
						if (value != null) {
							switch (key) {
								case "code" :
									predicate.getExpressions().add(builder.equal(root.get(key), value));
								case "name" :
									// builder.upper for PostgreSQL
									predicate.getExpressions().add(builder.like(builder.upper(root.<String>get(key)), String.format("%%%s%%", value.toString().toUpperCase())));
									break;
								case "corporateCode" :
									predicate.getExpressions().add(builder.equal(root.join("corporate").<String>get("corporateCode"), value.toString()));
									break;
								case "_all" :
									predicate.getExpressions().add(builder.like(builder.upper(root.<String>get("name")), String.format("%%%s%%", value.toString().toUpperCase())));
									break;
								default :
									break;
							}	
						}
					}
				}
				predicate = builder.and(predicate, builder.equal(root.get(IS_ACTIVE), true));
				return predicate;
			}
		};
	}

}
