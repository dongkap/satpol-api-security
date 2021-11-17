package com.dongkap.security.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.dongkap.security.entity.OccupationEntity;

public interface OccupationRepo extends JpaRepository<OccupationEntity, String>, JpaSpecificationExecutor<OccupationEntity> {
	
}