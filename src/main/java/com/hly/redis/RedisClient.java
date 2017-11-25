package com.hly.redis;

import com.hly.entity.Post;
import com.hly.util.MapTools;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;

import java.util.HashMap;
import java.util.Map;

/**
 * @author HHH
 * @date 2017/11/17
 */

@Component
public class RedisClient {

    private static final Logger LOGGER = LogManager.getLogger();

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

    /**
     * 获取post 唯一id
     * @return
     * @throws Exception
     */
    public long getPostId() throws Exception{
        Jedis jedis = null;
        try{
            jedis = jedisPool.getResource();
            return jedis.incr("post:id");
        } finally {
            jedis.close();
        }
    }

    public int addPost(Post post) throws Exception{
        Jedis jedis = null;

        try{

            jedis = jedisPool.getResource();
            Transaction trans = jedis.multi();

            // post 转 map
            Map<String, String> map = MapTools.objectToMap(post);

            String key = "post:" + post.getId();

            // 存储post详细信息 hashmap
            trans.hmset(key, map);

            trans.exec();
        }finally {
            jedis.close();
        }
        return 1;
    }
}
