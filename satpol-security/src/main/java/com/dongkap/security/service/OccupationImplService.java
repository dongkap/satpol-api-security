package com.dongkap.security.service;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dongkap.common.exceptions.SystemErrorException;
import com.dongkap.common.stream.PublishStream;
import com.dongkap.common.utils.ErrorCode;
import com.dongkap.common.utils.ParameterStatic;
import com.dongkap.common.utils.StreamKeyStatic;
import com.dongkap.dto.common.CommonResponseDto;
import com.dongkap.dto.common.FilterDto;
import com.dongkap.dto.security.OccupationDto;
import com.dongkap.dto.select.SelectDto;
import com.dongkap.dto.select.SelectResponseDto;
import com.dongkap.security.common.CommonService;
import com.dongkap.security.dao.EmployeeRepo;
import com.dongkap.security.dao.OccupationRepo;
import com.dongkap.security.dao.specification.OccupationSpecification;
import com.dongkap.security.entity.EmployeeEntity;
import com.dongkap.security.entity.OccupationEntity;

@Service("occupationService")
public class OccupationImplService extends CommonService {

	protected Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private OccupationRepo occupationRepo;

	@Autowired
	private EmployeeRepo employeeRepo;

	@Transactional
	public SelectResponseDto getSelectOccupation(String username, FilterDto filter) throws Exception {
		EmployeeEntity employee = employeeRepo.findByUser_Username(username);
		filter.getKeyword().put("corporateCode", employee.getCorporate().getCorporateCode());
		Page<OccupationEntity> occupation = occupationRepo.findAll(OccupationSpecification.getSelect(filter.getKeyword()), page(filter.getOrder(), filter.getOffset(), filter.getLimit()));
		final SelectResponseDto response = new SelectResponseDto();
		response.setTotalFiltered(Long.valueOf(occupation.getContent().size()));
		response.setTotalRecord(occupationRepo.count(OccupationSpecification.getSelect(filter.getKeyword())));
		occupation.getContent().forEach(value -> {
			response.getData().add(new SelectDto(value.getName(), value.getCode(), !value.isActive(), null));
		});
		return response;
	}

	@Transactional
	public CommonResponseDto<OccupationDto> getDatatable(String username, FilterDto filter) throws Exception {
		EmployeeEntity employee = employeeRepo.findByUser_Username(username);
		filter.getKeyword().put("corporateCode", employee.getCorporate().getCorporateCode());
		Page<OccupationEntity> occupation = occupationRepo.findAll(OccupationSpecification.getDatatable(filter.getKeyword()), page(filter.getOrder(), filter.getOffset(), filter.getLimit()));
		final CommonResponseDto<OccupationDto> response = new CommonResponseDto<OccupationDto>();
		response.setTotalFiltered(Long.valueOf(occupation.getContent().size()));
		response.setTotalRecord(occupationRepo.count(OccupationSpecification.getDatatable(filter.getKeyword())));
		occupation.getContent().forEach(value -> {
			OccupationDto temp = new OccupationDto();
			temp.setCode(value.getCode());
			temp.setName(value.getName());
			temp.setCorporateCode(value.getCorporate().getCorporateCode());
			temp.setCorporateName(value.getCorporate().getCorporateName());
			temp.setActive(value.isActive());
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
	@PublishStream(key = StreamKeyStatic.OCCUPATION, status = ParameterStatic.UPDATE_DATA)
	public List<OccupationDto> postOccupation(OccupationDto request, String username) throws Exception {
		EmployeeEntity employee = employeeRepo.findByUser_Username(username);
		OccupationEntity occupation = this.occupationRepo.findByCode(request.getCode());
		List<OccupationDto> result = null;
		if (occupation == null) {
			occupation = new OccupationEntity();
			occupation.setCorporate(employee.getCorporate());
		} else {
			request.setId(occupation.getId());
			result = new ArrayList<OccupationDto>();
			result.add(request);
		}
		occupation.setCode(request.getCode());
		occupation.setName(request.getName());
		occupationRepo.saveAndFlush(occupation);
		return result;
	}

	public void deleteOccupations(List<String> occupationCodes) throws Exception {
		List<OccupationEntity> occupations = occupationRepo.findByCodeIn(occupationCodes);
		try {
			occupationRepo.deleteInBatch(occupations);			
		} catch (DataIntegrityViolationException e) {
			throw new SystemErrorException(ErrorCode.ERR_SCR0009);
		} catch (ConstraintViolationException e) {
			throw new SystemErrorException(ErrorCode.ERR_SCR0009);
		}
	}

}
