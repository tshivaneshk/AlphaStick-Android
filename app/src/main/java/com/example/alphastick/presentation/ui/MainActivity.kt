package com.example.alphastick.presentation.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.alphastick.core.worker.ScanWorker
import com.example.alphastick.presentation.ui.theme.AlphaStickTheme
import com.example.alphastick.presentation.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val prefs = getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
        
        setupPeriodicScan()

        setContent {
            val themeState = remember { mutableStateOf(prefs.getString("app_theme", "System Default") ?: "System Default") }
            val darkTheme = when (themeState.value) {
                "Dark" -> true
                "Light" -> false
                else -> androidx.compose.foundation.isSystemInDarkTheme()
            }

            AlphaStickTheme(darkTheme = darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: MainViewModel = hiltViewModel()
                    val navController = rememberNavController()
                    
                    NavHost(
                        navController = navController, 
                        startDestination = "main",
                        enterTransition = { slideInHorizontally(initialOffsetX = { 1000 }, animationSpec = tween(400)) + fadeIn(animationSpec = tween(400)) },
                        exitTransition = { slideOutHorizontally(targetOffsetX = { -1000 }, animationSpec = tween(400)) + fadeOut(animationSpec = tween(400)) },
                        popEnterTransition = { slideInHorizontally(initialOffsetX = { -1000 }, animationSpec = tween(400)) + fadeIn(animationSpec = tween(400)) },
                        popExitTransition = { slideOutHorizontally(targetOffsetX = { 1000 }, animationSpec = tween(400)) + fadeOut(animationSpec = tween(400)) }
                    ) {
                        composable("main") {
                            MainScreen(
                                viewModel = viewModel,
                                onAppClick = { packageName ->
                                    navController.navigate("detail/$packageName")
                                },
                                onSettingsClick = {
                                    navController.navigate("settings")
                                }
                            )
                        }
                        composable("detail/{packageName}") { backStackEntry ->
                            val packageName = backStackEntry.arguments?.getString("packageName") ?: ""
                            DetailScreen(
                                viewModel = viewModel,
                                packageName = packageName,
                                onBackClick = { navController.popBackStack() },
                                onOpenSettingsClick = { openAppSettings(packageName) }
                            )
                        }
                        composable("settings") {
                            SettingsScreen(
                                currentTheme = themeState.value,
                                onThemeChange = { newTheme ->
                                    themeState.value = newTheme
                                    prefs.edit().putString("app_theme", newTheme).apply()
                                },
                                onBackClick = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun setupPeriodicScan() {
        val workRequest = PeriodicWorkRequestBuilder<ScanWorker>(24, TimeUnit.HOURS).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "PeriodicAppScan",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    private fun openAppSettings(packageName: String) {
        try {
            val intent = Intent("android.intent.action.MANAGE_APP_PERMISSIONS").apply {
                putExtra(Intent.EXTRA_PACKAGE_NAME, packageName)
            }
            startActivity(intent)
        } catch (e: Exception) {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
            }
            startActivity(intent)
        }
    }
}
