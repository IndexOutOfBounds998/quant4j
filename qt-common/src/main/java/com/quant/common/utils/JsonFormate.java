package com.quant.common.utils;

import java.lang.reflect.Field;
import java.util.HashMap;

public class JsonFormate {

    public static String parseJsonToString(String message) {
        message = message.replaceAll("\\\\", "");
        message = message.substring(1, message.length() - 1);
        return message;
    }

    /**
     *
     * 使用java.lang.reflect进行转换
     * @param object
     * @return map
     */
    public static HashMap<String, Object> objToHashMap(Object object){
        HashMap<String, Object> map = new HashMap<>();
        try{
            Field[] declaredFields = object.getClass().getDeclaredFields();
            for (Field field : declaredFields) {
                field.setAccessible(true);
                try {
                    map.put(field.getName(), field.get(object));
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }catch(SecurityException e){
            e.printStackTrace();
        }
        return map;
    }
}
