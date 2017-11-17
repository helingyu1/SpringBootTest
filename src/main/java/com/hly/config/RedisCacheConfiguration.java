package com.hly.config;

import com.sun.istack.internal.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @author HHH
 * @date 2017/11/17
 */

@Configuration
public class RedisCacheConfiguration {

    @Bean(name = "jedis.pool")
    @Autowired
    public JedisPool jedisPool(@Qualifier("jedis.pool.config")JedisPoolConfig config,
                               @Value("${spring.jedis.pool.host}") String host,
                               @Value("${spring.jedis.pool.port}") int port){
        return new JedisPool(config, host, port);
    }

    @Bean(name = "jedis.pool.config")
    public JedisPoolConfig jedisPoolConfig(@Value("${spring.jedis.pool.config.maxTotal}")int maxTotal,
                                           @Value("${spring.jedis.pool.config.maxIdle}")int maxIdle,
                                           @Value("${spring.jedis.pool.config.maxWaitMillis}")int maxWaitMillis){
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(maxTotal);
        config.setMaxIdle(maxIdle);
        config.setMaxWaitMillis(maxWaitMillis);
        return config;
    }
}
