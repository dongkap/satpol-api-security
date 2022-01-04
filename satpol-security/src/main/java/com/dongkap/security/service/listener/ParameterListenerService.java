package com.dongkap.security.service.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dongkap.common.stream.CommonStreamListener;
import com.dongkap.common.utils.ParameterStatic;
import com.dongkap.common.utils.StreamKeyStatic;
import com.dongkap.dto.common.CommonStreamMessageDto;
import com.dongkap.dto.master.ParameterI18nDto;
import com.dongkap.security.dao.ParameterI18nRepo;
import com.dongkap.security.entity.ParameterI18nEntity;

import lombok.SneakyThrows;

@Service
public class ParameterListenerService extends CommonStreamListener<CommonStreamMessageDto> {

	@Autowired
	private ParameterI18nRepo parameterI18nRepo;

    public ParameterListenerService(
    		@Value("${spring.application.name}") String appName,
    		@Value("${spring.application.name}") String groupId) {
		super(appName, groupId, StreamKeyStatic.PARAMETER, CommonStreamMessageDto.class);
	}
	
	@Override
    @SneakyThrows
    @Transactional
	public void onMessage(ObjectRecord<String, CommonStreamMessageDto> message) {
		try {
	        String stream = message.getStream();
	        RecordId id = message.getId();
			LOGGER.info("A message was received stream: [{}], id: [{}]", stream, id);
	        CommonStreamMessageDto value = message.getValue();
	        if(value != null) {
	        	for(Object data: value.getDatas()) {
		        	if(data instanceof ParameterI18nDto) {
		        		ParameterI18nDto param = (ParameterI18nDto)data;
		        		if(value.getStatus().equalsIgnoreCase(ParameterStatic.UPDATE_DATA)) {
			        		this.update(param);
		        		}
		        	}
		        }
	        }
		} catch (Exception e) {
			LOGGER.warn("Stream On Message : {}", e.getMessage());
		}
	}
	
	public void update(ParameterI18nDto request) {
		try {
			ParameterI18nEntity parameterI18n = parameterI18nRepo.findById(request.getParameterI18nUUID()).orElse(null);
			if(parameterI18n != null) {
	    		parameterI18n.setParameterValue(request.getParameterValue());
	    		parameterI18nRepo.save(parameterI18n);
			}
		} catch (Exception e) {
			LOGGER.warn("Stream Update : {}", e.getMessage());
		}	
	}
}
