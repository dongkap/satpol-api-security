package com.dongkap.security.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.dongkap.security.entity.EmployeeEntity;

public interface EmployeeRepo extends JpaRepository<EmployeeEntity, String>, JpaSpecificationExecutor<EmployeeEntity> {

	EmployeeEntity findByUser_Username(String username);

	EmployeeEntity findByIdAndCorporate_CorporateCode(String employeeId, String corporateCode);
	
}