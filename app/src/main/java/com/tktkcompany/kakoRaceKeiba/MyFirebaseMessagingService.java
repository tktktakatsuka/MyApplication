package com.tktkcompany.kakoRaceKeiba;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.tktkcompany.kakoRaceKeiba.notification.NotificationHelper;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        // 通知ペイロードがないメッセージ（データのみ）は表示しない
        RemoteMessage.Notification notification = remoteMessage.getNotification();
        if (notification == null) return;

        String title = notification.getTitle() != null ? notification.getTitle() : getString(R.string.app_name);
        String message = notification.getBody() != null ? notification.getBody() : "";

        // 通知IDを毎回変えて、前の通知が上書きされないようにする
        int notificationId = (int) (System.currentTimeMillis() & 0x7FFFFFFF);
        NotificationHelper.show(this, NotificationHelper.DEFAULT_CHANNEL_ID, notificationId, title, message);
    }
}
