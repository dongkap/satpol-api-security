package com.dongkap.security.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dongkap.common.exceptions.SystemErrorException;
import com.dongkap.common.security.UserPrincipal;
import com.dongkap.common.utils.ErrorCode;
import com.dongkap.dto.common.ApiBaseResponse;
import com.dongkap.dto.security.FunctionRequestDto;
import com.dongkap.dto.security.FunctionRoleRequestDto;
import com.dongkap.dto.security.RoleDto;
import com.dongkap.security.dao.FunctionRepo;
import com.dongkap.security.dao.MenuRepo;
import com.dongkap.security.dao.RoleRepo;
import com.dongkap.security.dao.SystemAuthorityRepo;
import com.dongkap.security.entity.FunctionEntity;
import com.dongkap.security.entity.MenuEntity;
import com.dongkap.security.entity.RoleEntity;
import com.dongkap.security.entity.SystemAuthorityEntity;

@Service("functionService")
public class FunctionImplService {

	protected Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	@Autowired
	@Qualifier("functionRepo")
	private FunctionRepo functionRepo;
	
	@Autowired
	@Qualifier("roleRepo")
	private RoleRepo roleRepo;
	
	@Autowired
	@Qualifier("menuRepo")
	private MenuRepo menuRepo;

	@Autowired
	private SystemAuthorityRepo systemAuthorityRepo;
	
	@Value("${dongkap.locale}")
	private String locale;

	@Transactional
	public ApiBaseResponse doPostFunctionRole(FunctionRoleRequestDto p_dto, UserPrincipal userPrincipal, String p_locale) throws Exception {
		if (p_dto != null) {
			RoleEntity role = this.postRole(p_dto.getRole(), p_locale);
			if(p_dto.getMain() == null)
				throw new SystemErrorException(ErrorCode.ERR_SYS0404);
			try {
				List<FunctionEntity> functions = new ArrayList<FunctionEntity>();
				List<MenuEntity> mainMenus = this.menuRepo.loadAllMenuInId(p_dto.getMain());
				List<MenuEntity> extraMenus = this.menuRepo.loadAllMenuInId(p_dto.getExtra());
				for(MenuEntity menu: mainMenus) {
					final FunctionEntity function = new FunctionEntity();
					function.setAccess("read,write,trust");
					function.setMenuId(menu.getId());
					function.setRoleId(role.getId());
					function.setCreatedBy(userPrincipal.getUsername());
					function.setCreatedDate(new Date());
					functions.add(function);
				}
				for(MenuEntity menu: extraMenus) {
					final FunctionEntity function = new FunctionEntity();
					function.setAccess("read,write,trust");
					function.setMenuId(menu.getId());
					function.setRoleId(role.getId());
					function.setCreatedBy(userPrincipal.getUsername());
					function.setCreatedDate(new Date());
					functions.add(function);
				}
				this.functionRepo.deleteFunctionRole(role.getId());
				this.functionRepo.saveAll(functions);
			} catch (Exception e) {
				throw new SystemErrorException(ErrorCode.ERR_SYS0500);
			} 
			return null;
		} else
			throw new SystemErrorException(ErrorCode.ERR_SYS0404);
	}

	private RoleEntity postRole(RoleDto request, String username) throws Exception {
		if (request.getAuthority() != null && request.getDescription() != null) {
			RoleEntity role = this.roleRepo.findByAuthority(request.getAuthority());
			SystemAuthorityEntity sysAuth = this.systemAuthorityRepo.findByCode(request.getGroup().getCode());
			if (sysAuth == null) {
				throw new SystemErrorException(ErrorCode.ERR_SYS0404);
			}
			if (role == null) {
				role = new RoleEntity();
				role.setAuthority(request.getAuthority());
			}
			role.setLevel(request.getLevel());
			role.setSysAuth(sysAuth);
			role.setDescription(request.getDescription());
			return roleRepo.saveAndFlush(role);
		} else {
			throw new SystemErrorException(ErrorCode.ERR_SYS0404);
		}
	}

	@Transactional
	public ApiBaseResponse doPostFunction(FunctionRequestDto p_dto, UserPrincipal userPrincipal, String p_locale) throws Exception {
		if (p_dto != null) {
			if(p_dto.getMenus() == null)
				throw new SystemErrorException(ErrorCode.ERR_SYS0404);
			if(p_dto.getMenus().isEmpty() && p_dto.getAuthority().isEmpty())
				throw new SystemErrorException(ErrorCode.ERR_SYS0404);
			try {
				List<FunctionEntity> functions = new ArrayList<FunctionEntity>();
				List<MenuEntity> menus = this.menuRepo.loadAllMenuInId(p_dto.getMenus());
				RoleEntity role = this.roleRepo.findByAuthority(p_dto.getAuthority());
				for(MenuEntity menu: menus) {
					FunctionEntity function = new FunctionEntity();
					function.setAccess("read,write,trust");
					function.setMenuId(menu.getId());
					function.setRoleId(role.getId());
					// function.setMenu(menu);
					// function.setRole(role);
					function.setCreatedBy(userPrincipal.getUsername());
					function.setCreatedDate(new Date());
					functions.add(function);
				}
				this.functionRepo.deleteFunctionRoleByType(role.getId(), p_dto.getType());
				this.functionRepo.saveAll(functions);
			} catch (Exception e) {
				throw new SystemErrorException(ErrorCode.ERR_SYS0500);
			} 
			return null;
		} else
			throw new SystemErrorException(ErrorCode.ERR_SYS0404);
	}
	
}