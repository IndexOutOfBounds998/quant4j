package com.qklx.qt.common.utils;

public class JsonFormate {

    public static String parseJsonToString(String message) {
        message = message.replaceAll("\\\\", "");
        message = message.substring(1, message.length() - 1);
        return message;
    }
}
