package com.hly.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

/**
 * Created by YuQing on 2017/11/25.
 */
public class HttpHelper {

    private static final Logger LOGGER = LogManager.getLogger();

    public static String getBodyString(ServletRequest request){

        StringBuilder sb = new StringBuilder();
        InputStream inputStream = null;
        BufferedReader reader = null;
        try{
            inputStream = request.getInputStream();
            reader = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")));

            String line = "";
            while((line = reader.readLine()) != null){
                sb.append(line);
            }
        }catch(IOException e){
            LOGGER.error("http帮助类异常", e);
        }finally {
            if(inputStream != null){
                try{
                    inputStream.close();;
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
            if(reader != null){
                try{
                    reader.close();
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }

}
