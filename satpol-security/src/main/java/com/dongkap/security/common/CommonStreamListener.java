package com.dongkap.security.common;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.Subscription;

public abstract class CommonStreamListener<T> implements StreamListener<String, ObjectRecord<String, T>>, InitializingBean, DisposableBean {

	protected Logger LOGGER = LoggerFactory.getLogger(this.getClass());
	
    @Autowired
    protected RedisConnectionFactory redisConnectionFactory;

    protected StreamMessageListenerContainer<String, ObjectRecord<String, T>> listenerContainer;
    protected Subscription subscription;
	protected String appName;
	protected String groupId;
	protected String streamKey;
	private Class<T> targetTypeClass;
	
	public CommonStreamListener(String appName, String groupId, String streamKey, Class<T> targetTypeClass) {
		this.appName = appName;
		this.groupId = groupId;
		this.streamKey = streamKey;
		this.targetTypeClass = targetTypeClass;
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
    	StreamMessageListenerContainer.StreamMessageListenerContainerOptions<String, ObjectRecord<String, T>> options = StreamMessageListenerContainer
				.StreamMessageListenerContainerOptions.builder()
				.pollTimeout(Duration.ofSeconds(1))
				.targetType(this.targetTypeClass)
				.build();
		this.listenerContainer = StreamMessageListenerContainer
				.create(this.redisConnectionFactory, options);
		try {
			this.redisConnectionFactory.getConnection()
			.xGroupCreate(this.streamKey.getBytes(), this.groupId, ReadOffset.from("0-0"), true);
		} catch (RedisSystemException exception) {
			LOGGER.warn(exception.getCause().getMessage());
		}
		this.subscription = listenerContainer.receive(Consumer.from(this.groupId, this.appName), 
				StreamOffset.create(this.streamKey, ReadOffset.lastConsumed()), this);
        this.subscription.await(Duration.ofSeconds(2));
		this.listenerContainer.start();
    }
}
