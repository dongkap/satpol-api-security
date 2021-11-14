package com.dongkap.security.service;

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
import com.dongkap.common.utils.ErrorCode;
import com.dongkap.dto.common.ApiBaseResponse;
import com.dongkap.dto.security.ProfileDto;
import com.dongkap.security.dao.ContactUserRepo;
import com.dongkap.security.dao.UserRepo;
import com.dongkap.security.entity.ContactUserEntity;
import com.dongkap.security.entity.UserEntity;

@Service("profileService")
public class ProfileImplService {

	protected Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private ContactUserRepo contactUserRepo;

	@Autowired
	private UserRepo userRepo;

	@Transactional
	public ApiBaseResponse doUpdateProfile(ProfileDto p_dto,  UserPrincipal p_user, String p_locale) throws Exception {
		if (p_user.getUsername() != null) {
			UserEntity user = this.userRepo.loadByUsername(p_user.getUsername());
			if(user == null)
				throw new SystemErrorException(ErrorCode.ERR_SYS0404);
			ContactUserEntity contactUser = user.getContactUser(); 
			if (contactUser != null) {
				if (p_dto.getAddress() != null)
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
				contactUser.setDescription(p_dto.getDescription());
				if (p_dto.getName() != null)
					user.setFullname(p_dto.getName());
				if (!p_dto.getEmail().equals(user.getEmail())) {
					if (p_dto.getEmail().matches(PatternGlobal.EMAIL.getRegex())) {
						user = this.userRepo.loadByUsernameOrEmail(p_dto.getEmail(), p_dto.getEmail());
						if(user == null) {
							user = contactUser.getUser();
							user.setEmail(p_dto.getEmail());
							contactUser.setUser(user);
						} else {
							throw new SystemErrorException(ErrorCode.ERR_SCR0010);
						}
					} else
						throw new SystemErrorException(ErrorCode.ERR_SCR0008);
				}
				if (p_dto.getPhoneNumber() != null) {
					if (p_dto.getPhoneNumber().matches(PatternGlobal.PHONE_NUMBER.getRegex())) {
						contactUser.setPhoneNumber(p_dto.getPhoneNumber());	
					} else
						throw new SystemErrorException(ErrorCode.ERR_SCR0007A);
				}
				contactUser.setModifiedBy(p_user.getUsername());
				contactUser.setModifiedDate(new Date());
				this.contactUserRepo.save(contactUser);
			}
			return null;
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
	
	public ProfileDto getProfileOtherAuth(Map<String, Object> param, String p_locale) throws Exception {
		if (!param.isEmpty()) {
			return getProfile(param.get("username").toString(), p_locale);
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
			dto.setAddress(user.getContactUser().getAddress());
			dto.setCountry(user.getContactUser().getCountry());
			dto.setCountryCode(user.getContactUser().getCountryCode());
			dto.setProvince(user.getContactUser().getProvince());
			dto.setProvinceCode(user.getContactUser().getProvinceCode());
			dto.setCity(user.getContactUser().getCity());
			dto.setCityCode(user.getContactUser().getCityCode());
			dto.setDistrict(user.getContactUser().getDistrict());
			dto.setDistrictCode(user.getContactUser().getDistrictCode());
			dto.setSubDistrict(user.getContactUser().getSubDistrict());
			dto.setSubDistrictCode(user.getContactUser().getSubDistrictCode());
			dto.setZipcode(user.getContactUser().getZipcode());
			dto.setImage(user.getImage());
			dto.setPhoneNumber(user.getContactUser().getPhoneNumber());
			dto.setDescription(user.getContactUser().getDescription());
			return dto;
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

}
