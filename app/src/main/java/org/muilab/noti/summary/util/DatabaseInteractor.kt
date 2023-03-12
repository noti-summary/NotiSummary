package org.muilab.noti.summary.util

import android.content.Context
import android.util.Log
import org.muilab.noti.summary.database.room.CurrentDrawerDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun getActiveNotifications(context: Context): String {
    val currentDrawerDao = CurrentDrawerDatabase.getInstance(context).currentDrawerDao()
    val pkgNames = withContext(Dispatchers.IO) {
        currentDrawerDao.getAllTitles()
    }
    var tmp = ""
    for (pkgName in pkgNames) {
        tmp += pkgName
    }
    Log.d("getActiveNotifications", tmp)
    return tmp
}
