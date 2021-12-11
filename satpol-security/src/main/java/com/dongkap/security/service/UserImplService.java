package com.dongkap.security.service;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.dongkap.common.exceptions.SystemErrorException;
import com.dongkap.common.pattern.PatternGlobal;
import com.dongkap.common.security.AESEncrypt;
import com.dongkap.common.utils.ErrorCode;
import com.dongkap.dto.common.ApiBaseResponse;
import com.dongkap.dto.common.CommonResponseDto;
import com.dongkap.dto.common.FilterDto;
import com.dongkap.dto.google.GoogleResponse;
import com.dongkap.dto.security.MenuDto;
import com.dongkap.dto.security.ProfileDto;
import com.dongkap.dto.security.RoleDto;
import com.dongkap.dto.security.SignUpDto;
import com.dongkap.dto.select.SelectDto;
import com.dongkap.dto.select.SelectResponseDto;
import com.dongkap.security.common.CommonService;
import com.dongkap.security.dao.AppRepo;
import com.dongkap.security.dao.RoleRepo;
import com.dongkap.security.dao.UserRepo;
import com.dongkap.security.dao.specification.UserSpecification;
import com.dongkap.security.entity.AppEntity;
import com.dongkap.security.entity.RoleEntity;
import com.dongkap.security.entity.SettingsEntity;
import com.dongkap.security.entity.UserEntity;

@Service("userService")
public class UserImplService extends CommonService {

	protected Logger LOGGER = LoggerFactory.getLogger(this.getClass());
	
	private static final String ROLE_END = "ROLE_END";

	@Autowired
	private UserRepo userRepo;
	
	@Autowired
	private RoleRepo roleRepo;
	
	@Autowired
	private AppRepo appRepo;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private MenuImplService menuService;

    @Autowired
    private RestTemplate restTemplate;
	
	@Value("${dongkap.app-code.default}")
	private String appCode;
	
	@Value("${dongkap.signature.aes.secret-key}")
	private String secretKey;
	
	@Value("${dongkap.recaptcha.secret-key}")
	private String recaptchaSecretKey;
	
	@Value("${dongkap.recaptcha.site-key}")
	private String recaptchaSiteKey;

    private static final String RECAPTCHA_URL = "https://www.google.com/recaptcha/api/siteverify";

	public CommonResponseDto<ProfileDto> getDatatableUser(FilterDto filter) throws Exception {
		Page<UserEntity> user = userRepo.findAll(UserSpecification.getDatatable(filter.getKeyword()), page(filter.getOrder(), filter.getOffset(), filter.getLimit()));
		final CommonResponseDto<ProfileDto> response = new CommonResponseDto<ProfileDto>();
		response.setTotalFiltered(Long.valueOf(user.getContent().size()));
		response.setTotalRecord(userRepo.count(UserSpecification.getDatatable(filter.getKeyword())));
		user.getContent().forEach(value -> {
			ProfileDto temp = new ProfileDto();
			temp.setUsername(value.getUsername());
			temp.setEmail(value.getEmail());
			temp.setActive(value.isActive());
			temp.setVersion(value.getVersion());
			temp.setCreatedDate(value.getCreatedDate());
			temp.setCreatedBy(value.getCreatedBy());
			temp.setModifiedDate(value.getModifiedDate());
			temp.setModifiedBy(value.getModifiedBy());
			response.getData().add(temp);
		});
		return response;
	}

	@Transactional(isolation = Isolation.READ_UNCOMMITTED, rollbackFor = SystemErrorException.class)
	public ApiBaseResponse doSignUp(SignUpDto dto, String locale) throws Exception {
		GoogleResponse googleResponse = this.recaptchaValidation(dto.getRecaptcha(), this.recaptchaSecretKey);
		if(googleResponse.isSuccess()) {
			return signUp(dto, locale);
		} else
			throw new SystemErrorException(ErrorCode.ERR_SCR0013);
	}
	
	private ApiBaseResponse signUp(SignUpDto dto, String locale) throws Exception {
		UserEntity user = this.userRepo.loadByUsernameOrEmail(dto.getUsername().toLowerCase(), dto.getEmail().toLowerCase());
		if(user == null) {
			user = new UserEntity();
			user.setUsername(dto.getUsername());
			user.setEmail(dto.getEmail());
			user.setFullname(dto.getFullname());
			String password = AESEncrypt.decrypt(this.secretKey, dto.getPassword());
			String confirmPassword = AESEncrypt.decrypt(this.secretKey, dto.getConfirmPassword());
			if (password.matches(PatternGlobal.PASSWORD_MEDIUM.getRegex())) {
				if (password.equals(confirmPassword)) {
					user.setPassword(this.passwordEncoder.encode((String)password));
				} else {
					throw new SystemErrorException(ErrorCode.ERR_SCR0011);
				}
			} else {
				throw new SystemErrorException(ErrorCode.ERR_SCR0005);
			}
	        AppEntity app = this.appRepo.findByAppCode(this.appCode);
	        user.getApps().add(app);
			RoleEntity role = this.roleRepo.findByAuthority(ROLE_END);
			user.getRoles().add(role);
			user.setAuthorityDefault(ROLE_END);
			SettingsEntity settings = new SettingsEntity();
			settings.setUser(user);
			user.setSettings(settings);
			user = this.userRepo.saveAndFlush(user);
			return null;
		} else
			throw new SystemErrorException(ErrorCode.ERR_SCR0010);
	}

	@Transactional
	public Map<String, List<MenuDto>> switchRole(Map<String, Object> data, String username, String locale) throws Exception {
		if(data == null || data.isEmpty())
			throw new SystemErrorException(ErrorCode.ERR_SYS0404);
		String authority = data.get("authority").toString();
		UserEntity userEntity = userRepo.findByUsername(username);
		if (userEntity != null) {
			RoleEntity role = userEntity.getRoles().stream().filter(rl -> rl.getAuthority().equals(authority)).findFirst().orElse(null);
			if(role == null)
				throw new SystemErrorException(ErrorCode.ERR_SYS0001);
			userEntity.setAuthorityDefault(role.getAuthority());
			userEntity = userRepo.save(userEntity);
			return menuService.loadAllMenuByRole(userEntity.getAuthorityDefault(), locale);
		} else {
			throw new SystemErrorException(ErrorCode.ERR_SYS0001);	
		}
	}

	public RoleDto getUserRole(String username) throws Exception {
		UserEntity userEntity = userRepo.findByUsername(username);
		if (userEntity != null) {
			RoleEntity role = userEntity.getRoles().stream().filter(rl -> rl.getAuthority().equals(userEntity.getAuthorityDefault())).findFirst().orElse(null);
			if(role == null)
				throw new SystemErrorException(ErrorCode.ERR_SYS0500);
			return role.dto();
		} else {
			throw new SystemErrorException(ErrorCode.ERR_SYS0001);	
		}
	}

	public SelectResponseDto getSelectUserRole(String username) throws Exception {
		UserEntity userEntity = userRepo.findByUsername(username);
		final SelectResponseDto response = new SelectResponseDto();
		response.setTotalFiltered(Long.valueOf(userEntity.getRoles().size()));
		response.setTotalRecord(Long.valueOf(userEntity.getRoles().size()));
		userEntity.getRoles().forEach(value -> {
			response.getData().add(new SelectDto(value.getDescription(), value.getAuthority(), !value.isActive(), null));
		});
		return response;
	}
	
	private GoogleResponse recaptchaValidation(String recaptcha, String recaptchaSecretKey) throws Exception {
		URI verifyUri = URI.create(String.format(RECAPTCHA_URL + "?secret=%s&response=%s", recaptchaSecretKey, recaptcha));
		return this.restTemplate.getForObject(verifyUri, GoogleResponse.class);
	}

}
