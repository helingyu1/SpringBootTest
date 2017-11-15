package com.hly.repository;

import org.apache.log4j.Logger;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.springframework.stereotype.Repository;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author HHH
 * @date 2017/11/15
 */
@Repository
public class TransportClientRepository
{
    private static final Logger logger = Logger.getLogger(TransportClientRepository.class);

    private TransportClient client;

    public TransportClientRepository(TransportClient client){
        super();
        this.client = client;
    }

//    public String saveDoc(String index, String type, String id, Object doc){
//        IndexResponse response = client.prepareIndex(index, type, id).setSource()
//    }
//
//    public static XContentBuilder getXContentBuilderKeyValue(Object o){
//        try{
//            XContentBuilder builder = XContentFactory.jsonBuilder().startObject();
//            List<Field> fieldList = new ArrayList<Field>();
//
//            Class tempClass = o.getClass();
//            while(tempClass != null){
//                fieldList.addAll(Arrays.asList(tempClass.getDeclaredFields()));
//                tempClass = tempClass.getSuperclass();
//            }
//            for(Field field : fieldList){
//                if(field.isAnnotationPresent(ESearchTy))
//            }
//        }
//    }

}
