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
import com.dongkap.dto.security.SettingsDto;
import com.dongkap.security.service.SettingsImplService;

@RestController
@RequestMapping(ResourceCode.SECURITY_PATH)
public class SettingsController extends BaseControllerException {

	@Autowired
	private SettingsImplService settingsService;
	
	@ResponseSuccess(SuccessCode.OK_SCR002)
	@RequestMapping(value = "/trx/put/settings/v.1", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiBaseResponse> putSettings(Authentication authentication,
													   @RequestHeader(name = HttpHeaders.ACCEPT_LANGUAGE, required = false) String locale,
													   @RequestBody(required = true) SettingsDto p_dto) throws Exception {
		UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
		return new ResponseEntity<ApiBaseResponse>(settingsService.doUpdateSettings(p_dto, userPrincipal, locale), HttpStatus.OK);
	}
	
	@RequestMapping(value = "/vw/get/settings/v.1", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<SettingsDto> getSettings(Authentication authentication,
			@RequestHeader(name = HttpHeaders.ACCEPT_LANGUAGE, required = false) String locale) throws Exception {
		UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
		return new ResponseEntity<SettingsDto>(settingsService.getSettings(userPrincipal, locale), HttpStatus.OK);
	}
	
	
}
