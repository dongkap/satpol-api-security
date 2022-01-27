package com.dongkap.security.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dongkap.dto.common.CommonResponseDto;
import com.dongkap.dto.common.FilterDto;
import com.dongkap.dto.security.TrainingDto;
import com.dongkap.security.common.CommonService;
import com.dongkap.security.dao.TrainingRepo;
import com.dongkap.security.entity.TrainingEntity;

@Service("trainingService")
public class TrainingImplService extends CommonService {

	protected Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private TrainingRepo trainingRepo;

	@Transactional
	public CommonResponseDto<TrainingDto> getDatatableEmployee(FilterDto filter) throws Exception {
		String employeeId = filter.getKeyword().get("employeeId").toString();
		List<TrainingEntity> educations = trainingRepo.findByEmployee_Id(employeeId);
		final CommonResponseDto<TrainingDto> response = new CommonResponseDto<TrainingDto>();
		response.setTotalFiltered(Long.valueOf(educations.size()));
		response.setTotalRecord(Long.valueOf(educations.size()));
		educations.forEach(value -> {
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

}
