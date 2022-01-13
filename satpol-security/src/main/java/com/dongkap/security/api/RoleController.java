package com.dongkap.security.api;

import java.util.List;
import java.util.Map;

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
import com.dongkap.dto.security.MenuDto;
import com.dongkap.dto.security.RoleDto;
import com.dongkap.dto.select.SelectResponseDto;
import com.dongkap.security.service.RoleImplService;
import com.dongkap.security.service.UserImplService;

@RestController
@RequestMapping(ResourceCode.SECURITY_PATH)
public class RoleController extends BaseControllerException {

	@Autowired
	private RoleImplService roleService;

	@Autowired
	private UserImplService userService;

	@RequestMapping(value = "/vw/auth/datatable/role/v.1", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<CommonResponseDto<RoleDto>> getDatatableRole(Authentication authentication,
			@RequestBody(required = true) FilterDto filter) throws Exception {
		return new ResponseEntity<CommonResponseDto<RoleDto>>(this.roleService.getDatatable(filter), HttpStatus.OK);
	}

	@RequestMapping(value = "/vw/auth/select/all-role/v.1", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<SelectResponseDto> getSelectAllRole(Authentication authentication,
														 @RequestHeader(name = HttpHeaders.ACCEPT_LANGUAGE, required = false) String locale,
														 @RequestBody(required = true) FilterDto filter) throws Exception {
		return new ResponseEntity<SelectResponseDto>(this.roleService.getSelectAllRole(filter), HttpStatus.OK);
	}

	@RequestMapping(value = "/vw/auth/select/role/v.2", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<SelectResponseDto> getSelectRole(Authentication authentication,
														 @RequestHeader(name = HttpHeaders.ACCEPT_LANGUAGE, required = false) String locale,
														 @RequestBody(required = true) FilterDto filter) throws Exception {
		return new ResponseEntity<SelectResponseDto>(this.roleService.getSelectRole(filter), HttpStatus.OK);
	}

	@ResponseSuccess(SuccessCode.OK_DEFAULT)
	@RequestMapping(value = "/trx/auth/role/v.1", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiBaseResponse> postRole(Authentication authentication,
			@RequestBody(required = true) RoleDto data) throws Exception {
		String username = authentication.getName();
		this.roleService.postRole(data, username);
		return new ResponseEntity<ApiBaseResponse>(new ApiBaseResponse(), HttpStatus.OK);
	}

	@ResponseSuccess(SuccessCode.OK_DELETED)
	@RequestMapping(value = "/trx/auth/delete/role/v.1", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiBaseResponse> deleteRole(Authentication authentication,
													  @RequestBody(required = true) List<String> datas) throws Exception {
		this.roleService.deleteRoles(datas);
		return new ResponseEntity<ApiBaseResponse>(new ApiBaseResponse(), HttpStatus.OK);
	}

	@RequestMapping(value = "/trx/post/switch/role/v.1", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, List<MenuDto>>> trxSwitchRole(Authentication authentication,
																@RequestHeader(name = HttpHeaders.ACCEPT_LANGUAGE, required = false) String locale,
																@RequestBody(required = true) Map<String, Object> data) throws Exception {
		String username = authentication.getName();
		return new ResponseEntity<Map<String, List<MenuDto>>>(this.userService.switchRole(data, username, locale), HttpStatus.OK);
	}
	
	@RequestMapping(value = "/vw/get/user/role/v.1", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<RoleDto> getUserRole(Authentication authentication,
			@RequestHeader(name = HttpHeaders.ACCEPT_LANGUAGE, required = false) String locale) throws Exception {
		String username = authentication.getName();
		return new ResponseEntity<RoleDto>(this.userService.getUserRole(username), HttpStatus.OK);
	}

	@RequestMapping(value = "/vw/get/select/user/role/v.1", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<SelectResponseDto> getSelectAllLocale(Authentication authentication) throws Exception {
		String username = authentication.getName();
		return new ResponseEntity<SelectResponseDto>(this.userService.getSelectUserRole(username), HttpStatus.OK);
	}
	
}
