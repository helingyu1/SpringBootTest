package com.hly.service;

import com.hly.entity.Post;
import com.hly.redis.RedisClient;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author HHH
 * @date 2017/11/17
 */

@Service
public class PostService {

    private static final Logger logger = Logger.getLogger(PostService.class);

    @Autowired
    private RedisClient redisClient;

    public String addPost(Post post){


        // todo 获取唯一post id
        long id = 1;
        try {
            redisClient.set("hahaha", "1111");
            logger.info(redisClient.get("hahaha"));
        }catch(Exception e){
            logger.error("异常：", e);
        }
        // todo 根据content 生成摘要
        String constracts = "";

        // todo 将post信息存储到redis中

        // todo 将post信息存储到elasticsearch中

        return "ok";
    }
}
