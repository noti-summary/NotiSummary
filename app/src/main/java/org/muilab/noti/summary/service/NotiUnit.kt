package org.muilab.noti.summary.service

import android.os.Parcel
import android.os.Parcelable

data class NotiUnit(
    val appName: String,
    val time: String,
    val title: String,
    val content: String,
    val postTime: Long
):
    Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readLong()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(appName)
        parcel.writeString(time)
        parcel.writeString(title)
        parcel.writeString(content)
        parcel.writeLong(postTime)
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
}