package com.dongkap.security.service;

import java.util.List;
import java.util.Map;

import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dongkap.common.exceptions.SystemErrorException;
import com.dongkap.common.utils.ErrorCode;
import com.dongkap.dto.common.CommonResponseDto;
import com.dongkap.dto.common.FilterDto;
import com.dongkap.dto.security.EmployeeRequestAddDto;
import com.dongkap.dto.security.TrainingDto;
import com.dongkap.security.common.CommonService;
import com.dongkap.security.dao.EmployeeRepo;
import com.dongkap.security.dao.TrainingRepo;
import com.dongkap.security.dao.specification.TrainingSpecification;
import com.dongkap.security.entity.EmployeeEntity;
import com.dongkap.security.entity.TrainingEntity;

@Service("trainingService")
public class TrainingImplService extends CommonService {

	protected Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private TrainingRepo trainingRepo;

	@Autowired
	private EmployeeRepo employeeRepo;

	@Transactional
	public CommonResponseDto<TrainingDto> getDatatableTrainingEmployee(FilterDto filter) throws Exception {
		Page<TrainingEntity> trainings = trainingRepo.findAll(TrainingSpecification.getDatatable(filter.getKeyword()), page(filter.getOrder(), filter.getOffset(), filter.getLimit()));
		final CommonResponseDto<TrainingDto> response = new CommonResponseDto<TrainingDto>();
		response.setTotalFiltered(Long.valueOf(trainings.getContent().size()));
		response.setTotalRecord(trainingRepo.count(TrainingSpecification.getDatatable(filter.getKeyword())));
		trainings.forEach(value -> {
			TrainingDto temp = new TrainingDto();
			temp.setId(value.getId());
			temp.setCode(value.getCode());
			temp.setName(value.getName());
			temp.setStartDate(value.getStartDate());
			temp.setEndDate(value.getEndDate());
			temp.setActive(value.getActive());
			temp.setVersion(value.getVersion());
			temp.setCreatedDate(value.getCreatedDate());
			temp.setCreatedBy(value.getCreatedBy());
			temp.setModifiedDate(value.getModifiedDate());
			temp.setModifiedBy(value.getModifiedBy());
			response.getData().add(temp);
		});
		return response;
	}

	@Transactional
	public CommonResponseDto<TrainingDto> getDatatableTrainingEmployee(String username, FilterDto filter) throws Exception {
		filter.getKeyword().put("username", username);
		Page<TrainingEntity> trainings = trainingRepo.findAll(TrainingSpecification.getDatatableTrainingEmployeeProfile(filter.getKeyword()), page(filter.getOrder(), filter.getOffset(), filter.getLimit()));
		final CommonResponseDto<TrainingDto> response = new CommonResponseDto<TrainingDto>();
		response.setTotalFiltered(Long.valueOf(trainings.getContent().size()));
		response.setTotalRecord(trainingRepo.count(TrainingSpecification.getDatatable(filter.getKeyword())));
		trainings.forEach(value -> {
			TrainingDto temp = new TrainingDto();
			temp.setId(value.getId());
			temp.setCode(value.getCode());
			temp.setName(value.getName());
			temp.setStartDate(value.getStartDate());
			temp.setEndDate(value.getEndDate());
			temp.setActive(value.getActive());
			temp.setVersion(value.getVersion());
			temp.setCreatedDate(value.getCreatedDate());
			temp.setCreatedBy(value.getCreatedBy());
			temp.setModifiedDate(value.getModifiedDate());
			temp.setModifiedBy(value.getModifiedBy());
			response.getData().add(temp);
		});
		return response;
	}

	@Transactional
	public void postTrainingEmployee(Map<String, Object> additionalInfo, EmployeeRequestAddDto request) throws Exception {
		if(additionalInfo.get("corporate_code") == null) {
			throw new SystemErrorException(ErrorCode.ERR_SYS0001);
		}

		EmployeeEntity employee = this.employeeRepo.findByIdAndCorporate_CorporateCode(request.getId(), additionalInfo.get("corporate_code").toString());
		if(employee == null) {
			throw new SystemErrorException(ErrorCode.ERR_SYS0001);
		}

		TrainingEntity training = new TrainingEntity();
		if(request.getTraining().getId() != null) {
			training = this.trainingRepo.findById(request.getTraining().getId()).orElse(new TrainingEntity());	
		}
		training.setId(request.getTraining().getId());
		training.setCode(request.getTraining().getName().toUpperCase().replaceAll("[^a-zA-Z0-9]+",""));
		training.setName(request.getTraining().getName());
		training.setStartDate(request.getTraining().getStartDate());
		training.setEndDate(request.getTraining().getEndDate());
		training.setEmployee(employee);
		this.trainingRepo.saveAndFlush(training);
	}

	@Transactional
	public void postTrainingEmployee(String username, EmployeeRequestAddDto request) throws Exception {
		EmployeeEntity employee = this.employeeRepo.findByUser_Username(username);
		if(employee == null) {
			throw new SystemErrorException(ErrorCode.ERR_SYS0001);
		}

		TrainingEntity training = new TrainingEntity();
		if(request.getTraining().getId() != null) {
			training = this.trainingRepo.findById(request.getTraining().getId()).orElse(new TrainingEntity());	
		}
		training.setId(request.getTraining().getId());
		training.setCode(request.getTraining().getName().toUpperCase().replaceAll("[^a-zA-Z0-9]+",""));
		training.setName(request.getTraining().getName());
		training.setStartDate(request.getTraining().getStartDate());
		training.setEndDate(request.getTraining().getEndDate());
		training.setEmployee(employee);
		this.trainingRepo.saveAndFlush(training);
	}

	public void deleteTrainings(List<String> trainingIds) throws Exception {
		List<TrainingEntity> trainings = this.trainingRepo.findByIdIn(trainingIds);
		try {
			this.trainingRepo.deleteInBatch(trainings);
		} catch (DataIntegrityViolationException e) {
			throw new SystemErrorException(ErrorCode.ERR_SCR0009);
		} catch (ConstraintViolationException e) {
			throw new SystemErrorException(ErrorCode.ERR_SCR0009);
		}
	}

	public void deleteTrainings(List<String> trainingIds, String username) throws Exception {
		List<TrainingEntity> trainings = this.trainingRepo.findByIdInAndEmployee_User_Username(trainingIds, username);
		try {
			this.trainingRepo.deleteInBatch(trainings);
		} catch (DataIntegrityViolationException e) {
			throw new SystemErrorException(ErrorCode.ERR_SCR0009);
		} catch (ConstraintViolationException e) {
			throw new SystemErrorException(ErrorCode.ERR_SCR0009);
		}
	}

}
