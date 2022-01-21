package com.dongkap.security.service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
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
import com.dongkap.dto.security.ContactUserDto;
import com.dongkap.dto.security.CorporateDto;
import com.dongkap.dto.security.EmployeeDto;
import com.dongkap.dto.security.EmployeeListDto;
import com.dongkap.dto.security.EmployeeRequestAddDto;
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

	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Value("${dongkap.signature.aes.secret-key}")
	private String secretKey;

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
	public List<EmployeeDto> postEmployee(Map<String, Object> additionalInfo, EmployeeRequestAddDto request) throws Exception {
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
		employee.setPersonalInfo(createPersonalInfo(request.getPersonalInfo(), user));;
		CorporateEntity corporate = corporateRepo.findByCorporateCode(additionalInfo.get("corporate_code").toString());
		if(corporate == null) {
			throw new SystemErrorException(ErrorCode.ERR_SYS0001);
		}
		employee.setCorporate(corporate);
		EmployeeEntity employeeParent = this.employeeRepo.findById(request.getParentId()).orElse(null);
		employee.setParentEmployee(employeeParent);
		OccupationEntity occupation = this.occupationRepo.findByCode(request.getOccupation().getCode());
		if(occupation == null) {
			throw new SystemErrorException(ErrorCode.ERR_SYS0001);
		}
		employee.setOccupation(occupation);
		employee = this.employeeRepo.saveAndFlush(employee);
		
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
		
		TrainingEntity training = new TrainingEntity();
		training.setCode(request.getTraining().getName().toUpperCase().replaceAll("[^a-zA-Z0-9]+",""));
		training.setName(request.getTraining().getName());
		training.setStartDate(request.getTraining().getStartDate());
		training.setEndDate(request.getTraining().getEndDate());
		training.setEmployee(employee);
		this.trainingRepo.saveAndFlush(training);

		List<EmployeeDto> publishDto = new ArrayList<EmployeeDto>();
		request.setId(employee.getId());;
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
			user.setPassword(this.passwordEncoder.encode(p_dto.getPassword()));
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
