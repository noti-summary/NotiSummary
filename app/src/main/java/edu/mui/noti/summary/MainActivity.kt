package edu.mui.noti.summary

import android.Manifest
import android.annotation.SuppressLint
import android.app.AppOpsManager
import android.content.*
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import edu.mui.noti.summary.service.NotiListenerService
import edu.mui.noti.summary.ui.common.NotiCard
import edu.mui.noti.summary.ui.theme.NotiappTheme
import edu.mui.noti.summary.viewModel.SummaryViewModel
import java.util.*

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!isNotiListenerEnabled()) {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }

        /*
        if (!isUsageEnabled()) {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }
        */

        val notiListenerIntent = Intent(this@MainActivity, NotiListenerService::class.java)
        startService(notiListenerIntent)

        val allNotiFilter = IntentFilter("edu.mui.noti.summary.RETURN_ALLNOTIS")
        registerReceiver(allNotiReturnReceiver, allNotiFilter)

        setContent {
//            NotiCard(sumViewModel)
//            Greeting("world")
            MainScreenView() {
                NotiCard(sumViewModel)
            }
        }
    }

    override fun onDestroy() {
        unregisterReceiver(allNotiReturnReceiver)
        super.onDestroy()
    }

    private val sumViewModel by viewModels<SummaryViewModel>()

    private fun isNotiListenerEnabled(): Boolean {
        val cn = ComponentName(this, NotiListenerService::class.java)
        val flat: String =
            Settings.Secure.getString(this.contentResolver, "enabled_notification_listeners")
        return cn.flattenToString() in flat
    }

    private fun chkPermissionOps(permission: String): Boolean {
        val appOps = this.getSystemService(APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), this.packageName)
        return if (mode == AppOpsManager.MODE_DEFAULT) {
            checkCallingOrSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
        } else {
            mode == AppOpsManager.MODE_ALLOWED
        }
    }

    private fun isUsageEnabled(): Boolean {
        return chkPermissionOps(Manifest.permission.PACKAGE_USAGE_STATS)
    }

    private val allNotiReturnReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "edu.mui.noti.summary.RETURN_ALLNOTIS") {
                val allNotiStr = intent.getStringExtra("allNotis")
                if (allNotiStr != null && allNotiStr != "Not connected") {
                    sumViewModel.updateSummaryText(allNotiStr)
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    NotiappTheme {
        Greeting("Android")
    }
}

sealed class BottomNavItem(var title:String, var icon:Int, var screen_route:String){
    object Home : BottomNavItem("我的摘要", R.drawable.summary,"home")
    object MyNetwork: BottomNavItem("設定",R.drawable.settings,"my_network")
}

@Composable
fun HomeScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.teal_700))
            .wrapContentSize(Alignment.Center)
    ) {
        Text(
            text = "摘要",
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            textAlign = TextAlign.Center,
            fontSize = 20.sp
        )
    }
}

@Composable
fun SettingsScreen() {

    /*
    var showTimePicker by remember { mutableStateOf(false) }
    val formatter = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val state = rememberTimePickerState()
    val snackState = remember { SnackbarHostState() }
    val snackScope = rememberCoroutineScope()

    Box(propagateMinConstraints = false) {
        Button(
            modifier = Modifier.align(Alignment.Center),
            onClick = { showTimePicker = true }
        ) { Text("Set Time") }
        SnackbarHost(hostState = snackState)
    }

    if (showTimePicker) {
        TimePickerDialog(
            onCancel = { showTimePicker = false },
            onConfirm = {
                val cal = Calendar.getInstance()
                cal.set(Calendar.HOUR_OF_DAY, state.hour)
                cal.set(Calendar.MINUTE, state.minute)
                cal.isLenient = false
                snackScope.launch {
                    snackState.showSnackbar("Entered time: ${formatter.format(cal.time)}")
                }
                showTimePicker = false
            },
        ) {
            TimeInput(state = state)
        }
    }
    */
}

@Composable
fun NavigationGraph(
    navController: NavHostController,
    content: @Composable() () -> Unit
) {
    NavHost(navController, startDestination = BottomNavItem.Home.screen_route) {
        composable(BottomNavItem.Home.screen_route) {
            content()
        }
        composable(BottomNavItem.MyNetwork.screen_route) {
            SettingsScreen()
        }
    }
}

@Composable
fun AppBottomNavigation(navController: NavController) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.MyNetwork
    )
    NavigationBar(
        contentColor = Color.Black
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(painterResource(id = item.icon), contentDescription = item.title, modifier = Modifier.size(30.dp)) },
                label = { Text(text = item.title,
                    fontSize = 9.sp) },
                alwaysShowLabel = true,
                selected = currentRoute == item.screen_route,
                onClick = {
                    navController.navigate(item.screen_route) {

                        navController.graph.startDestinationRoute?.let { screen_route ->
                            popUpTo(screen_route) {
                                saveState = true
                            }
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenView(content: @Composable() () -> Unit){
    val navController = rememberNavController()
    Scaffold(
        bottomBar = { AppBottomNavigation(navController = navController) }
    ) {
        NavigationGraph(navController = navController) {
            content()
        }
    }
}