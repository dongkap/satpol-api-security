package com.dongkap.security.service;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dongkap.common.exceptions.SystemErrorException;
import com.dongkap.common.utils.ErrorCode;
import com.dongkap.dto.common.CommonResponseDto;
import com.dongkap.dto.common.FilterDto;
import com.dongkap.dto.security.RoleDto;
import com.dongkap.dto.select.SelectDto;
import com.dongkap.dto.select.SelectResponseDto;
import com.dongkap.security.common.CommonService;
import com.dongkap.security.dao.SystemAuthorityRepo;
import com.dongkap.security.dao.RoleRepo;
import com.dongkap.security.dao.specification.RoleSpecification;
import com.dongkap.security.entity.SystemAuthorityEntity;
import com.dongkap.security.entity.RoleEntity;

@Service("roleService")
public class RoleImplService extends CommonService {

	protected Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private RoleRepo roleRepo;

	@Autowired
	private SystemAuthorityRepo systemAuthorityRepo;

	public SelectResponseDto getSelectAllRole(FilterDto filter) throws Exception {
		Page<RoleEntity> role = roleRepo.findAll(RoleSpecification.getSelect(filter.getKeyword()), page(filter.getOrder(), filter.getOffset(), filter.getLimit()));
		final SelectResponseDto response = new SelectResponseDto();
		response.setTotalFiltered(Long.valueOf(role.getContent().size()));
		response.setTotalRecord(roleRepo.count(RoleSpecification.getSelect(filter.getKeyword())));
		role.getContent().forEach(value -> {
			response.getData().add(new SelectDto(value.getDescription(), value.getAuthority(), !value.getActive(), null));
		});
		return response;
	}

	public SelectResponseDto getSelectRole(FilterDto filter) throws Exception {
		List<String> notAuthorities = new ArrayList<String>();
		notAuthorities.add("ROLE_ADMINISTRATOR");
		notAuthorities.add("ROLE_END_USER");
		List<RoleEntity> role = roleRepo.findByAuthorityNotIn(notAuthorities);
		final SelectResponseDto response = new SelectResponseDto();
		response.setTotalFiltered(Long.valueOf(role.size()));
		response.setTotalRecord(Long.valueOf(role.size()));
		role.forEach(value -> {
			response.getData().add(new SelectDto(value.getDescription(), value.getAuthority(), !value.getActive(), null));
		});
		return response;
	}

	public CommonResponseDto<RoleDto> getDatatable(FilterDto filter) throws Exception {
		Page<RoleEntity> role = roleRepo.findAll(RoleSpecification.getDatatable(filter.getKeyword()), page(filter.getOrder(), filter.getOffset(), filter.getLimit()));
		final CommonResponseDto<RoleDto> response = new CommonResponseDto<RoleDto>();
		response.setTotalFiltered(Long.valueOf(role.getContent().size()));
		response.setTotalRecord(roleRepo.count(RoleSpecification.getDatatable(filter.getKeyword())));
		role.getContent().forEach(value -> {
			RoleDto temp = new RoleDto();
			temp.setAuthority(value.getAuthority());
			temp.setDescription(value.getDescription());
			temp.setActive(value.getActive());
			temp.setVersion(value.getVersion());
			temp.setCreatedDate(value.getCreatedDate());
			temp.setCreatedBy(value.getCreatedBy());
			temp.setModifiedDate(value.getModifiedDate());
			temp.setModifiedBy(value.getModifiedBy());
			temp.setGroup(value.getSysAuth().dto());
			response.getData().add(temp);
		});
		return response;
	}
	
	@Transactional
	public void postRole(RoleDto request, String username) throws Exception {
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
			role.setSysAuth(sysAuth);
			role.setDescription(request.getDescription());
			roleRepo.saveAndFlush(role);
		} else {
			throw new SystemErrorException(ErrorCode.ERR_SYS0404);
		}
	}

	public void deleteRoles(List<String> autorities) throws Exception {
		List<RoleEntity> roleAutorities = roleRepo.findByAuthorityIn(autorities);
		try {
			roleRepo.deleteInBatch(roleAutorities );			
		} catch (DataIntegrityViolationException e) {
			throw new SystemErrorException(ErrorCode.ERR_SCR0009);
		} catch (ConstraintViolationException e) {
			throw new SystemErrorException(ErrorCode.ERR_SCR0009);
		}
	}

}
