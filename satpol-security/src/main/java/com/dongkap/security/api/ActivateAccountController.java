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
import com.dongkap.common.utils.SuccessCode;
import com.dongkap.dto.common.ApiBaseResponse;
import com.dongkap.dto.security.ActivateAccountDto;
import com.dongkap.security.service.ActivateAccountImplService;

@RestController
public class ActivateAccountController extends BaseControllerException {

	@Autowired
	private ActivateAccountImplService activateAccountService;

	@ResponseSuccess(SuccessCode.OK_VERIFICATION_ACTIVATE_ACCOUNT)
	@RequestMapping(value = "/oauth/verification-activate", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiBaseResponse> verificationActivateAccount(Authentication authentication,
			@RequestHeader(name = HttpHeaders.ACCEPT_LANGUAGE, required = false) String locale,
			@RequestBody(required = true) ActivateAccountDto p_dto) throws Exception {
		return new ResponseEntity<ApiBaseResponse>(activateAccountService.verificationActivateAccount(p_dto, locale), HttpStatus.OK);
	}

	@ResponseSuccess(SuccessCode.OK_ACTIVATE_ACCOUNT)
	@RequestMapping(value = "/oauth/activate", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiBaseResponse> activateAccount(Authentication authentication,
			@RequestHeader(name = HttpHeaders.ACCEPT_LANGUAGE, required = false) String locale,
			@RequestBody(required = true) ActivateAccountDto p_dto) throws Exception {
		return new ResponseEntity<ApiBaseResponse>(activateAccountService.activateAccount(p_dto, locale), HttpStatus.OK);
	}
	
}
