package com.qklx.qt.common.utils;

import java.util.Date;

public class DateUtils {

    public static void main(String[] args) {
        long time = System.currentTimeMillis();
        Date date = parseTimeMillisToDate(time);
        System.out.println(date);
    }

    public static Date parseTimeMillisToDate(Long timeStamp) {
        return new Date(Long.parseLong(String.valueOf(timeStamp)));
    }


}
