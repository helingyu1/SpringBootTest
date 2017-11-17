package com.hly.service;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.hly.entity.Post;
import com.hly.redis.RedisClient;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.*;

/**
 * @author HHH
 * @date 2017/11/17
 */

@Service
public class PostService {

    private static final Logger logger = Logger.getLogger(PostService.class);

    private static ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat("test-pool-%d").build();

    private static ExecutorService threadPool = new ThreadPoolExecutor(10,10,60, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>()
    ,namedThreadFactory, new ThreadPoolExecutor.AbortPolicy());

    @Autowired
    private RedisClient redisClient;

    public String addPost(Post post){

        try {

            // todo 根据content 生成摘要
            String abstracts = "test";
            post.setAbstracts(abstracts);

            HandleEsTask esRunnable = new HandleEsTask(post);

            HandleRedisTask redisRunnable = new HandleRedisTask(post);


            // 异步处理es入库事件
            threadPool.execute(esRunnable);


            // 异步处理redis入库事件
            threadPool.execute(redisRunnable);

        }catch(Exception e){
            logger.error("异常：", e);
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
            } catch(Exception e){
                logger.error("入redis异常", e);
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

        }
    }

}
