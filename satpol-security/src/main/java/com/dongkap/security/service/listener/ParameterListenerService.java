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
import com.dongkap.security.dao.ParameterRepo;
import com.dongkap.security.entity.ParameterEntity;
import com.dongkap.security.entity.ParameterI18nEntity;

import lombok.SneakyThrows;

@Service
public class ParameterListenerService extends CommonStreamListener<CommonStreamMessageDto> {

	@Autowired
	private ParameterI18nRepo parameterI18nRepo;

	@Autowired
	private ParameterRepo parameterRepo;

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
		        		if(value.getStatus().equalsIgnoreCase(ParameterStatic.PERSIST_DATA)) {
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
			ParameterI18nEntity parameterI18n = parameterI18nRepo.findById(request.getParameterI18nId()).orElse(null);
			if(parameterI18n == null) {
				parameterI18n = new ParameterI18nEntity();
				parameterI18n.setId(request.getParameterI18nId());
				parameterI18n.setLocaleCode(request.getLocale());
				ParameterEntity parameter = parameterRepo.findByParameterCode(request.getParameterCode());
				if(parameter == null) {
					parameter = new ParameterEntity();
					parameter.setId(request.getParameterId());
					parameter.setParameterCode(request.getParameterCode());
					parameter.getParameterI18n().add(parameterI18n);
				}
				parameterI18n.setParameter(parameter);
			}
    		parameterI18n.setParameterValue(request.getParameterValue());
    		parameterI18nRepo.saveAndFlush(parameterI18n);
		} catch (Exception e) {
			LOGGER.warn("Stream Update : {}", e.getMessage());
		}
	}
}
