package com.hly.elasticsearch;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 构建为elasticsearch方便使用的jsonBuilder对象
 * Created by YuQing on 2017/11/17.
 */

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ESearchTypeColumn {
    /**
     * 字段类型
     *
     * @return
     */
    String type() default  "String";

    /**
     * 是否分词
     *
     * @return
     */
    boolean analyze() default false;
}
