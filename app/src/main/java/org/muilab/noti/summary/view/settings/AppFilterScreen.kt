package org.muilab.noti.summary.view.settings

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import org.muilab.noti.summary.util.AppScope
import org.muilab.noti.summary.util.uploadData

@Composable
fun AppFilterScreen(context: Context) {

    val pm = context.packageManager
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

    val density = LocalDensity.current

    val appFilterPrefs = context.getSharedPreferences("app_filter", Context.MODE_PRIVATE)

    val appFilterMap = remember {
        val map = mutableStateMapOf<String, Boolean>()
        // Initialize all package names with true as the default state
        packages.forEach { packageInfo ->
            if (packageInfo.packageName in packagesWithLauncher) {
                map[packageInfo.packageName] = true
            }
        }
        map
    }

    // Load the saved state from shared preferences on composition start
    LaunchedEffect(Unit) {
        appFilterPrefs.all.forEach { (packageName, state) ->
            if (state is Boolean) {
                appFilterMap[packageName] = state
            }
        }
    }

    LazyColumn {
        items(packages) { packageInfo ->
            if (packageInfo.packageName in packagesWithLauncher) {
                val appIcon = pm.getApplicationIcon(packageInfo.packageName)
                val appBitmap = appIcon.toBitmap()
                val scaledBitmap = Bitmap.createScaledBitmap(
                    appBitmap,
                    density.run { 48.dp.toPx().toInt() },
                    density.run { 48.dp.toPx().toInt() },
                    true
                )
                val appIconBitmap: ImageBitmap = scaledBitmap.asImageBitmap()
                val appIconPainter: Painter = BitmapPainter(appIconBitmap)

                Box(Modifier.padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 10.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {

                        Image(painter = appIconPainter, contentDescription = null, contentScale = ContentScale.FillHeight)

                        Spacer(modifier = Modifier.width(20.dp))

                        Text(text = pm.getApplicationLabel(packageInfo).toString(), modifier = Modifier.weight(1f))

                        val checkedState = appFilterMap.getOrDefault(packageInfo.packageName, false)
                        Switch(
                            checked = checkedState,
                            onCheckedChange = { newState ->
                                appFilterMap[packageInfo.packageName] = newState
                                with(appFilterPrefs.edit()) {
                                    putBoolean(packageInfo.packageName, newState)
                                    apply()
                                }
                                LogAppFilters(context, appFilterMap)
                            }
                        )
                    }
                }
            }
        }
    }
}

fun LogAppFilters(context: Context, appFilterMap: Map<String, Boolean>) {
    val sharedPref = context.getSharedPreferences("user", Context.MODE_PRIVATE)
    val userId = sharedPref.getString("user_id", "000").toString()
    val timestamp = System.currentTimeMillis()
    val appScope = AppScope(userId, timestamp, appFilterMap)
    uploadData("appScope", appScope)
}