package com.leafree.app.network

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.leafree.app.MainActivity
import com.leafree.app.R
import com.leafree.app.util.SharedPreferencesHelper

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d("FCM TEST", "알림 받기 성공")

        remoteMessage.notification?.let {
            val title = it.title
            val body = it.body
            val message = body.orEmpty()

            saveNotification(message) // 💾 알림 저장
            sendNotification(title, body) // 🔔 시스템 알림 표시
            broadcastNotification(message) // 🔁 앱 포그라운드면 UI 갱신
        }
    }

    private fun sendNotification(title: String?, body: String?) {
        Log.d("FCM TEST", "화면에 표시하기 직전")

        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = "Notification"
        val defaultSoundUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.leafree_icon)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            channelId,
            "Leafree 알림 채널",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            enableLights(true)
            enableVibration(true)
            setSound(
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                null
            )
        }
        notificationManager.createNotificationChannel(channel)
        notificationManager.notify(0, notificationBuilder.build())
        Log.d("FCM TEST", "화면에 표시하기까지 성공")

    }

    private fun saveNotification(message: String) {
        val prefs = SharedPreferencesHelper(applicationContext)
        val currentList = prefs.getNotificationList()
        currentList.add(0, message) // 최신순
        prefs.saveNotificationList(currentList)
    }

    private fun broadcastNotification(message: String) {
        val intent = Intent("com.leafree.NEW_NOTIFICATION").apply {
            putExtra("message", message)
            setPackage(packageName)
        }
        sendBroadcast(intent)
    }

}