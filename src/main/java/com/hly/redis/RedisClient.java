package com.hly.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * @author HHH
 * @date 2017/11/17
 */

@Component
public class RedisClient {

    @Autowired
    private JedisPool jedisPool;

    public void set(String key, String value) throws Exception {
        Jedis jedis = null;
        try{
            jedis = jedisPool.getResource();
            jedis.set(key, value);
        } finally {
            jedis.close();
        }
    }

    public String get(String key) throws Exception {
        Jedis jedis = null;
        try{
            jedis = jedisPool.getResource();
            return jedis.get(key);
        }finally {
            jedis.close();
        }
    }
}
