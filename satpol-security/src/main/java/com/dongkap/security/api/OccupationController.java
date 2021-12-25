package com.dongkap.security.api;

import java.util.List;

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
import com.dongkap.common.utils.ResourceCode;
import com.dongkap.common.utils.SuccessCode;
import com.dongkap.dto.common.ApiBaseResponse;
import com.dongkap.dto.common.CommonResponseDto;
import com.dongkap.dto.common.FilterDto;
import com.dongkap.dto.security.OccupationDto;
import com.dongkap.dto.select.SelectResponseDto;
import com.dongkap.security.service.OccupationImplService;

@RestController
@RequestMapping(ResourceCode.SECURITY_PATH)
public class OccupationController extends BaseControllerException {
	
	@Autowired
	private OccupationImplService occupationService;

	@RequestMapping(value = "/vw/auth/datatable/occupation/v.1", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<CommonResponseDto<OccupationDto>> getDatatableOccupation(Authentication authentication,
			@RequestBody(required = true) FilterDto filter) throws Exception {
		String username = authentication.getName();
		return new ResponseEntity<CommonResponseDto<OccupationDto>>(this.occupationService.getDatatable(username, filter), HttpStatus.OK);
	}

	@RequestMapping(value = "/vw/auth/select/occupation/v.1", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<SelectResponseDto> getSelectRole(Authentication authentication,
														 @RequestHeader(name = HttpHeaders.ACCEPT_LANGUAGE, required = false) String locale,
														 @RequestBody(required = true) FilterDto filter) throws Exception {
		String username = authentication.getName();
		return new ResponseEntity<SelectResponseDto>(this.occupationService.getSelectOccupation(username, filter), HttpStatus.OK);
	}

	@ResponseSuccess(SuccessCode.OK_DEFAULT)
	@RequestMapping(value = "/trx/auth/occupation/v.1", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiBaseResponse> postOccupation(Authentication authentication,
			@RequestBody(required = true) OccupationDto data) throws Exception {
		String username = authentication.getName();
		this.occupationService.postOccupation(data, username);
		return new ResponseEntity<ApiBaseResponse>(new ApiBaseResponse(), HttpStatus.OK);
	}

	@ResponseSuccess(SuccessCode.OK_DELETED)
	@RequestMapping(value = "/trx/auth/delete/occupation/v.1", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiBaseResponse> deleteOccupation(Authentication authentication,
													  @RequestBody(required = true) List<String> datas) throws Exception {
		this.occupationService.deleteOccupations(datas);
		return new ResponseEntity<ApiBaseResponse>(new ApiBaseResponse(), HttpStatus.OK);
	}
	
}
