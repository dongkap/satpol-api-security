package com.dongkap.security.service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dongkap.common.exceptions.SystemErrorException;
import com.dongkap.common.pattern.PatternGlobal;
import com.dongkap.common.stream.PublishStream;
import com.dongkap.common.utils.DateUtil;
import com.dongkap.common.utils.ErrorCode;
import com.dongkap.common.utils.ParameterStatic;
import com.dongkap.common.utils.RandomString;
import com.dongkap.common.utils.StreamKeyStatic;
import com.dongkap.dto.common.CommonResponseDto;
import com.dongkap.dto.common.FilterDto;
import com.dongkap.dto.notification.MailNotificationDto;
import com.dongkap.dto.security.ContactUserDto;
import com.dongkap.dto.security.CorporateDto;
import com.dongkap.dto.security.EmployeeDto;
import com.dongkap.dto.security.EmployeeListDto;
import com.dongkap.dto.security.EmployeePersonalInfoDto;
import com.dongkap.dto.security.EmployeeRequestAddDto;
import com.dongkap.dto.security.EmployeeStatusDto;
import com.dongkap.dto.security.OccupationDto;
import com.dongkap.dto.security.PersonalInfoDto;
import com.dongkap.dto.security.RoleDto;
import com.dongkap.dto.select.SelectDto;
import com.dongkap.dto.select.SelectResponseDto;
import com.dongkap.security.common.CommonService;
import com.dongkap.security.dao.AppRepo;
import com.dongkap.security.dao.CorporateRepo;
import com.dongkap.security.dao.EducationRepo;
import com.dongkap.security.dao.EmployeeRepo;
import com.dongkap.security.dao.OccupationRepo;
import com.dongkap.security.dao.RoleRepo;
import com.dongkap.security.dao.TrainingRepo;
import com.dongkap.security.dao.UserRepo;
import com.dongkap.security.dao.specification.EmployeeSpecification;
import com.dongkap.security.entity.AppEntity;
import com.dongkap.security.entity.ContactUserEntity;
import com.dongkap.security.entity.CorporateEntity;
import com.dongkap.security.entity.EducationEntity;
import com.dongkap.security.entity.EmployeeEntity;
import com.dongkap.security.entity.OccupationEntity;
import com.dongkap.security.entity.ParameterI18nEntity;
import com.dongkap.security.entity.PersonalInfoEntity;
import com.dongkap.security.entity.RoleEntity;
import com.dongkap.security.entity.SettingsEntity;
import com.dongkap.security.entity.TrainingEntity;
import com.dongkap.security.entity.UserEntity;

@Service("employeeService")
public class EmployeeImplService extends CommonService {

	protected Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private EmployeeRepo employeeRepo;

	@Autowired
	private OccupationRepo occupationRepo;

	@Autowired
	private CorporateRepo corporateRepo;

	@Autowired
	private UserRepo userRepo;
	
	@Autowired
	private RoleRepo roleRepo;
	
	@Autowired
	private AppRepo appRepo;
	
	@Autowired
	private EducationRepo educationRepo;
	
	@Autowired
	private TrainingRepo trainingRepo;
	
	@Value("${dongkap.signature.aes.secret-key}")
	private String secretKey;

	@Value("${dongkap.locale}")
	private String localeCode;

	@Value("${dongkap.web.url.activate-account}")
	private String urlActivateAccount;
	
	@Autowired
	private MessageSource messageSource;
	
	private static final String NO_SCHOOL = "EDUCATIONAL_LEVEL.NO_EDUCATION";

	@Transactional
	public CorporateDto getCorporate(String username) throws Exception {
		EmployeeEntity employee = employeeRepo.findByUser_Username(username);
		if(employee == null) {
			throw new SystemErrorException(ErrorCode.ERR_SYS0001);
		}
		if (employee.getCorporate() == null) {
			throw new SystemErrorException(ErrorCode.ERR_SYS0001);
		}
		CorporateEntity corporate = employee.getCorporate();
		CorporateDto response = new CorporateDto();
		response.setId(corporate.getId());
		response.setCorporateCode(corporate.getCorporateCode());
		response.setCorporateName(corporate.getCorporateName());
		response.setCorporateNonExpired(corporate.isCorporateNonExpired());
		response.setEmail(corporate.getEmail());
		response.setAddress(corporate.getAddress());
		response.setTelpNumber(corporate.getTelpNumber());
		response.setFaxNumber(corporate.getFaxNumber());
		response.setActive(corporate.getActive());
		response.setVersion(corporate.getVersion());
		response.setCreatedDate(corporate.getCreatedDate());
		response.setCreatedBy(corporate.getCreatedBy());
		response.setModifiedDate(corporate.getModifiedDate());
		response.setModifiedBy(corporate.getModifiedBy());
		return response;
	}

	@Transactional
	public CommonResponseDto<EmployeeListDto> getDatatable(Map<String, Object> additionalInfo, FilterDto filter) throws Exception {
		if(additionalInfo.get("corporate_code") == null) {
			throw new SystemErrorException(ErrorCode.ERR_SYS0001);
		}
		filter.getKeyword().put("corporateCode", additionalInfo.get("corporate_code"));
		Page<EmployeeEntity> employee = employeeRepo.findAll(EmployeeSpecification.getDatatable(filter.getKeyword()), page(filter.getOrder(), filter.getOffset(), filter.getLimit()));
		final CommonResponseDto<EmployeeListDto> response = new CommonResponseDto<EmployeeListDto>();
		response.setTotalFiltered(Long.valueOf(employee.getContent().size()));
		response.setTotalRecord(employeeRepo.count(EmployeeSpecification.getDatatable(filter.getKeyword())));
		employee.getContent().forEach(value -> {
			EmployeeListDto temp = new EmployeeListDto();
			temp.setId(value.getId());
			temp.setIdEmployee(value.getIdEmployee());
			temp.setActive(value.getActive());
			temp.setVersion(value.getVersion());
			temp.setCreatedDate(value.getCreatedDate());
			temp.setCreatedBy(value.getCreatedBy());
			temp.setModifiedDate(value.getModifiedDate());
			temp.setModifiedBy(value.getModifiedBy());
			if(value.getOccupation() != null) {
				temp.setOccupationName(value.getOccupation().getName());
			}
			if(value.getUser() != null) {
				temp.getUser().put("fullname", value.getUser().getFullname());
				temp.getUser().put("email", value.getUser().getEmail());
				temp.getUser().put("username", value.getUser().getUsername());
			}
			if(value.getContactUser() != null) {
				temp.setPhoneNumber(value.getContactUser().getPhoneNumber());
				temp.setAddress(value.getContactUser().getAddress());
			}
			response.getData().add(temp);
		});
		return response;
	}

	@Transactional
	public EmployeePersonalInfoDto getEmployeePersonalInfo(Map<String, Object> additionalInfo, Map<String, Object> data, String p_locale) throws Exception {
		if(additionalInfo.get("corporate_code") == null) {
			throw new SystemErrorException(ErrorCode.ERR_SYS0001);
		}
		if(data.get("employeeId") == null) {
			throw new SystemErrorException(ErrorCode.ERR_SYS0001);			
		}
		if(p_locale == null) {
			p_locale = this.localeCode;
		}
		EmployeeEntity employee = employeeRepo.findByIdAndCorporate_CorporateCode(data.get("employeeId").toString(), additionalInfo.get("corporate_code").toString());
		if(employee != null) {
			final String locale = p_locale;
			final EmployeePersonalInfoDto response = new EmployeePersonalInfoDto();
			response.setId(employee.getId());
			response.setIdEmployee(employee.getIdEmployee());
			response.setFullname(employee.getUser().getFullname());
			response.setEmail(employee.getUser().getEmail());
			response.setImage(employee.getUser().getImage());
			response.setActive(employee.getActive());
			response.setVersion(employee.getVersion());
			response.setCreatedDate(employee.getCreatedDate());
			response.setCreatedBy(employee.getCreatedBy());
			response.setModifiedDate(employee.getModifiedDate());
			PersonalInfoDto personalInfo = new PersonalInfoDto();
			personalInfo.setIdNumber(employee.getPersonalInfo().getIdNumber());
			personalInfo.setPlaceOfBirth(employee.getPersonalInfo().getPlaceOfBirth());
			personalInfo.setDateOfBirth(DateUtil.DATE.format(employee.getPersonalInfo().getDateOfBirth()));
			personalInfo.setHeight(employee.getPersonalInfo().getHeight());
			personalInfo.setWeight(employee.getPersonalInfo().getWeight());
			personalInfo.setBloodType(employee.getPersonalInfo().getBloodType());
			personalInfo.setGenderCode(employee.getPersonalInfo().getParameterGender().getParameterCode());
			if(employee.getPersonalInfo().getParameterGender() != null) {
				ParameterI18nEntity parameter = employee.getPersonalInfo().getParameterGender().getParameterI18n().stream().filter(paramI8n->paramI8n.getLocaleCode().equalsIgnoreCase(locale)).findFirst().orElse(null);
				if(parameter != null) {
					personalInfo.setGenderValue(parameter.getParameterValue());
				}
			}
			response.setPersonalInfo(personalInfo);
			ContactUserDto contactUser = new ContactUserDto(); 
			contactUser.setPhoneNumber(employee.getContactUser().getPhoneNumber());
			contactUser.setAddress(employee.getContactUser().getAddress());
			response.setContact(contactUser);
			return response;
		} else
			throw new SystemErrorException(ErrorCode.ERR_SCR0010);
	}

	@Transactional
	public EmployeeStatusDto getEmployeeStatus(Map<String, Object> additionalInfo, Map<String, Object> data) throws Exception {
		if(additionalInfo.get("corporate_code") == null) {
			throw new SystemErrorException(ErrorCode.ERR_SYS0001);
		}
		if(data.get("employeeId") == null) {
			throw new SystemErrorException(ErrorCode.ERR_SYS0001);			
		}
		EmployeeEntity employee = employeeRepo.findByIdAndCorporate_CorporateCode(data.get("employeeId").toString(), additionalInfo.get("corporate_code").toString());
		if(employee != null) {
			final EmployeeStatusDto response = new EmployeeStatusDto();
			response.setId(employee.getId());
			response.setIdEmployee(employee.getIdEmployee());
			response.setFullname(employee.getUser().getFullname());
			response.setEmail(employee.getUser().getEmail());
			response.setUsername(employee.getUser().getUsername());
			response.setDisabled(!employee.getUser().isEnabled());
			response.setLocked(!employee.getUser().isAccountNonLocked());
			response.setAccountExpired(!employee.getUser().isAccountNonExpired());;
			response.setActive(employee.getActive());
			response.setVersion(employee.getVersion());
			response.setCreatedDate(employee.getCreatedDate());
			response.setCreatedBy(employee.getCreatedBy());
			response.setModifiedDate(employee.getModifiedDate());;
			CorporateDto corporate = new CorporateDto();
			corporate.setId(employee.getCorporate().getId());
			corporate.setCorporateCode(employee.getCorporate().getCorporateCode());
			corporate.setCorporateName(employee.getCorporate().getCorporateName());
			response.setCorporate(corporate);
			OccupationDto occupation = new OccupationDto();
			occupation.setId(employee.getOccupation().getId());
			occupation.setCode(employee.getOccupation().getCode());
			occupation.setName(employee.getOccupation().getName());
			response.setOccupation(occupation);
			employee.getUser().getRoles().forEach(roleEntity->{
				RoleDto role = new RoleDto();
				role.setId(roleEntity.getId());
				role.setAuthority(roleEntity.getAuthority());
				role.setDescription(roleEntity.getDescription());
				response.getRoles().add(role);
			});
			if(employee.getParentEmployee() != null) {
				response.setParentId(employee.getParentEmployee().getId());
				response.setParentLabel(employee.getParentEmployee().getIdEmployee() + " - " + employee.getParentEmployee().getUser().getFullname());
				response.setParentValue(employee.getParentEmployee().getId());
			};
			return response;
		} else
			throw new SystemErrorException(ErrorCode.ERR_SCR0010);
	}

	@Transactional
	public SelectResponseDto getSelect(Map<String, Object> additionalInfo, FilterDto filter) throws Exception {
		if(additionalInfo.get("corporate_code") == null) {
			throw new SystemErrorException(ErrorCode.ERR_SYS0001);
		}
		filter.getKeyword().put("corporateCode", additionalInfo.get("corporate_code"));
		Page<EmployeeEntity> employee = employeeRepo.findAll(EmployeeSpecification.getSelect(filter.getKeyword()), page(filter.getOrder(), filter.getOffset(), filter.getLimit()));
		final SelectResponseDto response = new SelectResponseDto();
		response.setTotalFiltered(Long.valueOf(employee.getContent().size()));
		response.setTotalRecord(employeeRepo.count(EmployeeSpecification.getSelect(filter.getKeyword())));
		employee.getContent().forEach(value -> {
			response.getData().add(new SelectDto(value.getIdEmployee() + " - " + value.getUser().getFullname(), value.getId(), !value.getActive(), null));
		});
		return response;
	}

	@Transactional
	public SelectResponseDto getSelectEmployeeParent(Map<String, Object> additionalInfo, FilterDto filter) throws Exception {
		if(additionalInfo.get("corporate_code") == null) {
			throw new SystemErrorException(ErrorCode.ERR_SYS0001);
		}
		filter.getKeyword().put("corporateCode", additionalInfo.get("corporate_code"));
		Page<EmployeeEntity> employee = employeeRepo.findAll(EmployeeSpecification.getSelectEmployeeParent(filter.getKeyword()), page(filter.getOrder(), filter.getOffset(), filter.getLimit()));
		final SelectResponseDto response = new SelectResponseDto();
		response.setTotalFiltered(Long.valueOf(employee.getContent().size()));
		response.setTotalRecord(employeeRepo.count(EmployeeSpecification.getSelectEmployeeParent(filter.getKeyword())));
		employee.getContent().forEach(value -> {
			response.getData().add(new SelectDto(value.getIdEmployee() + " - " + value.getUser().getFullname(), value.getId(), !value.getActive(), null));
		});
		return response;
	}

	@Transactional
	@PublishStream(key = StreamKeyStatic.EMPLOYEE, status = ParameterStatic.INSERT_DATA)
	public List<Object> postEmployee(Map<String, Object> additionalInfo, EmployeeRequestAddDto request, String p_locale) throws Exception {
		if(additionalInfo.get("corporate_code") == null) {
			throw new SystemErrorException(ErrorCode.ERR_SYS0001);
		}
		if(additionalInfo.get("app_code") == null) {
			throw new SystemErrorException(ErrorCode.ERR_SYS0001);
		}
		request.setPassword(new RandomString(6, new SecureRandom(), RandomString.alphanum).nextString());
		UserEntity user = createUser(request, additionalInfo.get("app_code").toString());
		user = this.userRepo.saveAndFlush(user);

		EmployeeEntity employee = new EmployeeEntity();
		employee.setIdEmployee(request.getIdEmployee());
		employee.setLastEducationLevel(request.getLastEducation());
		employee.setUser(user);
		employee.setContactUser(createContact(request.getContact(), user));
		employee.setPersonalInfo(createPersonalInfo(request.getPersonalInfo(), user));
		CorporateEntity corporate = corporateRepo.findByCorporateCode(additionalInfo.get("corporate_code").toString());
		if(corporate == null) {
			throw new SystemErrorException(ErrorCode.ERR_SYS0001);
		}
		employee.setCorporate(corporate);
		EmployeeEntity employeeParent = this.employeeRepo.findById(request.getParentId()).orElse(null);
		employee.setParentEmployee(employeeParent);
		if(request.getOccupation() == null) {
			throw new SystemErrorException(ErrorCode.ERR_SYS0001);
		}
		OccupationEntity occupation = this.occupationRepo.findByCode(request.getOccupation().getCode());
		if(occupation == null) {
			throw new SystemErrorException(ErrorCode.ERR_SYS0001);
		}
		employee.setOccupation(occupation);
		employee = this.employeeRepo.saveAndFlush(employee);

		if(request.getEducation() != null) {
			if(!request.getEducation().getEducationalLevel().equalsIgnoreCase(NO_SCHOOL)) {
				EducationEntity education = new EducationEntity();
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
		}

		if(request.getTraining() != null) {
			if(!request.getTraining().getName().isBlank()) {
				TrainingEntity training = new TrainingEntity();
				training.setCode(request.getTraining().getName().toUpperCase().replaceAll("[^a-zA-Z0-9]+",""));
				training.setName(request.getTraining().getName());
				training.setStartDate(request.getTraining().getStartDate());
				training.setEndDate(request.getTraining().getEndDate());
				training.setEmployee(employee);
				this.trainingRepo.saveAndFlush(training);	
			}
		}

		List<Object> publishDto = new ArrayList<Object>();
		Locale locale = Locale.getDefault();
		if(p_locale == null) {
			p_locale = localeCode;
		}
		locale = Locale.forLanguageTag(p_locale);
		String template = "activate-account_"+locale.getLanguage()+".ftl";
		if(locale == Locale.US)
			template = "activate-account.ftl";
		Map<String, Object> content = new HashMap<String, Object>();
		content.put("fullname", user.getFullname());
		content.put("urlActivateAccount", this.urlActivateAccount +"/"+user.getId()+"/"+user.getActivateCode());
		MailNotificationDto mail = new MailNotificationDto();
		mail.setTo(user.getEmail());
		mail.setSubject(messageSource.getMessage("subject.mail.activate-account", null, locale));
		mail.setContentTemplate(content);
		mail.setFileNameTemplate(template);
		mail.setLocale(p_locale);
		publishDto.add(mail);
        
		request.setId(employee.getId());
		request.setUsername(employee.getUser().getUsername());
		request.getOccupation().setId(occupation.getId());
		request.getOccupation().setCode(occupation.getCode());
		publishDto.add(request);

		return publishDto;
	}

	@Transactional
	@PublishStream(key = StreamKeyStatic.EMPLOYEE, status = ParameterStatic.UPDATE_DATA)
	public List<EmployeeDto> putEmployeePersonalInfo(Map<String, Object> additionalInfo, EmployeePersonalInfoDto request) throws Exception {
		if(additionalInfo.get("corporate_code") == null) {
			throw new SystemErrorException(ErrorCode.ERR_SYS0001);
		}

		EmployeeEntity employee = this.employeeRepo.findByIdAndCorporate_CorporateCode(request.getId(), additionalInfo.get("corporate_code").toString());
		if(employee == null) {
			throw new SystemErrorException(ErrorCode.ERR_SYS0001);	
		}

		UserEntity user = employee.getUser();
		if(user == null) {
			throw new SystemErrorException(ErrorCode.ERR_SYS0001);
		} else {
			UserEntity checkUser = this.userRepo.loadByUsernameOrEmail(request.getEmail().toLowerCase(), request.getEmail().toLowerCase());
			if(checkUser != null) {
				if(!checkUser.getUsername().equals(user.getUsername())) {
					throw new SystemErrorException(ErrorCode.ERR_SCR0010);	
				}
			}
			user.setUsername(request.getEmail());
			user.setEmail(request.getEmail());
			user.setFullname(request.getFullname());
			user = this.userRepo.saveAndFlush(user);
		}
		employee.setUser(user);

		if(employee.getContactUser() == null) {
			throw new SystemErrorException(ErrorCode.ERR_SYS0001);	
		}
		employee.getContactUser().setPhoneNumber(request.getContact().getPhoneNumber());
		employee.getContactUser().setAddress(request.getContact().getAddress());

		if (request.getPersonalInfo().getIdNumber() == null) {
			throw new SystemErrorException(ErrorCode.ERR_SYS0405);
		}
		if(employee.getPersonalInfo() == null) {
			throw new SystemErrorException(ErrorCode.ERR_SYS0001);	
		}
		employee.getPersonalInfo().setIdNumber(request.getPersonalInfo().getIdNumber());

		employee.setIdEmployee(request.getIdEmployee());
		employee = this.employeeRepo.saveAndFlush(employee);

		List<EmployeeDto> publishDto = new ArrayList<EmployeeDto>();
		request.setId(employee.getId());
		request.setUsername(employee.getUser().getUsername());
		OccupationDto occupationDto = new OccupationDto();
		occupationDto.setId(employee.getOccupation().getId());
		occupationDto.setCode(employee.getOccupation().getCode());
		request.setOccupation(occupationDto);
		publishDto.add(request);
		return publishDto;
	}

	@Transactional
	@PublishStream(key = StreamKeyStatic.EMPLOYEE, status = ParameterStatic.UPDATE_DATA)
	public List<EmployeeDto> putEmployeeStatus(Map<String, Object> additionalInfo, EmployeeStatusDto request) throws Exception {
		if(additionalInfo.get("corporate_code") == null) {
			throw new SystemErrorException(ErrorCode.ERR_SYS0001);
		}

		EmployeeEntity employee = this.employeeRepo.findByIdAndCorporate_CorporateCode(request.getId(), additionalInfo.get("corporate_code").toString());
		if(employee == null) {
			throw new SystemErrorException(ErrorCode.ERR_SYS0001);	
		}

		UserEntity user = employee.getUser();
		if(user == null) {
			throw new SystemErrorException(ErrorCode.ERR_SYS0001);
		} else {
	        for(RoleDto roleDto: request.getRoles()) {
	        	RoleEntity role = this.roleRepo.findByAuthority(roleDto.getAuthority());
				user.getRoles().add(role);
	        }
			user.setAuthorityDefault(request.getRoles().get(0).getAuthority());
			user.setAccountNonExpired(!request.getAccountExpired());
			user.setEnabled(!request.getDisabled());
			user.setAccountNonLocked(!request.getLocked());
			user = this.userRepo.saveAndFlush(user);
		}
		employee.setUser(user);
		
		EmployeeEntity employeeParent = this.employeeRepo.findById(request.getParentId()).orElse(null);
		employee.setParentEmployee(employeeParent);
		if(request.getOccupation() == null) {
			throw new SystemErrorException(ErrorCode.ERR_SYS0001);
		}
		OccupationEntity occupation = this.occupationRepo.findByCode(request.getOccupation().getCode());
		if(occupation == null) {
			throw new SystemErrorException(ErrorCode.ERR_SYS0001);
		}
		employee.setOccupation(occupation);
		employee = this.employeeRepo.saveAndFlush(employee);

		List<EmployeeDto> publishDto = new ArrayList<EmployeeDto>();
		request.setId(employee.getId());
		request.setUsername(employee.getUser().getUsername());
		request.getOccupation().setId(occupation.getId());
		request.getOccupation().setCode(occupation.getCode());
		publishDto.add(request);
		return publishDto;
	}

	private UserEntity createUser(EmployeeDto p_dto, String appCode) throws Exception {
		UserEntity user = this.userRepo.loadByUsernameOrEmail(p_dto.getEmail().toLowerCase(), p_dto.getEmail().toLowerCase());
		if(user == null) {
			user = new UserEntity();
			// email as username
			user.setUsername(p_dto.getEmail());
			user.setEmail(p_dto.getEmail());
			user.setFullname(p_dto.getFullname());
			Calendar cal = Calendar.getInstance();
			cal.setTime(new Date());
			cal.add(Calendar.MONTH, 1);
			user.setActivateExpired(cal.getTime());
			user.setActivateCode(new RandomString(6, new SecureRandom(), RandomString.digits).nextString());
			user.setPassword("N/A");
	        for(RoleDto roleDto: p_dto.getRoles()) {
	        	RoleEntity role = this.roleRepo.findByAuthority(roleDto.getAuthority());
				user.getRoles().add(role);
	        }
			user.setAuthorityDefault(p_dto.getRoles().get(0).getAuthority());
	        AppEntity app = this.appRepo.findByAppCode(appCode);
	        user.getApps().add(app);
			user.setAppCode(appCode);
			SettingsEntity settings = new SettingsEntity();
			settings.setUser(user);
			user.setSettings(settings);
			return user;
		} else
			throw new SystemErrorException(ErrorCode.ERR_SCR0010);
	}
	
	private ContactUserEntity createContact(ContactUserDto p_dto, UserEntity user) throws Exception {
		ContactUserEntity contactUser = new ContactUserEntity();
		if (p_dto.getAddress() == null) {
			throw new SystemErrorException(ErrorCode.ERR_SYS0405);
		}
		if (p_dto.getPhoneNumber() != null) {
			if (p_dto.getPhoneNumber().matches(PatternGlobal.PHONE_NUMBER.getRegex())) {
				contactUser.setPhoneNumber(p_dto.getPhoneNumber());	
			} else
				throw new SystemErrorException(ErrorCode.ERR_SCR0007A);
		} else
			throw new SystemErrorException(ErrorCode.ERR_SYS0405);
		contactUser.setAddress(p_dto.getAddress());
		contactUser.setCountry(p_dto.getCountry());
		contactUser.setCountryCode(p_dto.getCountryCode());
		contactUser.setProvince(p_dto.getProvince());
		contactUser.setProvinceCode(p_dto.getProvinceCode());
		contactUser.setCity(p_dto.getCity());
		contactUser.setCityCode(p_dto.getCityCode());
		contactUser.setDistrict(p_dto.getDistrict());
		contactUser.setDistrictCode(p_dto.getDistrictCode());
		contactUser.setSubDistrict(p_dto.getSubDistrict());
		contactUser.setSubDistrictCode(p_dto.getSubDistrictCode());
		contactUser.setZipcode(p_dto.getZipcode());
		contactUser.setUser(user);
		return contactUser;
	}

	private PersonalInfoEntity createPersonalInfo(PersonalInfoDto p_dto, UserEntity user) throws Exception {
		PersonalInfoEntity personalInfo = new PersonalInfoEntity();
		if (p_dto.getIdNumber() == null ||
				p_dto.getGenderCode() == null ||
				p_dto.getPlaceOfBirth() == null ||
				p_dto.getDateOfBirth() == null) {
			throw new SystemErrorException(ErrorCode.ERR_SYS0405);
		}
		personalInfo.setIdNumber(p_dto.getIdNumber());
		personalInfo.setGender(p_dto.getGenderCode());
		personalInfo.setPlaceOfBirth(p_dto.getPlaceOfBirth());
		personalInfo.setDateOfBirth(DateUtil.DATE.parse(p_dto.getDateOfBirth()));
		personalInfo.setHeight(p_dto.getHeight());
		personalInfo.setWeight(p_dto.getWeight());
		personalInfo.setBloodType(p_dto.getBloodType());
		personalInfo.setUser(user);
		return personalInfo;
	}

}
