package com.dongkap.security.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dongkap.dto.common.CommonResponseDto;
import com.dongkap.dto.common.FilterDto;
import com.dongkap.dto.security.EducationDto;
import com.dongkap.security.common.CommonService;
import com.dongkap.security.dao.EducationRepo;
import com.dongkap.security.entity.EducationEntity;
import com.dongkap.security.entity.ParameterI18nEntity;

@Service("educationService")
public class EducationImplService extends CommonService {

	protected Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private EducationRepo educationRepo;

	@Value("${dongkap.locale}")
	private String locale;

	@Transactional
	public CommonResponseDto<EducationDto> getDatatableEmployee(FilterDto filter, String p_locale) throws Exception {
		if(p_locale == null) {
			p_locale = this.locale;
		}
		String employeeId = filter.getKeyword().get("employeeId").toString();
		List<EducationEntity> educations = educationRepo.findByEmployee_Id(employeeId);
		final CommonResponseDto<EducationDto> response = new CommonResponseDto<EducationDto>();
		response.setTotalFiltered(Long.valueOf(educations.size()));
		response.setTotalRecord(Long.valueOf(educations.size()));
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
					temp.setEducationalLevel(parameter.getParameterValue());
				}
			}
			response.getData().add(temp);
		});
		return response;
	}

}
