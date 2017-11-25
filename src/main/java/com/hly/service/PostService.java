package com.hly.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.hly.elasticsearch.EsClient;
import com.hly.entity.Post;
import com.hly.redis.RedisClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.*;

/**
 * @author HHH
 * @date 2017/11/17
 */

@Service
public class PostService {

    private static final Logger LOGGER = LogManager.getLogger();

    private static ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat("test-pool-%d").build();

    private static ExecutorService threadPool = new ThreadPoolExecutor(10,10,60, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>()
    ,namedThreadFactory, new ThreadPoolExecutor.AbortPolicy());


    // redis 客户端
    @Autowired
    private RedisClient redisClient;

    // es 客户端
    @Autowired
    private EsClient esClient;

    public String addPost(Post post){

        try {

            // todo 根据content 生成摘要
            String abstracts = "test";
            post.setAbstracts(abstracts);

            // todo 生成文章唯一id
            long id = redisClient.getPostId();
            LOGGER.info("生成的id是:" + id);
            post.setId(id);

            HandleEsTask esRunnable = new HandleEsTask(post);

            HandleRedisTask redisRunnable = new HandleRedisTask(post);


            // 异步处理es入库事件
            threadPool.execute(esRunnable);


            // 异步处理redis入库事件
            threadPool.execute(redisRunnable);

        }catch(Exception e){
            LOGGER.error("异常：", e);
            return "异常";
        }

        return "ok";
    }


    private class HandleRedisTask implements Runnable {

        private Post post;

        public HandleRedisTask(Post post) {
            this.post = post;
        }

        // 使数据如redis库
        @Override
        public void run() {
            try {
                redisClient.addPost(post);
                LOGGER.info("-----------插入redis已完成-------------");
            } catch(Exception e){
                LOGGER.error("入redis异常", e);
            }
        }
    }

    private class HandleEsTask implements Runnable {

        private Post post;

        public HandleEsTask(Post post){
            this.post = post;
        }


        // 数据入es
        @Override
        public void run() {

            String ret = esClient.saveDoc("iblog","post", String.valueOf(post.getId()), post);
            LOGGER.info("-----------插入es已完成-------------");
        }
    }

}
