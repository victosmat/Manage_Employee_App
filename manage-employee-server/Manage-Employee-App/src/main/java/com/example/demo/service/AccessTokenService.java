package com.example.demo.service;

import com.example.demo.component.AccessToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class AccessTokenService {
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final String CACHE_NAME = "tokens";

    public List<AccessToken> findAll() {
        return redisTemplate.opsForHash().values(CACHE_NAME);
    }

    public String findById(Long userID) {
        return (String) redisTemplate.opsForHash().get(CACHE_NAME, userID);
//        return stringRedisTemplate.opsForValue().get(String.valueOf(userID));
    }

    public Boolean save(AccessToken accessToken) {
        try {
            redisTemplate.opsForHash().put(CACHE_NAME, accessToken.getUserID(), accessToken.getToken());
            stringRedisTemplate.opsForValue().set(String.valueOf(accessToken.getUserID()), accessToken.getToken());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Boolean delete(Long userID) {
        try {
            stringRedisTemplate.delete(String.valueOf(userID));
            redisTemplate.opsForHash().delete(CACHE_NAME, userID);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Boolean update(AccessToken accessToken) {
        try {
            stringRedisTemplate.opsForValue().set(String.valueOf(accessToken.getUserID()), accessToken.getToken());
            redisTemplate.opsForHash().put(CACHE_NAME, accessToken.getUserID(), accessToken.getToken());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
