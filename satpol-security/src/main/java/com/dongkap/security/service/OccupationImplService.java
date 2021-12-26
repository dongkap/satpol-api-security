package com.dongkap.security.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dongkap.common.exceptions.SystemErrorException;
import com.dongkap.common.stream.PublishStream;
import com.dongkap.common.utils.ErrorCode;
import com.dongkap.common.utils.ParameterStatic;
import com.dongkap.common.utils.StreamKeyStatic;
import com.dongkap.dto.common.CommonResponseDto;
import com.dongkap.dto.common.FilterDto;
import com.dongkap.dto.security.OccupationDto;
import com.dongkap.dto.select.SelectDto;
import com.dongkap.dto.select.SelectResponseDto;
import com.dongkap.security.common.CommonService;
import com.dongkap.security.dao.CorporateRepo;
import com.dongkap.security.dao.OccupationRepo;
import com.dongkap.security.dao.specification.OccupationSpecification;
import com.dongkap.security.entity.CorporateEntity;
import com.dongkap.security.entity.OccupationEntity;

@Service("occupationService")
public class OccupationImplService extends CommonService {

	protected Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private OccupationRepo occupationRepo;

	@Autowired
	private CorporateRepo corporateRepo;

	@Transactional
	public SelectResponseDto getSelect(Map<String, Object> additionalInfo, FilterDto filter) throws Exception {
		if(additionalInfo.get("corporate_code") == null) {
			throw new SystemErrorException(ErrorCode.ERR_SYS0001);
		}
		filter.getKeyword().put("corporateCode", additionalInfo.get("corporate_code"));
		Page<OccupationEntity> occupation = occupationRepo.findAll(OccupationSpecification.getSelect(filter.getKeyword()), page(filter.getOrder(), filter.getOffset(), filter.getLimit()));
		final SelectResponseDto response = new SelectResponseDto();
		response.setTotalFiltered(Long.valueOf(occupation.getContent().size()));
		response.setTotalRecord(occupationRepo.count(OccupationSpecification.getSelect(filter.getKeyword())));
		occupation.getContent().forEach(value -> {
			response.getData().add(new SelectDto(value.getName(), value.getCode(), !value.isActive(), null));
		});
		return response;
	}

	@Transactional
	public CommonResponseDto<OccupationDto> getDatatable(Map<String, Object> additionalInfo, FilterDto filter) throws Exception {
		if(additionalInfo.get("corporate_code") == null) {
			throw new SystemErrorException(ErrorCode.ERR_SYS0001);
		}
		filter.getKeyword().put("corporateCode", additionalInfo.get("corporate_code"));
		Page<OccupationEntity> occupation = occupationRepo.findAll(OccupationSpecification.getDatatable(filter.getKeyword()), page(filter.getOrder(), filter.getOffset(), filter.getLimit()));
		final CommonResponseDto<OccupationDto> response = new CommonResponseDto<OccupationDto>();
		response.setTotalFiltered(Long.valueOf(occupation.getContent().size()));
		response.setTotalRecord(occupationRepo.count(OccupationSpecification.getDatatable(filter.getKeyword())));
		occupation.getContent().forEach(value -> {
			OccupationDto temp = new OccupationDto();
			temp.setCode(value.getCode());
			temp.setName(value.getName());
			temp.setCorporateCode(value.getCorporate().getCorporateCode());
			temp.setCorporateName(value.getCorporate().getCorporateName());
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
	@PublishStream(key = StreamKeyStatic.OCCUPATION, status = ParameterStatic.UPDATE_DATA)
	public List<OccupationDto> postOccupation(Map<String, Object> additionalInfo, OccupationDto request) throws Exception {
		if(additionalInfo.get("corporate_code") == null) {
			throw new SystemErrorException(ErrorCode.ERR_SYS0001);
		}
		OccupationEntity occupation = this.occupationRepo.findByCode(request.getCode());
		List<OccupationDto> result = null;
		if (occupation == null) {
			CorporateEntity corporate = corporateRepo.findByCorporateCode(additionalInfo.get("corporate_code").toString());
			if(corporate == null) {
				throw new SystemErrorException(ErrorCode.ERR_SYS0001);
			}
			occupation = new OccupationEntity();
			occupation.setCorporate(corporate);
		} else {
			request.setId(occupation.getId());
			result = new ArrayList<OccupationDto>();
			result.add(request);
		}
		occupation.setCode(request.getCode());
		occupation.setName(request.getName());
		occupationRepo.saveAndFlush(occupation);
		return result;
	}

	public void deleteOccupations(List<String> occupationCodes) throws Exception {
		List<OccupationEntity> occupations = occupationRepo.findByCodeIn(occupationCodes);
		try {
			occupationRepo.deleteInBatch(occupations);			
		} catch (DataIntegrityViolationException e) {
			throw new SystemErrorException(ErrorCode.ERR_SCR0009);
		} catch (ConstraintViolationException e) {
			throw new SystemErrorException(ErrorCode.ERR_SCR0009);
		}
	}

}
