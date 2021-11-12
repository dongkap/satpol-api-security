package com.dongkap.security.service;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dongkap.common.exceptions.SystemErrorException;
import com.dongkap.common.security.UserPrincipal;
import com.dongkap.common.utils.ErrorCode;
import com.dongkap.common.utils.SuccessCode;
import com.dongkap.dto.common.ApiBaseResponse;
import com.dongkap.dto.security.SettingsDto;
import com.dongkap.security.dao.SettingsRepo;
import com.dongkap.security.entity.SettingsEntity;

@Service("settingsService")
public class SettingsImplService {

	protected Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private SettingsRepo settingsRepo;
	
	@Autowired
	private MessageSource messageSource;

	@Transactional
	public ApiBaseResponse doUpdateSettings(SettingsDto p_dto, UserPrincipal userPrincipal, String p_locale) throws Exception {
		if (userPrincipal.getUsername() != null) {
			SettingsEntity settings = this.settingsRepo.findByUser_Username(userPrincipal.getUsername());
			settings.setTheme(p_dto.getTheme());
			if (p_dto.getLocaleCode() != null)
				settings.setLocaleCode(p_dto.getLocaleCode());
			if (p_dto.getLocaleIdentifier() != null)
				settings.setLocaleIdentifier(p_dto.getLocaleIdentifier());
			if (p_dto.getLocaleIcon() != null)
				settings.setLocaleIcon(p_dto.getLocaleIcon());
			this.settingsRepo.save(settings);
			ApiBaseResponse response = new ApiBaseResponse();
			Locale locale = Locale.forLanguageTag(p_dto.getLocaleCode());
			response.setRespStatusCode(SuccessCode.OK_SCR002.name());
			response.getRespStatusMessage().put(SuccessCode.OK_SCR002.name(), messageSource.getMessage(SuccessCode.OK_SCR002.name(), null, locale));
			return response;
		} else
			throw new SystemErrorException(ErrorCode.ERR_SYS0404);
	}
	
	public SettingsDto getSettings(UserPrincipal userPrincipal, String p_locale) throws Exception {
		if (userPrincipal.getUsername() != null) {
			SettingsDto dto = new SettingsDto();
			SettingsEntity settings = this.settingsRepo.findByUser_Username(userPrincipal.getUsername());
			dto.setLocaleCode(settings.getLocaleCode());
			dto.setLocaleIdentifier(settings.getLocaleIdentifier());
			dto.setLocaleIcon(settings.getLocaleIcon());
			dto.setTheme(settings.getTheme());
			return dto;
		} else
			throw new SystemErrorException(ErrorCode.ERR_SYS0404);
	}

}
