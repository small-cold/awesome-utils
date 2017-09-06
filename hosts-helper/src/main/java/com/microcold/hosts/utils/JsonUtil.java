package com.microcold.hosts.utils;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;

/*
 * Created by MicroCold on 2017/9/4.
 */
public class JsonUtil {
    private static final Logger LOGGER = Logger.getLogger(JsonUtil.class);
    private static ObjectMapper objectMapper = new ObjectMapper();

    public static String toJosn(Object object){
        try {
            return objectMapper.writeValueAsString(object);
        } catch (IOException e) {
            LOGGER.warn("json 转换错误" + e.getMessage());
        }
        return "";
    }
}
