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

@Composable
fun NotiFilter(context: Context) {

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

    val density = LocalDensity.current

    val notiFilterPrefs = context.getSharedPreferences("noti_filter", Context.MODE_PRIVATE)

    val notiFilterMap = remember {
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
        notiFilterPrefs.all.forEach { (packageName, state) ->
            if (state is Boolean) {
                notiFilterMap[packageName] = state
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
                    Row(modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(painter = appIconPainter, contentDescription = null, contentScale = ContentScale.FillHeight)

                        Spacer(modifier = Modifier.width(20.dp))

                        Text(text = pm.getApplicationLabel(packageInfo).toString(), modifier = Modifier.weight(1f))

                        val checkedState = notiFilterMap.getOrDefault(packageInfo.packageName, false)
                        Switch(
                            checked = checkedState,
                            onCheckedChange = { newState ->
                                notiFilterMap[packageInfo.packageName] = newState
                                with(notiFilterPrefs.edit()) {
                                    putBoolean(packageInfo.packageName, newState)
                                    apply()
                                }
                            }
                        )
                    }
                }
            }
        }
    }

}