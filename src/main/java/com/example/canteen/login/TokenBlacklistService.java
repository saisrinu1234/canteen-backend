package com.example.canteen.login;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class TokenBlacklistService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    public void blacklistToken(String token, long ttlMillis) {

        System.out.println("BLACKLISTING TOKEN");
        System.out.println("TTL = " + ttlMillis);

        redisTemplate.opsForValue().set(
                token,
                "BLACKLISTED",
                Duration.ofMillis(ttlMillis));
    }

    public boolean isBlacklisted(String token) {

        return Boolean.TRUE.equals(
                redisTemplate.hasKey(token));
    }
}