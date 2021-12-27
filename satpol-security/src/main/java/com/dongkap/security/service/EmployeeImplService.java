package com.dongkap.security.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dongkap.common.exceptions.SystemErrorException;
import com.dongkap.common.utils.ErrorCode;
import com.dongkap.dto.security.CorporateDto;
import com.dongkap.security.common.CommonService;
import com.dongkap.security.dao.EmployeeRepo;
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

}
