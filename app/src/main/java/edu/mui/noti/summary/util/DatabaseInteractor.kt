package edu.mui.noti.summary.util

import android.content.Context
import android.util.Log
import edu.mui.noti.summary.database.room.CurrentDrawerDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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
