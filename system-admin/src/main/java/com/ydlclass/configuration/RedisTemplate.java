package com.ydlclass.configuration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisException;

import javax.annotation.Resource;

@Component
@Slf4j
public class RedisTemplate {
    @Resource
    private JedisPool jedisPool;

    @Resource
    private CustomObjectMapper objectMapper;

    //保存字符串类型数据
    public String set(String key, String value, Long expire) {
        Jedis resource = jedisPool.getResource();
        String returnValue = null;
        try {
            if (expire > 0) {
                returnValue = resource.setex(key, expire, value);
            } else {
                returnValue = resource.set(key, value);
            }
        } catch (JedisException jedisException) {
            log.error("redis execution error!", jedisException);
            jedisPool.returnBrokenResource(resource);
        } finally {
            jedisPool.returnResource(resource);
        }
        return returnValue;
    }

    //获取字符串类型数据
    public String get(String key) {
        Jedis resource = jedisPool.getResource();
        String returnValue = null;
        try {
            returnValue = resource.get(key);
        } catch (JedisException jedisException) {
            log.error("redis execution error!", jedisException);
            jedisPool.returnBrokenResource(resource);
        } finally {
            jedisPool.returnResource(resource);
        }
        return returnValue;
    }

    //存储序列化数据
    public String setObject(String key, Object value, Long expire) {
        String returnValue = null;
        try {
            String jsonValue = objectMapper.writeValueAsString(value);
            returnValue = set(key, jsonValue, expire);
        } catch (JsonProcessingException exception) {
            log.error("redis execution error!", exception);
        }
        return returnValue;
    }

    //获取序列化数据
    public <T> T getObject(String key, TypeReference<T> typeReference) {
        Jedis resource = jedisPool.getResource();
        T returnValue = null;
        try {
            String valueString = resource.get(key);
            if (valueString == null) {
                return null;
            }
            returnValue = objectMapper.readValue(valueString, typeReference);
        } catch (JedisException | JsonProcessingException e) {
            log.error("redis execution error!", e);
            jedisPool.returnBrokenResource(resource);
        } finally {
            jedisPool.returnResource(resource);
        }
        return returnValue;
    }

    public long remove(String ...key) {
        Jedis resource = jedisPool.getResource();
        long del = 0L;
        try {
            del = resource.del(key);
        } catch (JedisException e) {
            log.error("redis execution error! {}", e);
            jedisPool.returnBrokenResource(resource);
        } finally {
            jedisPool.returnResource(resource);
        }
        return del;
    }

    public long expire(String key, long seconds) {
        Jedis resource = jedisPool.getResource();
        long result = 0L;
        try {
            result = resource.expire(key, seconds);
        } catch (JedisException e) {
            log.error("redis execution error! {}", e);
            jedisPool.returnBrokenResource(resource);
        } finally {
            jedisPool.returnBrokenResource(resource);
        }
        return result;
    }
}
