package com.dongkap.security.service;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dongkap.common.exceptions.SystemErrorException;
import com.dongkap.common.pattern.PatternGlobal;
import com.dongkap.common.security.AESEncrypt;
import com.dongkap.common.utils.ErrorCode;
import com.dongkap.dto.common.ApiBaseResponse;
import com.dongkap.dto.security.ActivateAccountDto;
import com.dongkap.security.dao.UserRepo;
import com.dongkap.security.entity.UserEntity;

@Service("activateAccountService")
public class ActivateAccountImplService {

	protected Logger LOGGER = LoggerFactory.getLogger(this.getClass());
	
	@Value("${dongkap.signature.aes.secret-key}")
	private String secretKey;

	@Autowired
	private UserRepo userRepo;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Transactional
	public ApiBaseResponse verificationActivateAccount(ActivateAccountDto p_dto, String p_locale) throws Exception {
		if(p_dto.getActivateId() != null && p_dto.getActivateCode() != null) {
			UserEntity userEntity = userRepo.loadByIdAndActivateCode(p_dto.getActivateId(), p_dto.getActivateCode());
			if(userEntity != null) {
				if(!(new Date().after(userEntity.getActivateExpired()))) {
					return null;
				} else
					throw new SystemErrorException(ErrorCode.ERR_SYS0002);
			} else
				throw new SystemErrorException(ErrorCode.ERR_SCR0014);
		} else
			throw new SystemErrorException(ErrorCode.ERR_SYS0002);
	}

	@Transactional
	public ApiBaseResponse activateAccount(ActivateAccountDto p_dto, String p_locale) throws Exception {
		if(p_dto.getActivateId() != null && p_dto.getActivateCode() != null) {
			UserEntity userEntity = userRepo.loadByIdAndActivateCode(p_dto.getActivateId(), p_dto.getActivateCode());
			if(userEntity != null) {
				if(!(new Date().after(userEntity.getActivateExpired()))) {
					String password = AESEncrypt.decrypt(this.secretKey, p_dto.getPassword());
					String confirmPassword = AESEncrypt.decrypt(this.secretKey, p_dto.getConfirmPassword());
					if (password.matches(PatternGlobal.PASSWORD_MEDIUM.getRegex())) {
						if (password.equals(confirmPassword)) {
							userEntity.setPassword(this.passwordEncoder.encode((String)password));
							userEntity.setActivateCode(null);
							userEntity.setActivateExpired(null);
							userRepo.saveAndFlush(userEntity);
							return null;
						} else {
							throw new SystemErrorException(ErrorCode.ERR_SCR0003);
						}
					} else {
						throw new SystemErrorException(ErrorCode.ERR_SCR0005);
					}
				} else
					throw new SystemErrorException(ErrorCode.ERR_SYS0002);
			} else
				throw new SystemErrorException(ErrorCode.ERR_SYS0002);
		} else
			throw new SystemErrorException(ErrorCode.ERR_SYS0404);
	}

}
