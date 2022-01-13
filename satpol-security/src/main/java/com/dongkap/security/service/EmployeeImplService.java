package com.dongkap.security.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dongkap.common.exceptions.SystemErrorException;
import com.dongkap.common.utils.ErrorCode;
import com.dongkap.dto.common.CommonResponseDto;
import com.dongkap.dto.common.FilterDto;
import com.dongkap.dto.security.CorporateDto;
import com.dongkap.dto.security.EmployeeListDto;
import com.dongkap.security.common.CommonService;
import com.dongkap.security.dao.EmployeeRepo;
import com.dongkap.security.dao.specification.EmployeeSpecification;
import com.dongkap.security.entity.CorporateEntity;
import com.dongkap.security.entity.EmployeeEntity;

@Service("employeeService")
public class EmployeeImplService extends CommonService {

	protected Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private EmployeeRepo employeeRepo;

	@Transactional
	public CorporateDto getCorporate(String username) throws Exception {
		EmployeeEntity employee = employeeRepo.findByUser_Username(username);
		if(employee == null) {
			throw new SystemErrorException(ErrorCode.ERR_SYS0001);
		}
		if (employee.getCorporate() == null) {
			throw new SystemErrorException(ErrorCode.ERR_SYS0001);
		}
		CorporateEntity corporate = employee.getCorporate();
		CorporateDto response = new CorporateDto();
		response.setId(corporate.getId());
		response.setCorporateCode(corporate.getCorporateCode());
		response.setCorporateName(corporate.getCorporateName());
		response.setCorporateNonExpired(corporate.isCorporateNonExpired());
		response.setEmail(corporate.getEmail());
		response.setAddress(corporate.getAddress());
		response.setTelpNumber(corporate.getTelpNumber());
		response.setFaxNumber(corporate.getFaxNumber());
		response.setActive(corporate.getActive());
		response.setVersion(corporate.getVersion());
		response.setCreatedDate(corporate.getCreatedDate());
		response.setCreatedBy(corporate.getCreatedBy());
		response.setModifiedDate(corporate.getModifiedDate());
		response.setModifiedBy(corporate.getModifiedBy());
		return response;
	}

	@Transactional
	public CommonResponseDto<EmployeeListDto> getDatatable(Map<String, Object> additionalInfo, FilterDto filter) throws Exception {
		if(additionalInfo.get("corporate_code") == null) {
			throw new SystemErrorException(ErrorCode.ERR_SYS0001);
		}
		filter.getKeyword().put("corporateCode", additionalInfo.get("corporate_code"));
		Page<EmployeeEntity> occupation = employeeRepo.findAll(EmployeeSpecification.getDatatable(filter.getKeyword()), page(filter.getOrder(), filter.getOffset(), filter.getLimit()));
		final CommonResponseDto<EmployeeListDto> response = new CommonResponseDto<EmployeeListDto>();
		response.setTotalFiltered(Long.valueOf(occupation.getContent().size()));
		response.setTotalRecord(employeeRepo.count(EmployeeSpecification.getDatatable(filter.getKeyword())));
		occupation.getContent().forEach(value -> {
			EmployeeListDto temp = new EmployeeListDto();
			temp.setIdEmployee(value.getIdEmployee());
			temp.setActive(value.getActive());
			temp.setVersion(value.getVersion());
			temp.setCreatedDate(value.getCreatedDate());
			temp.setCreatedBy(value.getCreatedBy());
			temp.setModifiedDate(value.getModifiedDate());
			temp.setModifiedBy(value.getModifiedBy());
			if(value.getOccupation() != null) {
				temp.setOccupationName(value.getOccupation().getName());
			}
			if(value.getUser() != null) {
				temp.getUser().put("fullname", value.getUser().getFullname());
				temp.getUser().put("email", value.getUser().getEmail());
				temp.getUser().put("username", value.getUser().getUsername());
			}
			if(value.getContactUser() != null) {
				temp.setPhoneNumber(value.getContactUser().getPhoneNumber());
				temp.setAddress(value.getContactUser().getAddress());
			}
			response.getData().add(temp);
		});
		return response;
	}

}
