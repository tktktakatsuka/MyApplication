package com.tktkcompany.kakoRaceKeiba.notification;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.tktkcompany.kakoRaceKeiba.MainActivity;
import com.tktkcompany.kakoRaceKeiba.R;

/**
 * 通知チャンネルの作成と通知の表示をまとめたクラス。
 * チャンネルIDはここで一元管理する（AndroidManifestのFCM既定チャンネルとも一致させる）。
 */
public class NotificationHelper {

    public static final String DEFAULT_CHANNEL_ID = "default_channel";
    public static final String FAVORITE_CHANNEL_ID = "favorite_horse_channel";
    public static final String ENTRY_CHANNEL_ID = "horse_entry_channel";

    public static void createChannels(Context context) {
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager == null) return;

        NotificationChannel defaultChannel = new NotificationChannel(
                DEFAULT_CHANNEL_ID, "お知らせ", NotificationManager.IMPORTANCE_DEFAULT);
        defaultChannel.setDescription("アプリからのお知らせ");
        manager.createNotificationChannel(defaultChannel);

        NotificationChannel favoriteChannel = new NotificationChannel(
                FAVORITE_CHANNEL_ID, "お気に入り馬の結果", NotificationManager.IMPORTANCE_DEFAULT);
        favoriteChannel.setDescription("お気に入り登録した馬のレース結果");
        manager.createNotificationChannel(favoriteChannel);

        NotificationChannel entryChannel = new NotificationChannel(
                ENTRY_CHANNEL_ID, "お気に入り馬・メモした馬の出走予定", NotificationManager.IMPORTANCE_DEFAULT);
        entryChannel.setDescription("出馬表に載ったときにお知らせします");
        manager.createNotificationChannel(entryChannel);
    }

    public static boolean canPostNotifications(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        return NotificationManagerCompat.from(context).areNotificationsEnabled();
    }

    /**
     * 通知を表示する。notificationIdを変えれば複数の通知が上書きされずに並ぶ。
     */
    public static void show(Context context, String channelId, int notificationId, String title, String message) {
        if (!canPostNotifications(context)) return;
        createChannels(context);

        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, notificationId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setSmallIcon(R.drawable.uma)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        try {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build());
        } catch (SecurityException e) {
            // 通知権限が直前に取り消された場合
        }
    }
}
