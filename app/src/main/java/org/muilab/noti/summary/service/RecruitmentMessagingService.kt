package org.muilab.noti.summary.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.muilab.noti.summary.R
import org.muilab.noti.summary.util.TAG

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class RecruitmentMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "From: ${remoteMessage.from}")
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
            it.body?.let { body -> sendNotification(body) }
        }
    }

    private fun sendNotification(messageBody: String) {

        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://forms.gle/5pY6BBqpsSfZQ2LJA"))
        val pendingIntent = PendingIntent.getActivity(this, 0, browserIntent, PendingIntent.FLAG_IMMUTABLE)

        val channelId = "Notify"
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.quotation)
            .setContentTitle(getString(R.string.recruitment))
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(channelId, "Notify", importance)
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)

        val sharedPref = this.getSharedPreferences("user", Context.MODE_PRIVATE)
        val country = sharedPref.getString("country", "Unknown")
        val countryCode = country!!.substring(5, 7)
        if (countryCode == "TW") {
            notificationManager.notify(0, notificationBuilder.build())
        }

    }

}