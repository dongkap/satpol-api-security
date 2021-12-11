package com.dongkap.security.service;

import java.time.LocalDate;
import java.time.Period;
import java.util.Date;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dongkap.common.exceptions.SystemErrorException;
import com.dongkap.common.pattern.PatternGlobal;
import com.dongkap.common.security.UserPrincipal;
import com.dongkap.common.utils.DateUtil;
import com.dongkap.common.utils.ErrorCode;
import com.dongkap.dto.common.ApiBaseResponse;
import com.dongkap.dto.security.ContactUserDto;
import com.dongkap.dto.security.PersonalInfoDto;
import com.dongkap.dto.security.ProfileDto;
import com.dongkap.security.dao.ContactUserRepo;
import com.dongkap.security.dao.ParameterI18nRepo;
import com.dongkap.security.dao.PersonalInfoRepo;
import com.dongkap.security.dao.UserRepo;
import com.dongkap.security.entity.ContactUserEntity;
import com.dongkap.security.entity.ParameterI18nEntity;
import com.dongkap.security.entity.PersonalInfoEntity;
import com.dongkap.security.entity.UserEntity;

@Service("profileService")
public class ProfileImplService {

	protected Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private UserRepo userRepo;

	@Autowired
	private ContactUserRepo contactUserRepo;

	@Autowired
	private PersonalInfoRepo personalInfoRepo;

	@Autowired
	private ParameterI18nRepo parameterI18nRepo;

	@Transactional
	public ApiBaseResponse doUpdateProfile(ProfileDto p_dto,  UserPrincipal p_user, String p_locale) throws Exception {
		if (p_user.getUsername() != null) {
			UserEntity user = this.userRepo.loadByUsername(p_user.getUsername());
			if(user == null)
				throw new SystemErrorException(ErrorCode.ERR_SYS0404);
			if (p_dto.getName() == null) {
				throw new SystemErrorException(ErrorCode.ERR_SYS0405);
			}
			if (!p_dto.getEmail().equals(user.getEmail())) {
				if (p_dto.getEmail().matches(PatternGlobal.EMAIL.getRegex())) {
					UserEntity tmpUser = this.userRepo.loadByUsernameOrEmail(p_dto.getEmail(), p_dto.getEmail());
					if(tmpUser == null) {
						user.setEmail(p_dto.getEmail());
					} else {
						throw new SystemErrorException(ErrorCode.ERR_SCR0010);
					}
				} else
					throw new SystemErrorException(ErrorCode.ERR_SCR0008);
			}
			user.setFullname(p_dto.getName());
			this.userRepo.save(user);

			if(p_dto.getContact() != null) {
				this.updateContact(p_dto, user);
			}
			if(p_dto.getPersonalInfo() != null) {
				this.updatePersonalInfo(p_dto, user);
			}
			return null;
		} else
			throw new SystemErrorException(ErrorCode.ERR_SYS0404);
	}

	@Transactional
	public void doUpdatePhoto(Map<String, String> url, Authentication authentication, String locale) throws Exception {
		UserPrincipal user = (UserPrincipal) authentication.getPrincipal();
		if (user.getUsername() != null && url != null) {
			UserEntity userEntity = this.userRepo.findByUsername(user.getUsername());
			userEntity.setImage(url.get("url"));
			userEntity.setModifiedBy(user.getUsername());
			userEntity.setModifiedDate(new Date());
			this.userRepo.save(userEntity);
		} else
			throw new SystemErrorException(ErrorCode.ERR_SYS0404);
	}
	
	@Transactional
	private ProfileDto getProfile(String p_username, String p_locale) throws Exception {
		if (p_username != null) {
			ProfileDto dto = new ProfileDto();
			UserEntity user = this.userRepo.loadByUsername(p_username);
			dto.setUsername(p_username);
			dto.setName(user.getName());
			dto.setEmail(user.getEmail());
			dto.setImage(user.getImage());
			dto.setContact(this.getContact(p_username, p_locale));
			dto.setPersonalInfo(this.getPersonalInfo(p_username, p_locale));
			return dto;
		} else
			throw new SystemErrorException(ErrorCode.ERR_SYS0404);
	}

	public ProfileDto getProfile(Authentication authentication, String p_locale) throws Exception {
		UserPrincipal user = (UserPrincipal) authentication.getPrincipal();
		if (user.getUsername() != null) {
			return getProfile(user.getUsername(), p_locale);
		} else
			throw new SystemErrorException(ErrorCode.ERR_SYS0404);
	}
	
	private ContactUserDto getContact(String p_username, String p_locale) {
		ContactUserEntity contactUser = this.contactUserRepo.findByUser_Username(p_username); 
		ContactUserDto contactUserDto = null;
		if(contactUser != null) {
			contactUserDto = new ContactUserDto();
			contactUserDto.setAddress(contactUser.getAddress());
			contactUserDto.setCountry(contactUser.getCountry());
			contactUserDto.setCountryCode(contactUser.getCountryCode());
			contactUserDto.setProvince(contactUser.getProvince());
			contactUserDto.setProvinceCode(contactUser.getProvinceCode());
			contactUserDto.setCity(contactUser.getCity());
			contactUserDto.setCityCode(contactUser.getCityCode());
			contactUserDto.setDistrict(contactUser.getDistrict());
			contactUserDto.setDistrictCode(contactUser.getDistrictCode());
			contactUserDto.setSubDistrict(contactUser.getSubDistrict());
			contactUserDto.setSubDistrictCode(contactUser.getSubDistrictCode());
			contactUserDto.setZipcode(contactUser.getZipcode());	
			contactUserDto.setPhoneNumber(contactUser.getPhoneNumber());
		}
		return contactUserDto;
	}
	
	private void updateContact(ProfileDto p_dto, UserEntity user) throws Exception {
		ContactUserEntity contactUser = this.contactUserRepo.findByUser_Username(user.getUsername()); 
		if (contactUser == null) {
			contactUser = new ContactUserEntity();
		}
		if (p_dto.getContact().getAddress() == null) {
			throw new SystemErrorException(ErrorCode.ERR_SYS0405);
		}
		if (p_dto.getContact().getPhoneNumber() != null) {
			if (p_dto.getContact().getPhoneNumber().matches(PatternGlobal.PHONE_NUMBER.getRegex())) {
				contactUser.setPhoneNumber(p_dto.getContact().getPhoneNumber());	
			} else
				throw new SystemErrorException(ErrorCode.ERR_SCR0007A);
		} else
			throw new SystemErrorException(ErrorCode.ERR_SYS0405);
		contactUser.setAddress(p_dto.getContact().getAddress());
		contactUser.setCountry(p_dto.getContact().getCountry());
		contactUser.setCountryCode(p_dto.getContact().getCountryCode());
		contactUser.setProvince(p_dto.getContact().getProvince());
		contactUser.setProvinceCode(p_dto.getContact().getProvinceCode());
		contactUser.setCity(p_dto.getContact().getCity());
		contactUser.setCityCode(p_dto.getContact().getCityCode());
		contactUser.setDistrict(p_dto.getContact().getDistrict());
		contactUser.setDistrictCode(p_dto.getContact().getDistrictCode());
		contactUser.setSubDistrict(p_dto.getContact().getSubDistrict());
		contactUser.setSubDistrictCode(p_dto.getContact().getSubDistrictCode());
		contactUser.setZipcode(p_dto.getContact().getZipcode());
		contactUser.setUser(user);
		this.contactUserRepo.save(contactUser);
	}
	
	private PersonalInfoDto getPersonalInfo(String p_username, String p_locale) {
		PersonalInfoEntity personalInfo = this.personalInfoRepo.findByUser_Username(p_username);
		PersonalInfoDto personalInfoDto = null;
		if(personalInfo != null) {
			personalInfoDto = new PersonalInfoDto();
			personalInfoDto.setIdNumber(personalInfo.getIdNumber());
			personalInfoDto.setPlaceOfBirth(personalInfo.getPlaceOfBirth());	
			personalInfoDto.setDateOfBirth(DateUtil.DATE.format(personalInfo.getDateOfBirth()));
			personalInfoDto.setAge(this.calculateAge(personalInfo.getDateOfBirth(), new Date()));
			personalInfoDto.setGenderCode(personalInfo.getGender());
			try {
				ParameterI18nEntity parameterI18n = parameterI18nRepo.findByParameter_ParameterCodeAndLocaleCode(personalInfo.getGender(), p_locale);
				personalInfoDto.setGenderValue(parameterI18n.getParameterValue());
			} catch (Exception e) {}
		}
		return personalInfoDto;
	}
	
	private void updatePersonalInfo(ProfileDto p_dto, UserEntity user) throws Exception {
		PersonalInfoEntity personalInfo = this.personalInfoRepo.findByUser_Username(user.getUsername()); 
		if (personalInfo == null) {
			personalInfo = new PersonalInfoEntity();
		}
		if (p_dto.getPersonalInfo().getIdNumber() == null ||
				p_dto.getPersonalInfo().getGenderCode() == null ||
				p_dto.getPersonalInfo().getPlaceOfBirth() == null ||
				p_dto.getPersonalInfo().getDateOfBirth() == null) {
			throw new SystemErrorException(ErrorCode.ERR_SYS0405);
		}
		personalInfo.setIdNumber(p_dto.getPersonalInfo().getIdNumber());
		personalInfo.setGender(p_dto.getPersonalInfo().getGenderCode());
		personalInfo.setPlaceOfBirth(p_dto.getPersonalInfo().getPlaceOfBirth());
		personalInfo.setDateOfBirth(DateUtil.DATE.parse(p_dto.getPersonalInfo().getDateOfBirth()));
		personalInfo.setHeight(p_dto.getPersonalInfo().getHeight());
		personalInfo.setWeight(p_dto.getPersonalInfo().getWeight());
		personalInfo.setUser(user);
		this.personalInfoRepo.save(personalInfo);
	}

    private int calculateAge(Date birthDate, Date currentDate) {
    	return Period.between(toLocalDate(birthDate), toLocalDate(currentDate)).getYears();
    }

    public LocalDate toLocalDate(Date dateToConvert) {
        return new java.sql.Date(dateToConvert.getTime()).toLocalDate();
    }

}
