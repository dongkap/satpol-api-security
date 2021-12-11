package com.dongkap.security.api;

import java.security.Principal;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.endpoint.TokenEndpoint;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dongkap.common.exceptions.BaseControllerException;
import com.dongkap.common.exceptions.SystemErrorException;
import com.dongkap.common.utils.ErrorCode;
import com.dongkap.dto.common.ApiBaseResponse;
import com.dongkap.security.service.CheckAccountImplService;

@RestController
public class OAuthController extends BaseControllerException {

	protected Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private CheckAccountImplService checkAccountService;

	@Autowired
	private TokenEndpoint tokenEndpoint;

	@Autowired
	private TokenStore tokenStore;

	@Autowired
	private DefaultTokenServices tokenServices;
	
	@Autowired
	private ResourceServerTokenServices resourceServerTokenServices;

	@Value("#{new Boolean('${dongkap.login.single-session}')}")
	private boolean isSingleSession;

	private boolean isSessionActive = false;

	@RequestMapping(value = "/oauth/token", method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<OAuth2AccessToken> oauthToken(Principal principal,
														@RequestParam Map<String, String> parameters) throws Exception {
		if(!isRefreshTokenRequest(parameters)) {
			this.tokenStore.findTokensByClientIdAndUserName(parameters.get("client_id"), parameters.get("username"))
			.stream().filter(predicate->!predicate.isExpired()).forEach(oauth2AccessToken->{
				OAuth2AccessToken token = this.resourceServerTokenServices.readAccessToken(oauth2AccessToken.getValue());
				if(token != null) {
					this.isSessionActive = true;
				}
			});
		}

		if(this.isSessionActive && this.isSingleSession) {
			this.isSessionActive = false;
			throw new SystemErrorException(ErrorCode.ERR_SCR0000);
		} else {
			return this.tokenEndpoint.postAccessToken(principal, parameters);
		}
	}

	@RequestMapping(value = "/oauth/force", method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<OAuth2AccessToken> forceToken(Principal principal,
														@RequestParam Map<String, String> parameters) throws Exception {
		this.tokenStore.findTokensByClientIdAndUserName(parameters.get("client_id"), parameters.get("username"))
		.stream().filter(predicate->!predicate.isExpired()).forEach(oauth2AccessToken->{
			tokenServices.revokeToken(oauth2AccessToken.getValue());
			tokenStore.removeAccessToken(oauth2AccessToken);
			tokenStore.removeRefreshToken(oauth2AccessToken.getRefreshToken());
		});
		return this.tokenEndpoint.postAccessToken(principal, parameters);
	}

	@RequestMapping(value = "/oauth/check-user", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiBaseResponse> checkUser(Authentication authentication,
			@RequestBody(required = true) Map<String, String> p_dto,
			@RequestHeader(name = HttpHeaders.ACCEPT_LANGUAGE, required = false) String locale) throws Exception {
		ApiBaseResponse response = this.checkAccountService.checkUserByUsenamerOrEmail(p_dto.get("user"), locale);
		if(response.getRespStatusCode() == ErrorCode.ERR_SYS0302.name()) {
			return new ResponseEntity<ApiBaseResponse>(response, HttpStatus.FOUND);
		} else {
			return new ResponseEntity<ApiBaseResponse>(response, HttpStatus.OK);	
		}
	}

	@RequestMapping(value = "/oauth/extract-token", method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<OAuth2AccessToken> extractAccessToken(Authentication authentication,
			@RequestParam("access_token") String accessToken,
			@RequestHeader(name = HttpHeaders.ACCEPT_LANGUAGE, required = false) String locale) throws Exception {
		return new ResponseEntity<OAuth2AccessToken>(this.checkAccountService.extractAccessToken(accessToken), HttpStatus.OK);
	}

	private boolean isRefreshTokenRequest(Map<String, String> parameters) {
		return "refresh_token".equals(parameters.get("grant_type")) && parameters.get("refresh_token") != null;
	}

}
