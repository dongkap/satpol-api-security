package com.dongkap.security.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.dongkap.security.entity.TrainingEntity;

public interface TrainingRepo extends JpaRepository<TrainingEntity, String>, JpaSpecificationExecutor<TrainingEntity> {
	
	List<TrainingEntity> findByEmployee_Id(String employeeId);

	List<TrainingEntity> findByIdIn(List<String> trainingIds);

	List<TrainingEntity> findByIdInAndEmployee_User_Username(List<String> trainingIds, String username);
	
}