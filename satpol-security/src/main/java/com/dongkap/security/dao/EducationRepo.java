package com.dongkap.security.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.dongkap.security.entity.EducationEntity;

public interface EducationRepo extends JpaRepository<EducationEntity, String>, JpaSpecificationExecutor<EducationEntity> {
	
}