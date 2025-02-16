package com.westsomsom.finalproject.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
    @Value("${spring.data.redis.host}")
    private String ELASTICACHE_ENDPOINT; // ElastiCache 엔드포인트 입력
    @Value("${spring.data.redis.port}")
    private int ELASTICACHE_PORT; // 기본 Redis 포트

    @Bean // TLS 활성화된 Redis 설정
    public RedisConnectionFactory redisConnectionFactory() {
        // RedisStandaloneConfiguration에 호스트와 포트 설정
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        redisStandaloneConfiguration.setHostName(ELASTICACHE_ENDPOINT);
        redisStandaloneConfiguration.setPort(ELASTICACHE_PORT);

        // LettuceClientConfiguration에 TLS 활성화
        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                .useSsl() // TLS 활성화
                .build();

        return new LettuceConnectionFactory(redisStandaloneConfiguration, clientConfig);
    }

    @Bean
    @Primary
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }
}