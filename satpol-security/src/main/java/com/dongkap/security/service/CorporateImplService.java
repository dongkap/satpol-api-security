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
import com.dongkap.common.stream.PublishStream;
import com.dongkap.common.utils.ErrorCode;
import com.dongkap.common.utils.ParameterStatic;
import com.dongkap.common.utils.StreamKeyStatic;
import com.dongkap.dto.common.CommonResponseDto;
import com.dongkap.dto.common.FilterDto;
import com.dongkap.dto.security.CorporateDto;
import com.dongkap.dto.select.SelectDto;
import com.dongkap.dto.select.SelectResponseDto;
import com.dongkap.security.common.CommonService;
import com.dongkap.security.dao.CorporateRepo;
import com.dongkap.security.dao.specification.CorporateSpecification;
import com.dongkap.security.entity.CorporateEntity;

@Service("corporateService")
public class CorporateImplService extends CommonService {

	protected Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private CorporateRepo corporateRepo;

	@Transactional
	public SelectResponseDto getSelect(FilterDto filter) throws Exception {
		Page<CorporateEntity> corporate = corporateRepo.findAll(CorporateSpecification.getSelect(filter.getKeyword()), page(filter.getOrder(), filter.getOffset(), filter.getLimit()));
		final SelectResponseDto response = new SelectResponseDto();
		response.setTotalFiltered(Long.valueOf(corporate.getContent().size()));
		response.setTotalRecord(corporateRepo.count(CorporateSpecification.getSelect(filter.getKeyword())));
		corporate.getContent().forEach(value -> {
			response.getData().add(new SelectDto(value.getCorporateName(), value.getCorporateCode(), !value.getActive(), null));
		});
		return response;
	}

	@Transactional
	public CommonResponseDto<CorporateDto> getDatatable(FilterDto filter) throws Exception {
		Page<CorporateEntity> corporate = corporateRepo.findAll(CorporateSpecification.getDatatable(filter.getKeyword()), page(filter.getOrder(), filter.getOffset(), filter.getLimit()));
		final CommonResponseDto<CorporateDto> response = new CommonResponseDto<CorporateDto>();
		response.setTotalFiltered(Long.valueOf(corporate.getContent().size()));
		response.setTotalRecord(corporateRepo.count(CorporateSpecification.getDatatable(filter.getKeyword())));
		corporate.getContent().forEach(value -> {
			CorporateDto temp = new CorporateDto();
			temp.setCorporateCode(value.getCorporateCode());
			temp.setCorporateName(value.getCorporateName());
			temp.setCorporateNonExpired(value.isCorporateNonExpired());
			temp.setEmail(value.getEmail());
			temp.setAddress(value.getAddress());
			temp.setTelpNumber(value.getTelpNumber());
			temp.setFaxNumber(value.getFaxNumber());
			temp.setActive(value.getActive());
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
	@PublishStream(key = StreamKeyStatic.CORPORATE, status = ParameterStatic.UPDATE_DATA)
	public List<CorporateDto> postCorporate(CorporateDto request, String username) throws Exception {
		CorporateEntity corporate = this.corporateRepo.findByCorporateCode(request.getCorporateCode());
		List<CorporateDto> result = null;
		if (corporate == null) {
			corporate = new CorporateEntity();
		} else {
			request.setId(corporate.getId());
			result = new ArrayList<CorporateDto>();
			result.add(request);
		}
		corporate.setCorporateCode(request.getCorporateCode());
		corporate.setCorporateName(request.getCorporateName());
		corporate.setCorporateNonExpired(request.getCorporateNonExpired());
		corporate.setEmail(request.getEmail());
		corporate.setAddress(request.getAddress());
		corporate.setTelpNumber(request.getTelpNumber());
		corporate.setFaxNumber(request.getFaxNumber());
		corporateRepo.saveAndFlush(corporate);
		return result;
	}

	public void deleteCorporates(List<String> corporateCodes) throws Exception {
		List<CorporateEntity> corporates = corporateRepo.findByCorporateCodeIn(corporateCodes);
		try {
			corporateRepo.deleteInBatch(corporates);			
		} catch (DataIntegrityViolationException e) {
			throw new SystemErrorException(ErrorCode.ERR_SCR0009);
		} catch (ConstraintViolationException e) {
			throw new SystemErrorException(ErrorCode.ERR_SCR0009);
		}
	}

}
