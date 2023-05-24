package org.muilab.noti.summary.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.runtime.mutableStateMapOf
import kotlinx.coroutines.Dispatchers
import org.muilab.noti.summary.database.room.DrawerDatabase
import org.muilab.noti.summary.model.NotiUnit

suspend fun getDatabaseNotifications(context: Context, activeKeys: ArrayList<Pair<String, String>>): ArrayList<NotiUnit> = with(Dispatchers.IO) {

    var databaseNotifications = arrayListOf<NotiUnit>()
    val drawerDatabase = DrawerDatabase.getInstance(context)
    val drawerDao = drawerDatabase.drawerDao()

    activeKeys.forEach { (pkgName, sbnKey) ->
        val pkgNotis = drawerDao.getBySbnKey(pkgName, sbnKey)
            .sortedWith(
                compareByDescending<NotiUnit> { it.groupKey }
                    .thenBy { it.sortKey }
                    .thenBy { it.`when` }
            )
        databaseNotifications.addAll(pkgNotis)
    }
    databaseNotifications = databaseNotifications
        .distinctBy { it.appName to it.time to it.title to it.content }
        .toCollection(ArrayList())
    databaseNotifications.forEachIndexed { idx, notiUnit -> notiUnit.index = idx }

    return databaseNotifications
}

fun getAppFilter(context: Context): Map<String, Boolean> {

    val pm = context.packageManager
    val appFilterPref = context.getSharedPreferences("app_filter", Context.MODE_PRIVATE)

    val mainIntent = Intent(Intent.ACTION_MAIN, null)
    mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)

    val packages = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        pm.getInstalledApplications(PackageManager.ApplicationInfoFlags.of(0))
    } else {
        pm.getInstalledApplications(0)
    }

    val launcherActivities = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        pm.queryIntentActivities(mainIntent, PackageManager.ResolveInfoFlags.of(0))
    } else {
        pm.queryIntentActivities(mainIntent, 0)
    }

    val packagesWithLauncher = mutableListOf<String>()
    for (activity in launcherActivities) {
        packagesWithLauncher.add(activity.activityInfo.packageName)
    }
    packagesWithLauncher.remove("org.muilab.noti.summary")
    packagesWithLauncher.add("android")

    val appFilterMap = mutableStateMapOf<String, Boolean>()
    // Initialize all package names with true as the default state
    packages.forEach { packageInfo ->
        if (packageInfo.packageName in packagesWithLauncher) {
            appFilterMap[packageInfo.packageName] = true
        }
    }

    appFilterPref.all.forEach { (packageName, state) ->
        if (state is Boolean) {
            appFilterMap[packageName] = state
        }
    }

    return appFilterMap
}