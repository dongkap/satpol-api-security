package com.dongkap.security.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.dongkap.common.aspect.ResponseSuccess;
import com.dongkap.common.exceptions.BaseControllerException;
import com.dongkap.common.security.UserPrincipal;
import com.dongkap.common.utils.ResourceCode;
import com.dongkap.common.utils.SuccessCode;
import com.dongkap.dto.common.ApiBaseResponse;
import com.dongkap.dto.security.ProfileDto;
import com.dongkap.security.service.PhotoProfileImplService;
import com.dongkap.security.service.ProfileImplService;

@RestController
@RequestMapping(ResourceCode.PROFILE_PATH)
public class ProfileController extends BaseControllerException {

	@Autowired
	private ProfileImplService profileService;

	@Autowired
	private PhotoProfileImplService photoProfileService;
	
	@ResponseSuccess(SuccessCode.OK_SCR004)
	@RequestMapping(value = "/trx/post/profile/v.1", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiBaseResponse> putProfile(Authentication authentication,
			@RequestHeader(name = HttpHeaders.ACCEPT_LANGUAGE, required = false) String locale,
			@RequestBody(required = true) ProfileDto p_dto) throws Exception {
		UserPrincipal user = (UserPrincipal) authentication.getPrincipal();
		return new ResponseEntity<ApiBaseResponse>(profileService.doUpdateProfile(p_dto, user, locale), HttpStatus.OK);
	}
	
	@RequestMapping(value = "/vw/get/profile/v.1", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ProfileDto> getProfile(Authentication authentication,
			@RequestHeader(name = HttpHeaders.ACCEPT_LANGUAGE, required = false) String locale) throws Exception {
		return new ResponseEntity<ProfileDto>(profileService.getProfile(authentication, locale), HttpStatus.OK);
	}

    @ResponseSuccess(SuccessCode.OK_SCR005)
	@RequestMapping(value = "/trx/post/photo-profile/v.1", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)	
	public ResponseEntity<ApiBaseResponse> putPhotoProfile(Authentication authentication,
			@RequestPart MultipartFile photo,
			@RequestHeader(name = HttpHeaders.ACCEPT_LANGUAGE, required = false) String locale) throws Exception {
		String username = authentication.getName();
		ApiBaseResponse res = this.photoProfileService.putFile(photo.getOriginalFilename(), photo.getBytes(), username, locale);
		return new ResponseEntity<ApiBaseResponse>(res, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/vw/get/photo-profile/v.1/{checksum}", method = RequestMethod.GET, produces = MediaType.IMAGE_PNG_VALUE)	
	public ResponseEntity<Resource> downloadPhotoProfile(Authentication authentication,
			@PathVariable(required = true) String checksum,
			@RequestHeader(name = HttpHeaders.ACCEPT_LANGUAGE, required = false) String locale) throws Exception {
		String username = authentication.getName();
		return new ResponseEntity<Resource>(this.photoProfileService.getFile(checksum, username, locale), HttpStatus.OK);
	}
	
}
