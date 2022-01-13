package com.dongkap.security.api;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.dongkap.common.exceptions.BaseControllerException;
import com.dongkap.common.utils.ResourceCode;
import com.dongkap.dto.common.CommonResponseDto;
import com.dongkap.dto.common.FilterDto;
import com.dongkap.dto.security.EmployeeListDto;
import com.dongkap.security.service.EmployeeImplService;

@RestController
@RequestMapping(ResourceCode.SECURITY_PATH)
public class EmployeeController extends BaseControllerException {
	
	@Autowired
	private EmployeeImplService employeeService;

	@Autowired
	private TokenStore tokenStore;

	@RequestMapping(value = "/vw/auth/datatable/employee/v.1", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<CommonResponseDto<EmployeeListDto>> getDatatableEmployee(Authentication authentication,
			@RequestBody(required = true) FilterDto filter) throws Exception {
		Map<String, Object> additionalInfo = this.getAdditionalInformation(authentication);
		return new ResponseEntity<CommonResponseDto<EmployeeListDto>>(this.employeeService.getDatatable(additionalInfo, filter), HttpStatus.OK);
	}

	public Map<String, Object> getAdditionalInformation(Authentication auth) {
	    OAuth2AuthenticationDetails auth2AuthenticationDetails = (OAuth2AuthenticationDetails) auth.getDetails();
	    return tokenStore.readAccessToken(auth2AuthenticationDetails.getTokenValue()).getAdditionalInformation();
	}
}
