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
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dongkap.common.exceptions.SystemErrorException;
import com.dongkap.common.pattern.PatternGlobal;
import com.dongkap.common.security.AESEncrypt;
import com.dongkap.common.stream.PublishStream;
import com.dongkap.common.utils.AuthorizationProvider;
import com.dongkap.common.utils.ErrorCode;
import com.dongkap.common.utils.ParameterStatic;
import com.dongkap.common.utils.RandomString;
import com.dongkap.common.utils.StreamKeyStatic;
import com.dongkap.common.utils.SuccessCode;
import com.dongkap.dto.common.ApiBaseResponse;
import com.dongkap.dto.common.CommonStreamMessageDto;
import com.dongkap.dto.notification.MailNotificationDto;
import com.dongkap.dto.security.ForgotPasswordDto;
import com.dongkap.dto.security.RequestForgotPasswordDto;
import com.dongkap.security.dao.UserRepo;
import com.dongkap.security.entity.UserEntity;

@Service("forgotPassword")
public class ForgotPasswordImplService {

	protected Logger LOGGER = LoggerFactory.getLogger(this.getClass());
	
	@Value("${dongkap.signature.aes.secret-key}")
	private String secretKey;

	@Autowired
	private UserRepo userRepo;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private ReactiveRedisOperations<String, CommonStreamMessageDto> reactiveRedisTemplate;

	@Value("${dongkap.web.url.forgot-password}")
	private String urlForgotPassword;
	
	@Autowired
	private MessageSource messageSource;

	@Value("${dongkap.locale}")
	private String localeCode;

	@Transactional
	public ApiBaseResponse requestForgotPassword(RequestForgotPasswordDto p_dto, String p_locale) throws Exception {
		if(p_dto.getEmail() != null) {
			UserEntity userEntity = userRepo.findByEmail(p_dto.getEmail().toLowerCase()).get();	
			if(p_dto.getPin()) {
				this.requestForgotPasswordPin(userEntity, p_locale);
				ApiBaseResponse response = new ApiBaseResponse();
				response.setRespStatusCode(SuccessCode.OK_FORGOT_PASSWORD.name());
				response.getRespStatusMessage().put(response.getRespStatusCode(), userEntity.getId());
				return response;		
			} else {
				this.requestForgotPasswordUrl(userEntity, p_locale);
				return null;
			}		
		} else
			throw new SystemErrorException(ErrorCode.ERR_SYS0404);
	}

	@Transactional
	public ApiBaseResponse verificationForgotPassword(ForgotPasswordDto p_dto, String p_locale) throws Exception {
		if(p_dto.getVerificationId() != null && p_dto.getVerificationCode() != null) {
			UserEntity userEntity = userRepo.loadByIdAndVerificationCode(p_dto.getVerificationId(), p_dto.getVerificationCode());
			if(userEntity != null) {
				if(!(new Date().after(userEntity.getVerificationExpired()))) {
					return null;
				} else
					throw new SystemErrorException(ErrorCode.ERR_SYS0002);
			} else
				throw new SystemErrorException(ErrorCode.ERR_SCR0014);
		} else
			throw new SystemErrorException(ErrorCode.ERR_SYS0002);
	}

	public ApiBaseResponse forgotPassword(ForgotPasswordDto p_dto, String p_locale) throws Exception {
		if(p_dto.getVerificationId() != null && p_dto.getVerificationCode() != null) {
			UserEntity userEntity = userRepo.loadByIdAndVerificationCode(p_dto.getVerificationId(), p_dto.getVerificationCode());
			if(userEntity != null) {
				if(!(new Date().after(userEntity.getVerificationExpired()))) {
					String newPassword = AESEncrypt.decrypt(this.secretKey, p_dto.getNewPassword());
					String confirmPassword = AESEncrypt.decrypt(this.secretKey, p_dto.getConfirmPassword());
					if (newPassword.matches(PatternGlobal.PASSWORD_MEDIUM.getRegex())) {
						if (newPassword.equals(confirmPassword)) {
							userEntity.setPassword(this.passwordEncoder.encode((String)newPassword));
							userEntity.setVerificationCode(null);
							userEntity.setVerificationExpired(null);
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

	public void requestForgotPasswordPin(UserEntity userEntity, String p_locale) throws Exception {
		if(userEntity != null) {
			if(userEntity.getProvider().equals(AuthorizationProvider.local.toString())) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(new Date());
				cal.add(Calendar.SECOND, 120);
				userEntity.setVerificationExpired(cal.getTime());
				userEntity.setVerificationCode(new RandomString(6, new SecureRandom(), RandomString.digits).nextString());
				Locale locale = Locale.getDefault();
				if(p_locale == null) {
					p_locale = localeCode;
				}
				locale = Locale.forLanguageTag(p_locale);
				userEntity = this.userRepo.saveAndFlush(userEntity);
				String template = "forgot-password-pin_"+locale.getLanguage()+".ftl";
				if(locale == Locale.US)
					template = "forgot-password-pin.ftl";
				Map<String, Object> content = new HashMap<String, Object>();
				content.put("fullname", userEntity.getFullname());
				content.put("verificationCode", userEntity.getVerificationCode());
				content.put("locale", locale);
				MailNotificationDto mail = new MailNotificationDto();
				mail.setTo(userEntity.getEmail());
				mail.setSubject(messageSource.getMessage("subject.mail.forgot-password", null, locale));
				mail.setContentTemplate(content);
				mail.setFileNameTemplate(template);
				mail.setLocale(p_locale);
				List<Object> publishDto = new ArrayList<Object>();
				publishDto.add(mail);
				CommonStreamMessageDto message = new CommonStreamMessageDto(StreamKeyStatic.FORGOT_PASSWORD, p_locale, ParameterStatic.NOTIFICATION, publishDto);
				ObjectRecord<String, CommonStreamMessageDto> record = StreamRecords.newRecord()
						.in(StreamKeyStatic.FORGOT_PASSWORD)
                        .ofObject(message);
		        this.reactiveRedisTemplate
		                .opsForStream()
		                .add(record)
		                .subscribe();
			} else
				throw new SystemErrorException(ErrorCode.ERR_SYS0401);
		} else
			throw new SystemErrorException(ErrorCode.ERR_SCR0012);		
	}

	@PublishStream(key = StreamKeyStatic.FORGOT_PASSWORD, status = ParameterStatic.NOTIFICATION)
	public void requestForgotPasswordUrl(UserEntity userEntity, String p_locale) throws Exception {
		if(userEntity != null) {
			if(userEntity.getProvider().equals(AuthorizationProvider.local.toString())) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(new Date());
				cal.add(Calendar.MINUTE, 5);
				userEntity.setVerificationExpired(cal.getTime());
				userEntity.setVerificationCode(new RandomString(8).nextString());
				Locale locale = Locale.getDefault();
				if(p_locale == null) {
					p_locale = localeCode;
				}
				locale = Locale.forLanguageTag(p_locale);
				userEntity = this.userRepo.saveAndFlush(userEntity);
				String template = "forgot-password_"+locale.getLanguage()+".ftl";
				if(locale == Locale.US)
					template = "forgot-password.ftl";
				Map<String, Object> content = new HashMap<String, Object>();
				content.put("fullname", userEntity.getFullname());
				content.put("urlForgotPassword", this.urlForgotPassword +"/"+userEntity.getId()+"/"+userEntity.getVerificationCode());
				MailNotificationDto mail = new MailNotificationDto();
				mail.setTo(userEntity.getEmail());
				mail.setSubject(messageSource.getMessage("subject.mail.forgot-password", null, locale));
				mail.setContentTemplate(content);
				mail.setFileNameTemplate(template);
				mail.setLocale(p_locale);
				List<Object> publishDto = new ArrayList<Object>();
				publishDto.add(mail);
				CommonStreamMessageDto message = new CommonStreamMessageDto(StreamKeyStatic.FORGOT_PASSWORD, p_locale, ParameterStatic.NOTIFICATION, publishDto);
				ObjectRecord<String, CommonStreamMessageDto> record = StreamRecords.newRecord()
						.in(StreamKeyStatic.FORGOT_PASSWORD)
                        .ofObject(message);
		        this.reactiveRedisTemplate
		                .opsForStream()
		                .add(record)
		                .subscribe();
			} else
				throw new SystemErrorException(ErrorCode.ERR_SYS0401);
		} else
			throw new SystemErrorException(ErrorCode.ERR_SCR0012);
	}

}
