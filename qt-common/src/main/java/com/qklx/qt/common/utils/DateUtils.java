package com.qklx.qt.common.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {


    static ThreadLocal<SimpleDateFormat> local = new ThreadLocal<>();

    public static void main(String[] args) {
        long time = System.currentTimeMillis();
        Date date = parseTimeMillisToDate(time);
        System.out.println(date);
    }

    public static Date parseTimeMillisToDate(Long timeStamp) {
        return new Date(Long.parseLong(String.valueOf(timeStamp)));
    }

    public static String formateDate(Date date) {
        if (local.get() != null) {
            return local.get().format(date);
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        local.set(simpleDateFormat);
        return simpleDateFormat.format(date);
    }


}
