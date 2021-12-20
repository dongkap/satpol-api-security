package com.dongkap.security.configuration;

import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;

import com.dongkap.dto.common.CommonStreamMessageDto;

@Configuration
public class RedisConfiguration {

	protected Logger LOGGER = LoggerFactory.getLogger(this.getClass());
	
    @Autowired
    private RedisConnectionFactory redisConnectionFactory;
    
    @Bean
    public TokenStore tokenStore() {
        // return new JdbcOauth2TokenStore(dataSource);
        return new RedisTokenStore(redisConnectionFactory);
    }
	
	@Bean
	@ConditionalOnMissingBean(name = "redisTemplate")
	public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) throws UnknownHostException {
		RedisTemplate<Object, Object> template = new RedisTemplate<>();
		template.setConnectionFactory(redisConnectionFactory);
		return template;
	}

	@Bean
	@ConditionalOnMissingBean(name = "reactiveRedisTemplate")
	public ReactiveRedisOperations<String, CommonStreamMessageDto> reactiveRedisTemplate(LettuceConnectionFactory lettuceConnectionFactory){
        RedisSerializer<CommonStreamMessageDto> valueSerializer = new Jackson2JsonRedisSerializer<>(CommonStreamMessageDto.class);
        RedisSerializationContext<String, CommonStreamMessageDto> serializationContext = RedisSerializationContext.<String, CommonStreamMessageDto>newSerializationContext(RedisSerializer.string())
                .value(valueSerializer)
                .build();
        return new ReactiveRedisTemplate<String, CommonStreamMessageDto>(lettuceConnectionFactory, serializationContext);
    }
	
}
