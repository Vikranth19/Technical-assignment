package com.assignment.demo.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

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
    public JedisPool getJedisPool() {
        final JedisPoolConfig poolConfig = new JedisPoolConfig();
        return new JedisPool(poolConfig, cacheHostName, redisPort, timeOut, cachePassword);
    }
}
