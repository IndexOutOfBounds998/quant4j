package com.quant.common.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {

    /**
     * @author yang
     * @desc 使用ThreadLocal保证线程安全
     * @date 2019/5/26
     */
    private static final ThreadLocal<SimpleDateFormat> local = new ThreadLocal<>();

    public static void main(String[] args) {
        long time = System.currentTimeMillis();
        Date date = parseTimeMillisToDate(time);
        System.out.println(date);
    }

    public static Date parseTimeMillisToDate(Long timeStamp) {
        return new Date(Long.parseLong(String.valueOf(timeStamp)));
    }

    public static String formateDate(Date date, String format) {
        if (local.get() != null) {
            return local.get().format(date);
        }
        if (format == null) {
            format = "yyyy-MM-dd HH:mm:ss";
        }
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        local.set(simpleDateFormat);
        return simpleDateFormat.format(date);
    }


}
