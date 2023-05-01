package org.muilab.noti.summary.service

import android.app.Notification
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Build.VERSION_CODES.TIRAMISU
import android.os.Parcel
import android.os.Parcelable
import android.service.notification.StatusBarNotification
import androidx.annotation.RequiresApi
import java.text.SimpleDateFormat
import java.util.*

// val appName: String, val time: String, val title: String, val content: String

data class NotiUnit(
    // For logging
    val drawerIndex: Int,
    val `when`: Long,
    val postTime: Long,
    val sbnKey: String,
    val pkgName: String,
    val category: String,
    // For display
    var appName: String = "Unknown App",
    var time: String = "??:??",
    var title: String = "Unknown Title",
    var content: String = "Unknown Content"
): Parcelable {

    val notiId: String = "${sbnKey}_${postTime}"

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readLong(),
        parcel.readLong(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!
    )

    @RequiresApi(Build.VERSION_CODES.Q)
    constructor(context: Context, sbn: StatusBarNotification, index: Int): this(
        drawerIndex = index,
        `when` = sbn.notification?.`when` as Long,
        postTime = sbn.postTime,
        sbnKey = sbn.key,
        pkgName = sbn.opPkg,
        category = sbn.notification?.category ?: "Unknown",
    ) {
        contentInit(context, sbn)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(drawerIndex)
        parcel.writeLong(`when`)
        parcel.writeLong(postTime)
        parcel.writeString(sbnKey)
        parcel.writeString(pkgName)
        parcel.writeString(category)
        parcel.writeString(appName)
        parcel.writeString(time)
        parcel.writeString(title)
        parcel.writeString(content)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<NotiUnit> {
        override fun createFromParcel(parcel: Parcel): NotiUnit {
            return NotiUnit(parcel)
        }

        override fun newArray(size: Int): Array<NotiUnit?> {
            return arrayOfNulls(size)
        }
    }

    private fun contentInit(context: Context, sbn: StatusBarNotification) {

        fun replaceChars(str: String): String {
            return str.replace("\n", " ")
                .replace(",", " ")
        }

        // appName
        val pm = context.packageManager
        val applicationInfo: ApplicationInfo? =
            sbn.packageName?.let {
                if (Build.VERSION.SDK_INT >= TIRAMISU) {
                    pm.getApplicationInfo(it, PackageManager.ApplicationInfoFlags.of(0))
                } else {
                    pm.getApplicationInfo(it, 0)
                }
            }
        appName = (if (applicationInfo != null) {
            pm.getApplicationLabel(applicationInfo).toString()
        } else {
            "Unknown App"
        })

        // time
        val simpleDateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val date = Date(postTime)
        time = simpleDateFormat.format(date)

        // title
        title = sbn.notification?.extras?.getCharSequence(Notification.EXTRA_TITLE).toString()
        title = replaceChars(title)

        // content
        content = sbn.notification?.extras?.getCharSequence(Notification.EXTRA_TEXT).toString()
        if (content.isBlank())
            content = sbn.notification?.extras?.getCharSequence(Notification.EXTRA_BIG_TEXT).toString()
        content = replaceChars(content)
    }
}