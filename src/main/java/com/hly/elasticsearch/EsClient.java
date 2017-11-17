package com.hly.elasticsearch;

import org.apache.log4j.Logger;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by YuQing on 2017/11/17.
 */
@Component
public class EsClient {

    private static final Logger logger = Logger.getLogger(EsClient.class);

    @Autowired
    TransportClient client;

    /**
     * 创建搜索引擎文档
     * @param index 索引名称
     * @param type  索引类型
     * @param id  索引id
     * @param doc doc对象
     * @return
     */
    public String saveDoc(String index, String type, String id, Object doc) {
        IndexResponse response = client.prepareIndex(index, type, id).setSource(getXContentBuilderKeyValue(doc)).get();
        return response.getId();
    }

    public static XContentBuilder getXContentBuilderKeyValue(Object o){
        try{
            XContentBuilder builder = XContentFactory.jsonBuilder().startObject();
            List<Field> fieldList = new ArrayList<Field>();

            Class tempClass = o.getClass();
            while(tempClass != null){
                fieldList.addAll(Arrays.asList(tempClass.getDeclaredFields()));
                tempClass = tempClass.getSuperclass();

            }
            for(Field field : fieldList) {
                if(field.isAnnotationPresent(ESearchTypeColumn.class)){
                    PropertyDescriptor descriptor = new PropertyDescriptor(field.getName(), o.getClass());
                    Object value = descriptor.getReadMethod().invoke(o);
                    if(value != null){
                        builder.field(field.getName(), value.toString());
                    }
                }
            }
            builder.endObject();
            logger.info(builder.string());
            return builder;
        } catch (Exception e){
            logger.error("获取object key-value失败，{}", e);
        }
        return null;
    }
}
