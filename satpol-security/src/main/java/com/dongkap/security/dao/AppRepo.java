package com.dongkap.security.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.dongkap.security.entity.AppEntity;

public interface AppRepo extends JpaRepository<AppEntity, String>, JpaSpecificationExecutor<AppEntity> {

	AppEntity findByAppCode(String appCode);

	List<AppEntity> findByAppCodeIn(List<String> appCodes);
	
}