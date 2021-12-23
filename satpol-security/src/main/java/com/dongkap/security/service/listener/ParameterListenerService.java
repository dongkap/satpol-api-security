package com.dongkap.security.service.listener;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dongkap.common.utils.StreamKeyStatic;
import com.dongkap.dto.common.CommonStreamMessageDto;
import com.dongkap.dto.master.ParameterI18nDto;
import com.dongkap.security.common.CommonStreamListener;

import lombok.SneakyThrows;

@Service
public class ParameterListenerService extends CommonStreamListener<CommonStreamMessageDto> {

    public ParameterListenerService(
    		@Value("${spring.application.name}") String appName,
    		@Value("${spring.application.name}") String groupId) {
		super(appName, groupId, StreamKeyStatic.PARAMETER, CommonStreamMessageDto.class);
	}
	
	@Override
    @SneakyThrows
    @Transactional
	public void onMessage(ObjectRecord<String, CommonStreamMessageDto> message) {
        String stream = message.getStream();
        RecordId id = message.getId();
		LOGGER.info("A message was received stream: [{}], id: [{}]", stream, id);
        CommonStreamMessageDto value = message.getValue();
        value.getDatas().forEach(data->{
        	if(data instanceof ParameterI18nDto) {
        		ParameterI18nDto param = (ParameterI18nDto)data;
        		LOGGER.info("Parameter Code:[{}], Locale:[{}], Value:[{}]", param.getParameterCode(), param.getLocale(), param.getParameterValue());
        	}
        });
	}
}
