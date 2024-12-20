package com.assignment.demo.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Value("${spring.redis.host}")
    private String cacheHostName;

    @Value("${spring.redis.port}")
    private int redisPort;

    @Value("${spring.redis.timeout-in-secs:0}")
    private int timeOut;

    @Value("${spring.redis.password}")
    private String cachePassword;


    @Bean
    public ReactiveRedisConnectionFactory reactiveRedisConnectionFactory() {
        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
        redisConfig.setHostName(cacheHostName);
        redisConfig.setPort(redisPort);
        if (cachePassword != null && !cachePassword.isEmpty()) {
            redisConfig.setPassword(RedisPassword.of(cachePassword));
        }
        return new LettuceConnectionFactory(redisConfig);
    }

    /**
     * ReactiveRedisTemplate for Redis operations
     */
    @Bean
    @Primary
    public ReactiveRedisTemplate<String, String> reactiveRedisTemplate(
            ReactiveRedisConnectionFactory reactiveRedisConnectionFactory) {
        StringRedisSerializer keySerializer = new StringRedisSerializer();
        StringRedisSerializer valueSerializer = new StringRedisSerializer();
        return new ReactiveRedisTemplate<>(reactiveRedisConnectionFactory,
                RedisSerializationContext.<String, String>newSerializationContext(keySerializer)
                        .value(valueSerializer)
                        .build());
    }

    /**
     * RedisTemplate for Redis operations
     */
    @Bean
    public RedisTemplate<String, String> redisTemplate() {
        RedisTemplate<String, String> template = new RedisTemplate<>();

        // Use LettuceConnectionFactory to connect to Redis
        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
        redisConfig.setHostName(cacheHostName);
        redisConfig.setPort(redisPort);
        if (cachePassword != null && !cachePassword.isEmpty()) {
            redisConfig.setPassword(RedisPassword.of(cachePassword));
        }

        LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory(redisConfig);
        connectionFactory.afterPropertiesSet();

        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());

        return template;
    }

}
