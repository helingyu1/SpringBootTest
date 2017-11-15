package com.hly.config;

import org.apache.log4j.Logger;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author HHH
 * @date 2017/11/15
 */
@Configuration
public class ElasticsearchConfiguration implements FactoryBean<TransportClient>, InitializingBean, DisposableBean {

    private static final Logger logger = Logger.getLogger(ElasticsearchConfiguration.class);


    @Value("${spring.data.elasticsearch.cluster-nodes}")
    private String clusterNodes;

    @Value("${spring.data.elasticsearch.cluster-name}")
    private String clusterName;

    private TransportClient client;

    @Override
    public void destroy() throws Exception {
        try{
            logger.info("Closing elasticsearch client");
            if(client != null) {
                client.close();
            }
        } catch (final Exception e){
            logger.error("Error closing ElasticSearch client: ", e);
        }
    }

    @Override
    public TransportClient getObject() throws Exception {
        return client;
    }

    @Override
    public Class<?> getObjectType() {
        return TransportClient.class;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        logger.info("------------------------------------------------ok");
        buildClient();
    }

    protected void buildClient() {
        try{
            PreBuiltTransportClient preBuiltTransportClient = new PreBuiltTransportClient(settings());
            if(!"".equals(clusterNodes)){
                for(String nodes : clusterNodes.split(",")){
                    String InetSocket[] = nodes.split(":");
                    String address = InetSocket[0];
                    Integer port = Integer.valueOf(InetSocket[1]);
                    preBuiltTransportClient.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(address), port));
                }
                client = preBuiltTransportClient;
                logger.info("------------------client:" + client);
            }
        } catch(UnknownHostException e){
            logger.error(e.getMessage());
        }
    }

    private Settings settings(){
        logger.info("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%" + clusterName);
        Settings settings = Settings.builder().put("cluster.name", clusterName)
                .put("client.transport.sniff", true)
                .build();
        client = new PreBuiltTransportClient(settings);
        return settings;
    }
}
