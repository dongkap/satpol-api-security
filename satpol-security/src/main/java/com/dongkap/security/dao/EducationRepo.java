package com.dongkap.security.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.dongkap.security.entity.EducationEntity;

public interface EducationRepo extends JpaRepository<EducationEntity, String>, JpaSpecificationExecutor<EducationEntity> {

	List<EducationEntity> findByEmployee_Id(String employeeId);

	List<EducationEntity> findByIdIn(List<String> educationIds);

	List<EducationEntity> findByIdInAndEmployee_User_Username(List<String> educationIds, String username);

}