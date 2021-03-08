package com.ikangtai.bluetoothui.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.text.TextUtils;

import com.ikangtai.bluetoothui.R;

import java.io.File;
import java.util.Random;

/**
 * Notification Util
 *
 * @author xiongyl 2021/1/21 21:11
 */
public class NotificationUtil {
    /**
     * Whether to allow notifications
     * @param context
     * @return
     */
    public static boolean isNotificationEnable(Context context) {
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        boolean areNotificationsEnabled = notificationManagerCompat.areNotificationsEnabled();
        return areNotificationsEnabled;
    }

    /**
     * Send app notification
     * @param context
     * @param playSound
     * @param title
     * @param msg
     * @param intent
     */
    public static void pushMessage(Context context, boolean playSound, String title, String msg, Intent intent) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder notification;
        if (TextUtils.isEmpty(title)) {
            title = context.getString(R.string.app_name);
        }

        String id = context.getPackageName();
        Uri sound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + id
                + File.separator + R.raw.reminder_alarm+".mp3");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build();

            NotificationChannel channel = new NotificationChannel(id, title, importance);
            channel.enableLights(true);
            channel.setLightColor(Color.parseColor("#9559a4"));
            if (playSound) {
                channel.setSound(sound, audioAttributes);
            }
            channel.setShowBadge(true);
            //channel.enableVibration(true);
            //channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});


            notificationManager.createNotificationChannel(channel);

            notification = new NotificationCompat.Builder(context, id)
                    .setAutoCancel(true)
                    .setContentText(msg)
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .setTicker(title)
                    .setSmallIcon(R.mipmap.ic_launcher, 3)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setWhen(System.currentTimeMillis())
                    .setContentTitle(title);

        } else {
            notification = new NotificationCompat.Builder(context)
                    .setAutoCancel(true)
                    .setContentText(msg)
                    .setTicker(title)
                    .setSmallIcon(R.mipmap.ic_launcher, 3)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setWhen(System.currentTimeMillis())
                    .setContentTitle(title);
            if (playSound) {
                notification.setSound(sound);
            }
        }

        if (intent == null) {
            intent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(context,
                0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        notification.setContentIntent(pendingIntent);
        notificationManager.notify(new Random().nextInt(Integer.MAX_VALUE), notification.build());
    }
}
