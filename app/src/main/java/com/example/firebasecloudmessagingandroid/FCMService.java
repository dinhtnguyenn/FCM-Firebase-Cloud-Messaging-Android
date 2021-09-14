package com.example.firebasecloudmessagingandroid;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import com.google.firebase.messaging.RemoteMessage;

import java.util.Date;
import java.util.Map;

import androidx.core.app.NotificationCompat;

public class FCMService extends com.google.firebase.messaging.FirebaseMessagingService {
    private static final String TAG = "MyFirebaseService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // Kiểm tra xem message có chứa notification payload không.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            showNotification(remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody());
        }

        // Kiểm tra xem message có chứa data payload không.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            Map<String, String> dataPayload = remoteMessage.getData();
            String title = dataPayload.get("title");
            String body = dataPayload.get("body");
            showNotification(title, body);

        }
    }

    /**
     * Token có thể thay đổi khi :
     * - App xóa Instance ID .
     * - App được khôi phục trên một thiết bị mới.
     * - Người dùng gỡ cài đặt hoặc cài đặt lại app.
     * - Người dúng xóa app data.
     */
    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);

        sendRegistrationToServer(token);
    }

    private void sendRegistrationToServer(String token) {
        //lưu lại token devices khi token được refresh
    }

    private void showNotification(String title, String body) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        String channelId = getString(R.string.project_id);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.logo) //set icon cho thông báo
                        .setContentTitle(title) //tiêu đề thông báo
                        .setContentText(body) //nội dung thông báo
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri) //âm thanh khi nhận thông báo
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        /** từ Android 8.0 trở lên, để hiển thị thông báo, bắt buộc cần phải có Notification Channel */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Channel title",
                    NotificationManager.IMPORTANCE_DEFAULT);

            notificationManager.createNotificationChannel(channel);
        }

        /**
         * mỗi thông báo được nhận trên devices đều có 1 id riêng
         * sử dụng (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE) để tạo id ngẫu nhiên cho từng thông báo được nhận
         * */
        notificationManager.notify((int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE), notificationBuilder.build());
    }
}