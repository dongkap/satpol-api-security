package com.dongkap.security.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.dongkap.security.entity.PersonalInfoEntity;

public interface PersonalInfoRepo extends JpaRepository<PersonalInfoEntity, String>, JpaSpecificationExecutor<PersonalInfoEntity> {
	
	PersonalInfoEntity findByUser_Username(String username);
	
}