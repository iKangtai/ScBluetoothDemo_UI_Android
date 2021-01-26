package com.example.bledemo.util;

import android.text.TextUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * desc
 *
 * @author xiongyl 2021/1/26 22:59
 */
public class DateUtil {
    /**
     * 获取月日 时分
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
     * 获取月日 时分对应时间戳
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
}
