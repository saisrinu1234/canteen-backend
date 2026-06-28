package com.example.canteen.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
// @Configuration
// public class RedisConfig {

//     @Bean
//     public LettuceConnectionFactory redisConnectionFactory() {
//         return new LettuceConnectionFactory();
//     }

//     @Bean
//     public StringRedisTemplate stringRedisTemplate(
//             LettuceConnectionFactory factory) {

//         return new StringRedisTemplate(factory);
//     }
//     @Bean
//     public RedisCacheManager cacheManager(
//             RedisConnectionFactory factory) {

//         return RedisCacheManager.builder(factory)
//                 .build();
//     }
// }
// @Configuration
// public class RedisConfig {

//     @Value("${spring.data.redis.host}")
//     private String host;

//     @Value("${spring.data.redis.port}")
//     private int port;

//     @Bean
//     public LettuceConnectionFactory redisConnectionFactory() {

//         System.out.println("Redis Host = " + host);
//         System.out.println("Redis Port = " + port);

//         RedisStandaloneConfiguration config =
//                 new RedisStandaloneConfiguration(host, port);

//         return new LettuceConnectionFactory(config);
//     }

//     @Bean
//     public StringRedisTemplate stringRedisTemplate(
//             LettuceConnectionFactory factory) {
//         return new StringRedisTemplate(factory);
//     }

//     @Bean
//     public RedisCacheManager cacheManager(
//             RedisConnectionFactory factory) {

//         return RedisCacheManager.builder(factory)
//                 .build();
//     }
// }
@Configuration
public class RedisConfig {

    @Bean
    public StringRedisTemplate stringRedisTemplate(
            RedisConnectionFactory factory) {
        return new StringRedisTemplate(factory);
    }

    @Bean
    public RedisCacheManager cacheManager(
            RedisConnectionFactory factory) {
        return RedisCacheManager.builder(factory).build();
    }
}