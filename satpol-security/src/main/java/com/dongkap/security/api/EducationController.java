package com.dongkap.security.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.dongkap.common.exceptions.BaseControllerException;
import com.dongkap.common.utils.ResourceCode;
import com.dongkap.dto.common.CommonResponseDto;
import com.dongkap.dto.common.FilterDto;
import com.dongkap.dto.security.EducationDto;
import com.dongkap.security.service.EducationImplService;

@RestController
@RequestMapping(ResourceCode.SECURITY_PATH)
public class EducationController extends BaseControllerException {

	@Autowired
	private EducationImplService educationService;

	@RequestMapping(value = "/vw/auth/datatable/education-employee/v.1", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<CommonResponseDto<EducationDto>> getDatatableEmployee(Authentication authentication,
			@RequestBody(required = true) FilterDto filter,
			@RequestHeader(name = HttpHeaders.ACCEPT_LANGUAGE, required = false) String locale) throws Exception {
		return new ResponseEntity<CommonResponseDto<EducationDto>>(this.educationService.getDatatableEmployee(filter, locale), HttpStatus.OK);
	}

}
