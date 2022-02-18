package com.dongkap.security.service;

import java.util.List;
import java.util.Map;

import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dongkap.common.exceptions.SystemErrorException;
import com.dongkap.common.utils.ErrorCode;
import com.dongkap.dto.common.CommonResponseDto;
import com.dongkap.dto.common.FilterDto;
import com.dongkap.dto.security.EducationDto;
import com.dongkap.dto.security.EmployeeRequestAddDto;
import com.dongkap.security.common.CommonService;
import com.dongkap.security.dao.EducationRepo;
import com.dongkap.security.dao.EmployeeRepo;
import com.dongkap.security.dao.specification.EducationSpecification;
import com.dongkap.security.entity.EducationEntity;
import com.dongkap.security.entity.EmployeeEntity;
import com.dongkap.security.entity.ParameterI18nEntity;

@Service("educationService")
public class EducationImplService extends CommonService {

	protected Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private EducationRepo educationRepo;

	@Autowired
	private EmployeeRepo employeeRepo;

	@Value("${dongkap.locale}")
	private String locale;

	@Transactional
	public CommonResponseDto<EducationDto> getDatatableEducationEmployee(FilterDto filter, String p_locale) throws Exception {
		if(p_locale == null) {
			p_locale = this.locale;
		}
		Page<EducationEntity> educations = educationRepo.findAll(EducationSpecification.getDatatable(filter.getKeyword()), page(filter.getOrder(), filter.getOffset(), filter.getLimit()));
		final CommonResponseDto<EducationDto> response = new CommonResponseDto<EducationDto>();
		response.setTotalFiltered(Long.valueOf(educations.getContent().size()));
		response.setTotalRecord(educationRepo.count(EducationSpecification.getDatatable(filter.getKeyword())));
		final String locale = p_locale;
		educations.forEach(value -> {
			EducationDto temp = new EducationDto();
			temp.setId(value.getId());
			temp.setSchoolName(value.getSchoolName());
			temp.setDegree(value.getDegree());
			temp.setGrade(value.getGrade());
			temp.setStudy(value.getStudy());
			temp.setStartYear(value.getStartYear());
			temp.setEndYear(value.getEndYear());
			temp.setActive(value.getActive());
			temp.setVersion(value.getVersion());
			temp.setCreatedDate(value.getCreatedDate());
			temp.setCreatedBy(value.getCreatedBy());
			temp.setModifiedDate(value.getModifiedDate());
			temp.setModifiedBy(value.getModifiedBy());
			if(value.getLevel() != null) {
				ParameterI18nEntity parameter = value.getLevel().getParameterI18n().stream().filter(paramI8n->paramI8n.getLocaleCode().equalsIgnoreCase(locale)).findFirst().orElse(null);
				if(parameter != null) {
					temp.setEducationalLevelCode(value.getLevel().getParameterCode());
					temp.setEducationalLevel(parameter.getParameterValue());
				}
			}
			response.getData().add(temp);
		});
		return response;
	}

	@Transactional
	public CommonResponseDto<EducationDto> getDatatableEducationEmployee(String username, FilterDto filter, String p_locale) throws Exception {
		if(p_locale == null) {
			p_locale = this.locale;
		}
		filter.getKeyword().put("username", username);
		Page<EducationEntity> educations = educationRepo.findAll(EducationSpecification.getDatatableEduactionEmployeeProfile(filter.getKeyword()), page(filter.getOrder(), filter.getOffset(), filter.getLimit()));
		final CommonResponseDto<EducationDto> response = new CommonResponseDto<EducationDto>();
		response.setTotalFiltered(Long.valueOf(educations.getContent().size()));
		response.setTotalRecord(educationRepo.count(EducationSpecification.getDatatable(filter.getKeyword())));
		final String locale = p_locale;
		educations.forEach(value -> {
			EducationDto temp = new EducationDto();
			temp.setId(value.getId());
			temp.setSchoolName(value.getSchoolName());
			temp.setDegree(value.getDegree());
			temp.setGrade(value.getGrade());
			temp.setStudy(value.getStudy());
			temp.setStartYear(value.getStartYear());
			temp.setEndYear(value.getEndYear());
			temp.setActive(value.getActive());
			temp.setVersion(value.getVersion());
			temp.setCreatedDate(value.getCreatedDate());
			temp.setCreatedBy(value.getCreatedBy());
			temp.setModifiedDate(value.getModifiedDate());
			temp.setModifiedBy(value.getModifiedBy());
			if(value.getLevel() != null) {
				ParameterI18nEntity parameter = value.getLevel().getParameterI18n().stream().filter(paramI8n->paramI8n.getLocaleCode().equalsIgnoreCase(locale)).findFirst().orElse(null);
				if(parameter != null) {
					temp.setEducationalLevelCode(value.getLevel().getParameterCode());
					temp.setEducationalLevel(parameter.getParameterValue());
				}
			}
			response.getData().add(temp);
		});
		return response;
	}

	@Transactional
	public void postEducationEmployee(Map<String, Object> additionalInfo, EmployeeRequestAddDto request) throws Exception {
		if(additionalInfo.get("corporate_code") == null) {
			throw new SystemErrorException(ErrorCode.ERR_SYS0001);
		}

		EmployeeEntity employee = this.employeeRepo.findByIdAndCorporate_CorporateCode(request.getId(), additionalInfo.get("corporate_code").toString());
		if(employee == null) {
			throw new SystemErrorException(ErrorCode.ERR_SYS0001);
		}

		EducationEntity education = new EducationEntity();
		if(request.getEducation().getId() != null) {
			education = this.educationRepo.findById(request.getEducation().getId()).orElse(new EducationEntity());	
		}
		education.setId(request.getEducation().getId());
		education.setEducationalLevel(request.getEducation().getEducationalLevel());
		education.setDegree(request.getEducation().getDegree());
		education.setGrade(request.getEducation().getGrade());
		education.setStudy(request.getEducation().getStudy());
		education.setSchoolName(request.getEducation().getSchoolName());
		education.setStartYear(request.getEducation().getStartYear());
		education.setEndYear(request.getEducation().getEndYear());
		education.setEmployee(employee);
		this.educationRepo.saveAndFlush(education);
	}

	@Transactional
	public void postEducationEmployee(String username, EmployeeRequestAddDto request) throws Exception {
		EmployeeEntity employee = this.employeeRepo.findByUser_Username(username);
		if(employee == null) {
			throw new SystemErrorException(ErrorCode.ERR_SYS0001);
		}

		EducationEntity education = new EducationEntity();
		if(request.getEducation().getId() != null) {
			education = this.educationRepo.findById(request.getEducation().getId()).orElse(new EducationEntity());	
		}
		education.setId(request.getEducation().getId());
		education.setEducationalLevel(request.getEducation().getEducationalLevel());
		education.setDegree(request.getEducation().getDegree());
		education.setGrade(request.getEducation().getGrade());
		education.setStudy(request.getEducation().getStudy());
		education.setSchoolName(request.getEducation().getSchoolName());
		education.setStartYear(request.getEducation().getStartYear());
		education.setEndYear(request.getEducation().getEndYear());
		education.setEmployee(employee);
		this.educationRepo.saveAndFlush(education);
	}

	public void deleteEducations(List<String> educationIds) throws Exception {
		List<EducationEntity> educations = this.educationRepo.findByIdIn(educationIds);
		try {
			this.educationRepo.deleteInBatch(educations);
		} catch (DataIntegrityViolationException e) {
			throw new SystemErrorException(ErrorCode.ERR_SCR0009);
		} catch (ConstraintViolationException e) {
			throw new SystemErrorException(ErrorCode.ERR_SCR0009);
		}
	}

	public void deleteEducations(List<String> educationIds, String username) throws Exception {
		List<EducationEntity> educations = this.educationRepo.findByIdInAndEmployee_User_Username(educationIds, username);
		try {
			this.educationRepo.deleteInBatch(educations);
		} catch (DataIntegrityViolationException e) {
			throw new SystemErrorException(ErrorCode.ERR_SCR0009);
		} catch (ConstraintViolationException e) {
			throw new SystemErrorException(ErrorCode.ERR_SCR0009);
		}
	}

}
