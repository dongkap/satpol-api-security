package com.dongkap.security.service.listener;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.Subscription;
import org.springframework.stereotype.Service;

import com.dongkap.common.utils.StreamKeyStatic;
import com.dongkap.dto.common.CommonStreamMessageDto;
import com.dongkap.dto.master.ParameterI18nDto;

import lombok.SneakyThrows;

@Service
public class ParameterListenerService implements StreamListener<String, ObjectRecord<String, CommonStreamMessageDto>>, InitializingBean, DisposableBean {

	protected Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

	@Value("${spring.application.name}")
	private String appName;

    private StreamMessageListenerContainer<String, ObjectRecord<String, CommonStreamMessageDto>> listenerContainer;
    private Subscription subscription;

	@Override
    @SneakyThrows
	public void onMessage(ObjectRecord<String, CommonStreamMessageDto> message) {
        String stream = message.getStream();
        RecordId id = message.getId();
        CommonStreamMessageDto value = message.getValue();
		LOGGER.info("A message was received stream:[{}],id:[{}],value:[{}]", stream, id, value);
        value.getDatas().forEach(data->{
        	if(data instanceof ParameterI18nDto) {
        		ParameterI18nDto param = (ParameterI18nDto)data;
        		LOGGER.info("Parameter Code:[{}], Locale:[{}], Value:[{}]", param.getParameterCode(), param.getLocale(), param.getParameterValue());
        	}
        });
	}

    @Override
    public void destroy() throws Exception {
        if (this.subscription != null) {
        	this.subscription.cancel();
        }

        if (this.listenerContainer != null) {
        	this.listenerContainer.stop();
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
    	StreamMessageListenerContainer.StreamMessageListenerContainerOptions<String, ObjectRecord<String, CommonStreamMessageDto>> options = StreamMessageListenerContainer
				.StreamMessageListenerContainerOptions.builder()
				.pollTimeout(Duration.ofSeconds(1))
				.targetType(CommonStreamMessageDto.class)
				.build();
		this.listenerContainer = StreamMessageListenerContainer
				.create(this.redisTemplate.getConnectionFactory(), options);
		try {
			this.redisTemplate.getConnectionFactory().getConnection()
			.xGroupCreate(StreamKeyStatic.PARAMETER.getBytes(), StreamKeyStatic.PARAMETER, ReadOffset.from("0-0"), true);
		} catch (RedisSystemException exception) {
			LOGGER.warn(exception.getCause().getMessage());
		}
		this.subscription = listenerContainer.receive(Consumer.from(StreamKeyStatic.PARAMETER, appName), 
				StreamOffset.create(StreamKeyStatic.PARAMETER, ReadOffset.lastConsumed()), this);
        this.subscription.await(Duration.ofSeconds(2));
		this.listenerContainer.start();
    }
}
