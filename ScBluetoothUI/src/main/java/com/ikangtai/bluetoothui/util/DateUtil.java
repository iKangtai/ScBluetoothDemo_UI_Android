package com.ikangtai.bluetoothui.util;

import android.text.TextUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Time format Util
 *
 * @author xiongyl 2021/1/26 22:59
 */
public class DateUtil {
    /**
     * Get month, day, hour and minute
     *
     * @param seconds
     * @return
     */
    public static String getDateFormatYMDHM(long seconds) {
        Date date = new Date(seconds * 1000);
        SimpleDateFormat simpleDateFormat;
        if (TextUtils.equals(Locale.getDefault().getLanguage(), "zh")) {
            simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm");
        } else {
            simpleDateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm");
        }
        return simpleDateFormat.format(date);
    }

    /**
     * MM DD
     *
     * @param seconds
     * @return
     */
    public static String getDateFormatMD(long seconds) {
        Date date = new Date(seconds * 1000);
        SimpleDateFormat simpleDateFormat;
        if (TextUtils.equals(Locale.getDefault().getLanguage(), "zh")) {
            simpleDateFormat = new SimpleDateFormat("MM月dd日");
        } else {
            simpleDateFormat = new SimpleDateFormat("MMM dd");
        }
        return simpleDateFormat.format(date);
    }

    /**
     * Get the timestamp corresponding to the date str
     *
     * @param timeStr
     * @return
     */
    public static long getYMDHMDate(String timeStr) {
        SimpleDateFormat simpleDateFormat;
        if (TextUtils.equals(Locale.getDefault().getLanguage(), "zh")) {
            simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm");
        } else {
            simpleDateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm");
        }
        try {
            return simpleDateFormat.parse(timeStr).getTime() / 1000;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static String getDateTimeStr2bit(long seconds) {
        Date date = new Date(seconds * 1000);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return simpleDateFormat.format(date);
    }

    public static long getStringToDate(String time) {

        SimpleDateFormat sf = null;

        if (!time.contains(":")) {
            time = time + " 12:00:00";
        }
        if (time.contains("-")) {
            sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        } else if (time.contains(".")) {
            sf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        }

        Date date = new Date();
        try {
            date = sf.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date.getTime() / 1000;
    }
}
