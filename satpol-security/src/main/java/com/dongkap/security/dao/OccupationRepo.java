package com.dongkap.security.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.dongkap.security.entity.OccupationEntity;

public interface OccupationRepo extends JpaRepository<OccupationEntity, String>, JpaSpecificationExecutor<OccupationEntity> {

	OccupationEntity findByCode(String code);

	List<OccupationEntity> findByCodeIn(List<String> codes);
	
}