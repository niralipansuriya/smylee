package smylee.app.firbase

import android.app.*
import android.app.ActivityManager.RunningAppProcessInfo
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import smylee.app.R
import smylee.app.URFeedApplication
import smylee.app.home.HomeActivity
import smylee.app.utils.Constants
import smylee.app.utils.Logger
import smylee.app.utils.SharedPreferenceUtils


class MyFirebaseMessagingService : FirebaseMessagingService() {

//    private val TAG = "MyFirebaseMsgService"

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

//        val dataJSON = JSONObject(remoteMessage.data as Map<*, *>).toString()
        remoteMessage.notification?.let {
            Log.d("Notification", "Message Notification Body: ${it}")
            Log.d("Notification body ", "Message Notification Body: ${it.body}")
            Log.d("Notification title ", "Message Notification Title: ${it.title}")
        }
        /*  Logger.print("remoteMessage======${remoteMessage.data.toString()}")

          Logger.print("is app is in forground===============${isAppOnForeground(this)}")
  */
        if (!isAppOnForeground(this)) {
            if (remoteMessage.data.isNotEmpty()) {

                val bundle = Bundle()
                bundle.putBoolean("isforPushNotification", true)
                Logger.print("post id remoteMessage.data=======${remoteMessage.data["post_id"]}")
                bundle.putString("post_id", remoteMessage.data["post_id"])

                val intent = Intent("notificationBroadCast")
                intent.putExtras(bundle)
                LocalBroadcastManager.getInstance(URFeedApplication.context!!).sendBroadcast(intent)

                var title = ""
                var body = ""
                remoteMessage.data.let {
                    Logger.print("notification_title=======${it["notification_title"]}")
                    title = it["notification_title"]!!
                    body = it["notify_body"]!!
                }
                showNotification(
                    URFeedApplication.context!!,
                    remoteMessage.data.toString(),
                    remoteMessage.data["post_id"]!!,
                    title,
                    body,
                    bundle
                )
            } else {
                Logger.print("data is empty ==============")
            }
        }


    }

    private fun isAppOnForeground(context: Context): Boolean {
        val activityManager =
            context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val appProcesses =
            activityManager.runningAppProcesses ?: return false
        val packageName = context.packageName
        for (appProcess in appProcesses) {
            if (appProcess.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName == packageName) {
                return true
            }
        }
        return false
    }

    override fun handleIntent(p0: Intent?) {
        super.handleIntent(p0)
        if (p0 != null) {
            for (key in p0.extras!!.keySet()) {
                val value = p0.extras!![key]
                Log.d("firebase handleIntent: ", "Key: $key Value: $value")

            }

        }
    }

    private fun showNotification(
        context: Context,
        msg: String?,
        postID: String,
        title: String,
        body: String,
        bundle: Bundle
    ) {
        Logger.print("title===============$title")
        Logger.print("body===============$body")
        Logger.print("postID===============$postID")
        val intent: Intent?
        intent = Intent(context, HomeActivity::class.java)
        intent.putExtra("screenName", "")
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        intent.putExtras(bundle)

        val chatIntent =
            PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        var message = title
        if (message == null || message == "") {
            message = "You have received a new message"
        }

        Logger.print("msg!!!!!!!!!!!!!!!!$msg")
        val builder: Notification.Builder?
        builder = Notification.Builder(applicationContext)
            //.setContentTitle(context.getString(R.string.app_name))
            .setContentTitle(title)

            //   .setContentText(message)
            .setContentText(body)
            // .setStyle(Notification.BigTextStyle().bigText(message))
            .setLargeIcon(
                BitmapFactory.decodeResource(
                    applicationContext.resources,
                    R.mipmap.ic_launcher_final
                )
            )
            .setWhen(System.currentTimeMillis())
            .setContentIntent(chatIntent)
            .setDefaults(Notification.DEFAULT_ALL)
            .setAutoCancel(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setColor(ContextCompat.getColor(applicationContext, R.color.colorPrimary))
            builder.setSmallIcon(R.mipmap.ic_launcher_final)
        } else {
            builder.setSmallIcon(R.mipmap.ic_launcher_final)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = applicationContext.getString(R.string.app_name)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(Constants.APP_NAME, name, importance)
            notificationManager.createNotificationChannel(channel)
            builder.setChannelId(Constants.APP_NAME)
        }
        val notification = builder.build()
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val oldToken = SharedPreferenceUtils.getInstance(this).getStringValue(Constants.FIREBASE_TOKEN, "")!!
        SharedPreferenceUtils.getInstance(this@MyFirebaseMessagingService).setValue(Constants.FIREBASE_TOKEN, token)
        if (oldToken != token) {
            SharedPreferenceUtils.getInstance(this@MyFirebaseMessagingService)
                .setValue(Constants.FIREBASE_TOKEN_CHANGED, true)
        }
        val intent = Intent("onNewToken")
        LocalBroadcastManager.getInstance(URFeedApplication.context!!).sendBroadcast(intent)
    }
}