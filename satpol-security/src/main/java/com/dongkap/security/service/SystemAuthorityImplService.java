package com.dongkap.security.service;

import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import com.dongkap.common.exceptions.SystemErrorException;
import com.dongkap.common.utils.ErrorCode;
import com.dongkap.dto.common.CommonResponseDto;
import com.dongkap.dto.common.FilterDto;
import com.dongkap.dto.security.SystemAuthorityDto;
import com.dongkap.dto.select.SelectDto;
import com.dongkap.dto.select.SelectResponseDto;
import com.dongkap.security.common.CommonService;
import com.dongkap.security.dao.SystemAuthorityRepo;
import com.dongkap.security.dao.specification.SystemAuhtoritySpecification;
import com.dongkap.security.entity.SystemAuthorityEntity;

@Service("systemAuthorityService")
public class SystemAuthorityImplService extends CommonService {

	protected Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private SystemAuthorityRepo systemAuthorityRepo;

	public SelectResponseDto getSelectGroup(FilterDto filter) throws Exception {
		Page<SystemAuthorityEntity> role = systemAuthorityRepo.findAll(SystemAuhtoritySpecification.getSelect(filter.getKeyword()), page(filter.getOrder(), filter.getOffset(), filter.getLimit()));
		final SelectResponseDto response = new SelectResponseDto();
		response.setTotalFiltered(Long.valueOf(role.getContent().size()));
		response.setTotalRecord(systemAuthorityRepo.count(SystemAuhtoritySpecification.getSelect(filter.getKeyword())));
		role.getContent().forEach(value -> {
			response.getData().add(new SelectDto(value.getName(), value.getCode(), !value.isActive(), null));
		});
		return response;
	}

	public CommonResponseDto<SystemAuthorityDto> getDatatable(FilterDto filter) throws Exception {
		Page<SystemAuthorityEntity> role = systemAuthorityRepo.findAll(SystemAuhtoritySpecification.getDatatable(filter.getKeyword()), page(filter.getOrder(), filter.getOffset(), filter.getLimit()));
		final CommonResponseDto<SystemAuthorityDto> response = new CommonResponseDto<SystemAuthorityDto>();
		response.setTotalFiltered(Long.valueOf(role.getContent().size()));
		response.setTotalRecord(systemAuthorityRepo.count(SystemAuhtoritySpecification.getDatatable(filter.getKeyword())));
		role.getContent().forEach(value -> {
			SystemAuthorityDto temp = new SystemAuthorityDto();
			temp.setCode(value.getCode());
			temp.setName(value.getName());
			temp.setActive(value.isActive());
			temp.setVersion(value.getVersion());
			temp.setCreatedDate(value.getCreatedDate());
			temp.setCreatedBy(value.getCreatedBy());
			temp.setModifiedDate(value.getModifiedDate());
			temp.setModifiedBy(value.getModifiedBy());
			response.getData().add(temp);
		});
		return response;
	}
	
	@Transactional
	public void postCode(SystemAuthorityDto request, String username) throws Exception {
		if (request.getCode() != null && request.getName() != null) {
			SystemAuthorityEntity group = systemAuthorityRepo.findByCode(request.getCode());
			if (group == null) {
				group = new SystemAuthorityEntity();
				group.setCode(request.getCode());
				group.setCreatedBy(username);
				group.setCreatedDate(new Date());
			} else {
				group.setModifiedBy(username);
				group.setModifiedDate(new Date());				
			}
			group.setName(request.getName());
			group = systemAuthorityRepo.saveAndFlush(group);
		} else {
			throw new SystemErrorException(ErrorCode.ERR_SYS0404);
		}
	}

	public void deleteSysAuths(List<String> codes) throws Exception {
		List<SystemAuthorityEntity> groupCodes = systemAuthorityRepo.findByCodeIn(codes);
		try {
			systemAuthorityRepo.deleteInBatch(groupCodes );			
		} catch (DataIntegrityViolationException e) {
			throw new SystemErrorException(ErrorCode.ERR_SCR0009);
		} catch (ConstraintViolationException e) {
			throw new SystemErrorException(ErrorCode.ERR_SCR0009);
		}
	}

}
