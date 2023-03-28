package org.muilab.noti.summary.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Icon
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import org.muilab.noti.summary.R
import org.muilab.noti.summary.view.settings.APIKeyScreen
import org.muilab.noti.summary.view.settings.MainSettingScreen
import org.muilab.noti.summary.view.settings.PromptScreen
import org.muilab.noti.summary.viewModel.APIKeyViewModel
import org.muilab.noti.summary.viewModel.PromptViewModel

enum class SettingScreenItem(var title: String, var iconId: Int) {
    Start("Main Setting Page", R.drawable.settings),
    SettingPrompt("提示句設定", R.drawable.setting_sms),
    SettingAPI("OpenAI API 金鑰設定", R.drawable.setting_key),
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    promptViewModel: PromptViewModel,
    apiKeyViewModel: APIKeyViewModel,
    context: Context,
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen =
        SettingScreenItem.valueOf(
            backStackEntry?.destination?.route ?: SettingScreenItem.Start.name
        )

    Scaffold(
        topBar = {
            SettingTopBar(
                currentScreen = currentScreen,
                canNavigateBack = navController.previousBackStackEntry != null,
                navigateUp = { navController.navigateUp() }
            )
        }
    ) { innerPadding ->
        NavigateSetting(
            navController = navController,
            modifier = modifier.padding(innerPadding),
            context,
            promptViewModel,
            apiKeyViewModel
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingTopBar(
    currentScreen: SettingScreenItem,
    canNavigateBack: Boolean,
    navigateUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (currentScreen != SettingScreenItem.Start) {
        TopAppBar(
            title = { Text(currentScreen.title) },
            modifier = modifier,
            navigationIcon = {
                if (canNavigateBack) {
                    IconButton(onClick = navigateUp) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back_button)
                        )
                    }
                }
            }
        )
    } else {
        Text(
            text = "設定",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
fun NavigateSetting(
    navController: NavHostController,
    modifier: Modifier,
    context: Context,
    promptViewModel: PromptViewModel,
    apiKeyViewModel: APIKeyViewModel
) {
    NavHost(navController, startDestination = SettingScreenItem.Start.name, modifier = modifier) {
        composable(SettingScreenItem.Start.name) {
            MainSettingScreen(navController)
        }
        composable(SettingScreenItem.SettingPrompt.name) {
            PromptScreen(context, promptViewModel)
        }
        composable(SettingScreenItem.SettingAPI.name) {
            APIKeyScreen(apiKeyViewModel)
        }
    }
}
