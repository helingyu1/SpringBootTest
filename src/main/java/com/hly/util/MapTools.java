package com.hly.util;

import org.apache.log4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author HHH
 * @date 2017/11/17
 */
public class MapTools {

    private static final Logger logger = Logger.getLogger(MapTools.class);

    public static <T extends Object> void flushParams(Map<String, String> params, T t){
        if(params == null || t == null){
            return ;
        }

        Class<?> clazz = t.getClass();
        for(; clazz != Object.class; clazz = clazz.getSuperclass()) {
            try{
                Field[] fields = clazz.getDeclaredFields();

                for(int j = 0; j < fields.length; j++){
                    String name = fields[j].getName();
                    String value = null;

                    if(logger.isDebugEnabled()) {
                        logger.debug(MapTools.class + "attribute name : " + name);
                    }

                    Method method = t.getClass().getMethod("get" + name.substring(0, 1).toUpperCase() + name.substring(1));
                    value = (String)method.invoke(t);

                    if(logger.isDebugEnabled()) {
                        logger.debug(MapTools.class + "attribute value : " + value);
                    }

                    if(value != null){
                        params.put(name, value);
                    }

                }
            }catch(Exception e){
                logger.error("pojo转map异常", e);
            }
        }
        logger.info(t.getClass().getName() + " 转成map为：" + params);
    }

    public static Map<String, String> objectToMap(Object obj){
        Map<String, String> map = new HashMap<String, String>();
        try{
            if(obj == null){return null;}
            Field[] declaredFields = obj.getClass().getDeclaredFields();
            for (Field field : declaredFields){
                field.setAccessible(true);
                if(field.get(obj) instanceof String){
                    if(field.get(obj)!=null&&!field.get(obj).equals("")){
                        map.put(field.getName(), field.get(obj).toString());
                    }
                }else if(field.get(obj) instanceof Long){
                    if(field.get(obj)!=null){
                        map.put(field.getName(), String.valueOf(field.get(obj)));
                    }
                }else if(field.get(obj) instanceof Integer){
                    if(field.get(obj)!=null){
                        map.put(field.getName(), String.valueOf(field.get(obj)));
                    }
                }else if(field.get(obj) instanceof Date){
                    if(field.get(obj)!=null){
                        map.put(field.getName(), field.get(obj).toString());
                        //map.put(field.getName(), DateFormater.DateToString((Date)field.get(obj),DateFormater.TIMEF_FORMAT));
                    }
                }else if(field.get(obj) instanceof Double){
                    if(field.get(obj)!=null){
                        map.put(field.getName(), String.valueOf(field.get(obj)));
                    }
                }else if(field.get(obj) instanceof Float){
                    if(field.get(obj)!=null){
                        map.put(field.getName(), String.valueOf(field.get(obj)));
                    }
                }else if(field.get(obj) instanceof BigInteger){
                    if(field.get(obj)!=null){
                        map.put(field.getName(), String.valueOf(field.get(obj)));
                    }
                }else if(field.get(obj) instanceof Short){
                    if(field.get(obj)!=null){
                        map.put(field.getName(), String.valueOf(field.get(obj)));
                    }
                }else if(field.get(obj) instanceof BigInteger){
                    if(field.get(obj)!=null){
                        map.put(field.getName(), String.valueOf(field.get(obj)));
                    }
                }else{
                    if(field.get(obj)!=null){
                        map.put(field.getName(), field.get(obj).toString());
                    }
                }

            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return map;
    }
}
