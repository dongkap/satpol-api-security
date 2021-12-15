package com.dongkap.security.service;

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
import com.dongkap.dto.security.AppDto;
import com.dongkap.dto.select.SelectDto;
import com.dongkap.dto.select.SelectResponseDto;
import com.dongkap.security.common.CommonService;
import com.dongkap.security.dao.AppRepo;
import com.dongkap.security.dao.specification.AppSpecification;
import com.dongkap.security.entity.AppEntity;

@Service("AppService")
public class AppImplService extends CommonService {

	protected Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private AppRepo appRepo;

	@Transactional
	public SelectResponseDto getSelectApp(FilterDto filter) throws Exception {
		Page<AppEntity> app = this.appRepo.findAll(AppSpecification.getSelect(filter.getKeyword()), page(filter.getOrder(), filter.getOffset(), filter.getLimit()));
		final SelectResponseDto response = new SelectResponseDto();
		response.setTotalFiltered(Long.valueOf(app.getContent().size()));
		response.setTotalRecord(this.appRepo.count(AppSpecification.getSelect(filter.getKeyword())));
		app.getContent().forEach(value -> {
			response.getData().add(new SelectDto(value.getAppName(), value.getAppCode(), !value.isActive(), null));
		});
		return response;
	}

	@Transactional
	public CommonResponseDto<AppDto> getDatatable(FilterDto filter) throws Exception {
		Page<AppEntity> app = this.appRepo.findAll(AppSpecification.getDatatable(filter.getKeyword()), page(filter.getOrder(), filter.getOffset(), filter.getLimit()));
		final CommonResponseDto<AppDto> response = new CommonResponseDto<AppDto>();
		response.setTotalFiltered(Long.valueOf(app.getContent().size()));
		response.setTotalRecord(this.appRepo.count(AppSpecification.getDatatable(filter.getKeyword())));
		app.getContent().forEach(value -> {
			AppDto temp = new AppDto();
			temp.setAppCode(value.getAppCode());
			temp.setAppName(value.getAppName());
			temp.setDescription(value.getDescription());
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
	public void postApp(AppDto request, String username) throws Exception {
		AppEntity app = this.appRepo.findByAppCode(request.getAppCode());
		if (app == null) {
			app = new AppEntity();
		}
		app.setAppCode(request.getAppCode());
		app.setAppName(request.getAppName());
		app.setDescription(request.getDescription());
		this.appRepo.saveAndFlush(app);
	}

	public void deleteApps(List<String> appCodes) throws Exception {
		List<AppEntity> apps = this.appRepo.findByAppCodeIn(appCodes);
		try {
			this.appRepo.deleteInBatch(apps);			
		} catch (DataIntegrityViolationException e) {
			throw new SystemErrorException(ErrorCode.ERR_SCR0009);
		} catch (ConstraintViolationException e) {
			throw new SystemErrorException(ErrorCode.ERR_SCR0009);
		}
	}

}
