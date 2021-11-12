package com.dongkap.security.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.dongkap.security.entity.SystemAuthorityEntity;


public interface SystemAuthorityRepo extends JpaRepository<SystemAuthorityEntity, String>, JpaSpecificationExecutor<SystemAuthorityEntity> {

	SystemAuthorityEntity findByCode(String code);

	List<SystemAuthorityEntity> findByCodeIn(List<String> codes);
	
}