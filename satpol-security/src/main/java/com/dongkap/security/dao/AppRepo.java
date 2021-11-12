package com.dongkap.security.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.dongkap.security.entity.AppEntity;

public interface AppRepo extends JpaRepository<AppEntity, String>, JpaSpecificationExecutor<AppEntity> {

	AppEntity findByAppCode(String appCode);
	
}