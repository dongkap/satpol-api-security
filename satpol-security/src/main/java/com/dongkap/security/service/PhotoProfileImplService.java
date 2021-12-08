package com.dongkap.security.service;

import java.io.File;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dongkap.dto.common.ApiBaseResponse;
import com.dongkap.dto.file.FileMetadataDto;
import com.dongkap.security.dao.FileMetadataRepo;
import com.dongkap.security.dao.UserRepo;
import com.dongkap.security.entity.FileMetadataEntity;
import com.dongkap.security.entity.UserEntity;

@Service("photoProfileService")
public class PhotoProfileImplService {

	protected Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	@Autowired
	@Qualifier("fileMetadataRepo")
	private FileMetadataRepo fileMetadataRepo;

	@Autowired
	private FileGenericImplService fileGenericService;

	@Autowired
	private UserRepo userRepo;
	
    @Value("${dongkap.file.path.image.profile}")
    protected String path;

	@Transactional
	public ApiBaseResponse putFile(String filename, byte[] fileContent, String username, String locale) throws Exception {
		String filePath = this.path.concat(username);
		FileMetadataEntity fileExist = fileMetadataRepo.findByLocation(filePath);
		FileMetadataDto fileMetadataDto = null;
		String checksum = null;
		try {
			fileMetadataDto = fileGenericService.putFile(filePath, filename, fileContent);
			checksum = fileMetadataDto.getChecksum();
		} catch (DataIntegrityViolationException e) {
			checksum = fileExist.getChecksum();
			LOGGER.warn(e.getMessage());
		}
		
		if (fileExist != null) {
			if (!fileExist.getChecksum().equals(checksum)) {
				fileMetadataRepo.deleteByChecksum(fileExist.getChecksum());
			    File currentFile = new File(filePath, fileExist.getChecksum());
			    currentFile.delete();
			}
		}

		UserEntity userEntity = this.userRepo.loadByUsername(username);
		userEntity.setImage(checksum);
		userEntity.setModifiedBy(username);
		userEntity.setModifiedDate(new Date());
		this.userRepo.save(userEntity);

		ApiBaseResponse response = new ApiBaseResponse();
		response.getRespStatusMessage().put("checksum", checksum);
		return response;
	}

	@Transactional
	public Resource getFile(String checksum, String username, String locale) throws Exception {
		String path = this.path.concat(username);
		return this.fileGenericService.getFile(checksum, path);
	}
}