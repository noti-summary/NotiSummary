package edu.mui.noti.noti.service

import android.app.Notification
import android.app.Notification.*
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Build.VERSION_CODES.TIRAMISU
import android.service.notification.StatusBarNotification
import android.util.Log
import edu.mui.noti.noti.model.CurrentDrawer
import edu.mui.noti.noti.util.TAG
import java.util.*

class NotiItem(context: Context,
               sbn: StatusBarNotification?,
               private val userId: String) {
    private var appName: String? = null
    private var title: String = ""
    private var content: String = ""
    private var category: String = ""
    private var packageName: String

    private var notification: Notification? = null
    private var key: String
    private var unixTime: Long? = null
    private var postTime: String? = null
    private var group: String? = null
    private var onGoing: Boolean? = null
    private var flags: Int? = null
    private var sortKey: String

    private var notificationId: String

    private var sbnId: Int? = null

    init {

        this.title = sbn?.notification?.extras?.getCharSequence(EXTRA_TITLE).toString()

        this.content = if (sbn?.notification?.extras?.getCharSequence(EXTRA_TEXT).toString().isEmpty()) {
            sbn?.notification?.extras?.getCharSequence(EXTRA_BIG_TEXT).toString()
        } else {
            sbn?.notification?.extras?.getCharSequence(EXTRA_TEXT).toString()
        }
        this.category = sbn?.notification?.category ?: "others"
        this.packageName = sbn?.packageName.toString()

        this.notification = sbn?.notification
        this.key = sbn?.key.toString()
        this.unixTime = sbn?.postTime
        this.postTime = sbn?.postTime?.let { Date(it).toString() }
        this.group = sbn?.notification?.group
        this.flags = sbn?.notification?.flags
        this.sortKey = sbn?.notification?.sortKey.toString()

        this.notificationId = "${this.userId}_${this.unixTime}"

        this.sbnId = sbn?.id

        val pm = context.packageManager
        val applicationInfo: ApplicationInfo? =
            sbn?.packageName?.let {
                if (Build.VERSION.SDK_INT >= TIRAMISU) {
                    pm.getApplicationInfo(it, PackageManager.ApplicationInfoFlags.of(0))
                } else {
                    pm.getApplicationInfo(it, 0)
                }
            }

        this.appName = (if (applicationInfo != null) {
            pm.getApplicationLabel(applicationInfo).toString()
        } else {
            this.packageName
        })
    }

    fun logProperty() {
        Log.d(TAG, "############# NotiItem Property Start #############")

        Log.d(TAG, "userId=${this.userId}")
        Log.d(TAG, "notificationId=${this.notificationId}")
        Log.d(TAG, "postTime=${this.unixTime}")

        Log.d(TAG, "appName=${this.appName}")
        Log.d(TAG, "title=${this.title}")
        Log.d(TAG, "content=${this.content}")
        Log.d(TAG, "category=${this.category}")

        Log.d(TAG, "packageName=${this.packageName}")
        Log.d(TAG, "notification=${this.notification}")
        Log.d(TAG, "key=${this.key}")
        Log.d(TAG, "group=${this.group}")

        Log.d(TAG, "############# NotiItem Property End #############")
    }

    fun getFlags(): Int? {
        return flags
    }

    fun getPackageName(): String {
        return packageName
    }

    fun getSbnId(): Int? {
        return sbnId
    }

    fun getTitle(): String {
        return title
    }

    fun getContent(): String {
        return content
    }

    fun makeDrawerNoti(): CurrentDrawer {
        return this.postTime?.let {
            CurrentDrawer(
                0,
                this.notificationId,
                this.packageName,
                this.key,
                this.sortKey,
                "app_name",
                this.title,
                this.content,
                it
            )
        }!!
    }
}