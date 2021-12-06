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

import com.dongkap.common.aspect.ResponseSuccess;
import com.dongkap.common.exceptions.BaseControllerException;
import com.dongkap.common.security.UserPrincipal;
import com.dongkap.common.utils.ResourceCode;
import com.dongkap.common.utils.SuccessCode;
import com.dongkap.dto.common.ApiBaseResponse;
import com.dongkap.dto.security.FunctionRequestDto;
import com.dongkap.dto.security.FunctionRoleRequestDto;
import com.dongkap.security.service.FunctionImplService;

@RestController
@RequestMapping(ResourceCode.SECURITY_PATH)
public class FunctionController extends BaseControllerException {


	@Autowired
	private FunctionImplService functionService;
	
	@ResponseSuccess(SuccessCode.OK_DEFAULT)
	@RequestMapping(value = "/trx/auth/function-role/v.1", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiBaseResponse> postFunctionRole(Authentication authentication,
													   @RequestHeader(name = HttpHeaders.ACCEPT_LANGUAGE, required = false) String locale,
													   @RequestBody(required = true) FunctionRoleRequestDto p_dto) throws Exception {
		UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
		return new ResponseEntity<ApiBaseResponse>(functionService.doPostFunctionRole(p_dto, userPrincipal, locale), HttpStatus.OK);
	}
	
	@ResponseSuccess(SuccessCode.OK_DEFAULT)
	@RequestMapping(value = "/trx/auth/function/v.1", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiBaseResponse> putFunction(Authentication authentication,
													   @RequestHeader(name = HttpHeaders.ACCEPT_LANGUAGE, required = false) String locale,
													   @RequestBody(required = true) FunctionRequestDto p_dto) throws Exception {
		UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
		return new ResponseEntity<ApiBaseResponse>(functionService.doPostFunction(p_dto, userPrincipal, locale), HttpStatus.OK);
	}
	
}
