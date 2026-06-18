package com.mindtrace.diary.ui.screens.webdav

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mindtrace.diary.core.sync.RestoreResult
import com.mindtrace.diary.core.sync.SyncResult
import com.mindtrace.diary.core.util.DateUtils
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebDAVSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: WebDAVSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.testResult) {
        uiState.testResult?.let { result ->
            val message = if (result) "连接成功" else "连接失败"
            snackbarHostState.showSnackbar(message)
            viewModel.clearTestResult()
        }
    }

    LaunchedEffect(uiState.syncResult) {
        uiState.syncResult?.let { result ->
            val message = when (result) {
                is SyncResult.Success -> "同步完成：上传 ${result.uploadedCount} 项，下载 ${result.downloadedCount} 项"
                is SyncResult.Error -> "同步失败：${result.message}"
                is SyncResult.NotConfigured -> "请先配置 WebDAV"
            }
            snackbarHostState.showSnackbar(message)
            viewModel.clearSyncResult()
        }
    }

    LaunchedEffect(uiState.restoreResult) {
        uiState.restoreResult?.let { result ->
            val message = when (result) {
                is RestoreResult.Success -> "恢复完成：共恢复 ${result.restoredCount} 条数据"
                is RestoreResult.Error -> "恢复失败：${result.message}"
                is RestoreResult.NotConfigured -> "请先配置 WebDAV"
            }
            snackbarHostState.showSnackbar(message)
            viewModel.clearRestoreResult()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("WebDAV 同步") },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.saveConfig()
                        onNavigateBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 服务器配置卡片
            Text(
                text = "服务器配置",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            var showPassword by remember { mutableStateOf(false) }

            OutlinedTextField(
                value = uiState.config.url,
                onValueChange = viewModel::updateWebDavUrl,
                label = { Text("服务器地址") },
                placeholder = { Text("https://dav.example.com") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.config.username,
                onValueChange = viewModel::updateWebDavUsername,
                label = { Text("用户名") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.config.password,
                onValueChange = viewModel::updateWebDavPassword,
                label = { Text("密码") },
                singleLine = true,
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (showPassword) "隐藏密码" else "显示密码"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.config.path,
                onValueChange = viewModel::updateWebDavPath,
                label = { Text("同步路径") },
                placeholder = { Text("/MindTrace") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider()

            // 同步操作按钮
            Text(
                text = "同步操作",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = {
                        viewModel.saveConfig()
                        viewModel.testConnection()
                    },
                    enabled = !uiState.isTesting && uiState.config.isConfigured,
                    modifier = Modifier.weight(1f)
                ) {
                    if (uiState.isTesting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("测试连接")
                    }
                }

                Button(
                    onClick = viewModel::syncNow,
                    enabled = !uiState.isSyncing && uiState.config.isConfigured,
                    modifier = Modifier.weight(1f)
                ) {
                    if (uiState.isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("立即同步")
                    }
                }
            }

            // 从云端恢复按钮
            OutlinedButton(
                onClick = { viewModel.showRestoreConfirmDialog() },
                enabled = !uiState.isRestoring && uiState.config.isConfigured,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isRestoring) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Icon(Icons.Default.CloudDownload, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("从云端恢复")
            }

            Text(
                text = "从云端恢复会删除本地所有数据，用云端数据完全替换",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalDivider()

            // 自动同步开关
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "自动同步",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "数据变更时自动同步到云端",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = uiState.autoSyncEnabled,
                    onCheckedChange = viewModel::setAutoSyncEnabled,
                    enabled = uiState.config.isConfigured
                )
            }

            // 上次同步时间
            uiState.lastSyncTime?.let { time ->
                val dateTime = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(time),
                    ZoneId.systemDefault()
                )
                Text(
                    text = "上次同步: ${DateUtils.formatDateTime(dateTime)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } ?: run {
                Text(
                    text = "从未同步",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // 确认恢复对话框
    if (uiState.showRestoreConfirmDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissRestoreConfirmDialog() },
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("确认恢复") },
            text = {
                Text("此操作将删除所有本地数据，并用云端数据替换。此操作不可撤销，确定继续吗？")
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.restoreFromCloud() },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("确认恢复")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissRestoreConfirmDialog() }) {
                    Text("取消")
                }
            }
        )
    }
}
