package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableCaching
public class RedisConfig {
    @Value("${redis.host}")
    private String redisHost;

    @Value("${redis.port}")
    private int redisPort;


    // tạo lettuce để quản lý kết nối tới Redis
    @Bean
    public LettuceConnectionFactory jedisConnectionFactory() {
        // Tạo một đối tượng RedisStandaloneConfiguration để cấu hình thông tin kết nối Redis độc lập (standalone)
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        redisStandaloneConfiguration.setHostName(redisHost);
        redisStandaloneConfiguration.setPort(redisPort);
        return new LettuceConnectionFactory(redisStandaloneConfiguration);
    }


    @Bean
    public RedisTemplate<Long, String> redisTemplate() {
        RedisTemplate<Long, String> redisTemplate = new RedisTemplate<>();
        // Thiết lập kết nối của RedisTemplate thông qua đối tượng LettuceConnectionFactory
        redisTemplate.setConnectionFactory(jedisConnectionFactory());
        // StringRedisSerializer sử dụng để chuyển string thành byte và ngược lại
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        // JdkSerializationRedisSerializer dùng trong trường hợp lưu trữ các đối tượng phức tạp
        redisTemplate.setHashKeySerializer(new JdkSerializationRedisSerializer());
        redisTemplate.setValueSerializer(new JdkSerializationRedisSerializer());
        // Cho phép hỗ trợ giao dịch với Redis
        redisTemplate.setEnableTransactionSupport(true);
        // Đảm bảo rằng tất cả các cài đặt đã được thiết lập đúng trước khi sử dụng RedisTemplate
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate() {
        return new StringRedisTemplate(jedisConnectionFactory());
    }
}