package com.dongkap.security.api;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
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
import com.dongkap.dto.security.MenuDto;
import com.dongkap.dto.security.MenuItemDto;
import com.dongkap.dto.select.SelectResponseDto;
import com.dongkap.dto.tree.TreeDto;
import com.dongkap.security.service.MenuImplService;

@RestController
@RequestMapping(ResourceCode.SECURITY_PATH)
public class MenuController extends BaseControllerException {

	@Autowired
	private MenuImplService menuService;
	
	@RequestMapping(value = "/vw/get/menus/v.1", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, List<MenuDto>>> getAllMenuI18n(Authentication authentication,
			@RequestHeader(name = HttpHeaders.ACCEPT_LANGUAGE, required = false) String locale) throws Exception {
		UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
		return new ResponseEntity<Map<String, List<MenuDto>>>(menuService.loadAllMenuByRole(userPrincipal.getAuthorityDefault(), locale), HttpStatus.OK);
	}
	
	@RequestMapping(value = "/vw/auth/tree/menus/v.1/{type}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<TreeDto<MenuItemDto>>> getTreeMenuI18n(Authentication authentication,
			@PathVariable(required = true) String type,
			@RequestHeader(name = HttpHeaders.ACCEPT_LANGUAGE, required = false) String locale) throws Exception {
		return new ResponseEntity<List<TreeDto<MenuItemDto>>>(menuService.loadTreeMenu(type, locale), HttpStatus.OK);
	}

	@RequestMapping(value = "/vw/auth/select/main-menus/v.1", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<SelectResponseDto> getSelectRootMainMenus(Authentication authentication,
			@RequestHeader(name = HttpHeaders.ACCEPT_LANGUAGE, required = false) String locale) throws Exception {
		return new ResponseEntity<SelectResponseDto>(menuService.getSelectRootMainMenus("main",locale), HttpStatus.OK);
	}
	
	@RequestMapping(value = "/vw/auth/tree/menu/function/v.1/{type}/{role}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<TreeDto<MenuItemDto>>> getTreeMenuFunctionControl(Authentication authentication,
			@PathVariable(required = true) String type,
			@PathVariable(required = true) String role,
			@RequestHeader(name = HttpHeaders.ACCEPT_LANGUAGE, required = false) String locale) throws Exception {
		return new ResponseEntity<List<TreeDto<MenuItemDto>>>(menuService.loadTreeMenuFunctionControl(type, role, locale), HttpStatus.OK);
	}
	
	@ResponseSuccess(SuccessCode.OK_DEFAULT)
	@RequestMapping(value = "/trx/auth/menu/v.1", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiBaseResponse> putMenu(Authentication authentication,
												   @RequestHeader(name = HttpHeaders.ACCEPT_LANGUAGE, required = false) String locale,
												   @RequestBody(required = true) MenuItemDto p_dto) throws Exception {
		UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
		return new ResponseEntity<ApiBaseResponse>(menuService.doPostMenu(p_dto, userPrincipal, locale), HttpStatus.OK);
	}
	
	@ResponseSuccess(SuccessCode.OK_DELETED)
	@RequestMapping(value = "/trx/auth/delete/menu/v.1/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiBaseResponse> deleteMenu(Authentication authentication,
			@RequestHeader(name = HttpHeaders.ACCEPT_LANGUAGE, required = false) String locale,
			@PathVariable(required = true) String id) throws Exception {
		this.menuService.deleteMenu(id);
		return new ResponseEntity<ApiBaseResponse>(new ApiBaseResponse(), HttpStatus.OK);
	}
	
}
