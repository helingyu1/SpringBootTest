package com.hly.controller;

import com.alibaba.fastjson.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.transport.TransportClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * @author HHH
 * @date 2017/11/14
 */
@RestController
@Import(value = {RestTemplate.class})
public class TestController {

    private static final Logger LOGGER = LogManager.getLogger();

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    TransportClient client;


    @RequestMapping("/")
    @ResponseBody
    String home(){
        String url = "http://47.94.106.157:9200";
        JSONObject json = restTemplate.getForEntity(url, JSONObject.class).getBody();

        return json.toJSONString();
    }

    @RequestMapping("/test")
    public String test() {

        // 批量创建索引
        BulkRequestBuilder bulkRequest = client.prepareBulk();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("name", "Jack");
        IndexRequest request = client.prepareIndex("blog", "post","1").setSource(map).request();
        bulkRequest.add(request);
        BulkResponse bulkResponse = bulkRequest.execute().actionGet();
        if(bulkResponse.hasFailures()){
            LOGGER.error("批量创建索引错误...");
        }
        client.close();
        LOGGER.info("批量创建索引成功");
        LOGGER.info(bulkResponse.toString());
        return "ok";
    }
}
