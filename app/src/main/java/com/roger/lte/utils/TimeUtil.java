package com.roger.lte.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Administrator on 2017/11/11.
 */

public class TimeUtil {

    private static DateFormat defaultDateTimeFormat =new SimpleDateFormat("yyyy MM dd HH mm ss");

    /**
     * 将时间格式转化为String类型
     * @param date
     * @param dateFormat
     * @return
     */
    public static String parser2DateTime(Date date, DateFormat dateFormat){
        return dateFormat.format(date);
    }

    /**
     * 将时间格式转化为String类型
     * @param date
     * @return
     */
    public static String parser2DateTime(Date date){
        return parser2DateTime(date, defaultDateTimeFormat);
    }
}
