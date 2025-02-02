package com.tktkcompany.kakoRaceKeiba;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        // 通知のタイトルとメッセージを取得
        String title = remoteMessage.getNotification() != null ? remoteMessage.getNotification().getTitle() : "デフォルトのタイトル";
        String message = remoteMessage.getNotification() != null ? remoteMessage.getNotification().getBody() : "デフォルトのメッセージ";

        sendNotification(title, message);
    }

    private void sendNotification(String title, String message) {
        // 通知チャンネルの設定 (Android 8.0以上)
        String channelId = "default_channel_id";
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Default Channel", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        // 通知クリック時のIntent
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        // 通知の作成
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.uma) // アイコンを適切に設定
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        // 通知を表示
        notificationManager.notify(0, notificationBuilder.build());
    }
}
