package com.mindtrace.diary.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mindtrace.diary.core.datastore.SettingsDataStore
import com.mindtrace.diary.ui.navigation.BottomNavBar
import com.mindtrace.diary.ui.navigation.NavGraph
import com.mindtrace.diary.ui.navigation.Screen
import com.mindtrace.diary.ui.theme.MindTraceTheme
import com.mindtrace.diary.ui.theme.ProvideMoodIconPack
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsDataStore: SettingsDataStore

    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* 权限请求结果，无需额外处理 */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNotificationPermission()

        setContent {
            val mainViewModel: MainViewModel = hiltViewModel()
            val themeMode by mainViewModel.themeMode.collectAsState()
            val moodIconPackId by mainViewModel.moodIconPackId.collectAsState()

            MindTraceTheme(themeMode = themeMode) {
                ProvideMoodIconPack(moodIconPackId = moodIconPackId) {
                    val navController = rememberNavController()
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route

                    // 检测键盘是否弹出
                    val imeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0

                    // Determine if we should show bottom nav (不在键盘弹出时显示)
                    val showBottomNav = !imeVisible && currentRoute in listOf(
                        Screen.Home.route,
                        Screen.Calendar.route,
                        Screen.Statistics.route,
                        Screen.Settings.route
                    )

                    Scaffold(
                        bottomBar = {
                            if (showBottomNav) {
                                BottomNavBar(navController = navController)
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    ) { innerPadding ->
                        NavGraph(
                            navController = navController,
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // 更新最后活跃时间（用于沉默唤醒功能）
        lifecycleScope.launch {
            settingsDataStore.updateLastActiveTime()
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
