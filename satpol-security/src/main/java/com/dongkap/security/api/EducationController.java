package com.dongkap.security.api;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.dongkap.common.aspect.ResponseSuccess;
import com.dongkap.common.exceptions.BaseControllerException;
import com.dongkap.common.utils.ResourceCode;
import com.dongkap.common.utils.SuccessCode;
import com.dongkap.dto.common.ApiBaseResponse;
import com.dongkap.dto.common.CommonResponseDto;
import com.dongkap.dto.common.FilterDto;
import com.dongkap.dto.security.EducationDto;
import com.dongkap.dto.security.EmployeeRequestAddDto;
import com.dongkap.security.service.EducationImplService;

@RestController
public class EducationController extends BaseControllerException {

	@Autowired
	private EducationImplService educationService;

	@Autowired
	private TokenStore tokenStore;

	@RequestMapping(value = ResourceCode.SECURITY_PATH + "/vw/auth/datatable/education-employee/v.1", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<CommonResponseDto<EducationDto>> getDatatableEducationEmployee(Authentication authentication,
			@RequestBody(required = true) FilterDto filter,
			@RequestHeader(name = HttpHeaders.ACCEPT_LANGUAGE, required = false) String locale) throws Exception {
		return new ResponseEntity<CommonResponseDto<EducationDto>>(this.educationService.getDatatableEducationEmployee(filter, locale), HttpStatus.OK);
	}

	@ResponseSuccess(SuccessCode.OK_DEFAULT)
	@RequestMapping(value = ResourceCode.SECURITY_PATH + "/trx/auth/post/education/employee/v.1", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiBaseResponse> postEducationEmployee(Authentication authentication,
			@RequestBody(required = true) EmployeeRequestAddDto data) throws Exception {
		Map<String, Object> additionalInfo = this.getAdditionalInformation(authentication);
		this.educationService.postEducationEmployee(additionalInfo, data);
		return new ResponseEntity<ApiBaseResponse>(new ApiBaseResponse(), HttpStatus.OK);
	}

	@ResponseSuccess(SuccessCode.OK_DELETED)
	@RequestMapping(value = ResourceCode.SECURITY_PATH + "/trx/auth/delete/education/employee/v.1", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiBaseResponse> deleteEducations(Authentication authentication,
													  @RequestBody(required = true) List<String> datas) throws Exception {
		this.educationService.deleteEducations(datas);
		return new ResponseEntity<ApiBaseResponse>(new ApiBaseResponse(), HttpStatus.OK);
	}

	@RequestMapping(value = ResourceCode.PROFILE_PATH + "/vw/post/datatable/education-employee/v.1", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<CommonResponseDto<EducationDto>> getDatatableEducationEmployeeProfile(Authentication authentication,
			@RequestBody(required = true) FilterDto filter,
			@RequestHeader(name = HttpHeaders.ACCEPT_LANGUAGE, required = false) String locale) throws Exception {
		String username = authentication.getName();
		return new ResponseEntity<CommonResponseDto<EducationDto>>(this.educationService.getDatatableEducationEmployee(username, filter, locale), HttpStatus.OK);
	}

	@ResponseSuccess(SuccessCode.OK_DEFAULT)
	@RequestMapping(value = ResourceCode.PROFILE_PATH + "/trx/post/education/employee/v.1", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiBaseResponse> postEducationEmployeeProfile(Authentication authentication,
			@RequestBody(required = true) EmployeeRequestAddDto data) throws Exception {
		String username = authentication.getName();
		this.educationService.postEducationEmployee(username, data);
		return new ResponseEntity<ApiBaseResponse>(new ApiBaseResponse(), HttpStatus.OK);
	}

	@ResponseSuccess(SuccessCode.OK_DELETED)
	@RequestMapping(value = ResourceCode.PROFILE_PATH + "/trx/delete/education/employee/v.1", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiBaseResponse> deleteEducationsProfile(Authentication authentication,
													  @RequestBody(required = true) List<String> datas) throws Exception {
		String username = authentication.getName();
		this.educationService.deleteEducations(datas, username);
		return new ResponseEntity<ApiBaseResponse>(new ApiBaseResponse(), HttpStatus.OK);
	}

	public Map<String, Object> getAdditionalInformation(Authentication auth) {
	    OAuth2AuthenticationDetails auth2AuthenticationDetails = (OAuth2AuthenticationDetails) auth.getDetails();
	    return tokenStore.readAccessToken(auth2AuthenticationDetails.getTokenValue()).getAdditionalInformation();
	}

}
