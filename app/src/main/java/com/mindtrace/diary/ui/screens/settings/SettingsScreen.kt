package com.mindtrace.diary.ui.screens.settings

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.mindtrace.diary.core.datastore.ThemeMode
import com.mindtrace.diary.domain.model.MoodIconPacks
import com.mindtrace.diary.domain.model.MoodLevel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onTagsClick: () -> Unit = {},
    onAISettingsClick: () -> Unit = {},
    onAIMemoryCenterClick: () -> Unit = {},
    onAISoulConfigClick: () -> Unit = {},
    onWebDAVSettingsClick: () -> Unit = {},
    onMidnightReviewHistoryClick: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // 当返回此页面时重新检查通知权限
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.checkNotificationPermission()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // 文件选择器 - 导出为 ZIP 格式
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri: Uri? ->
        uri?.let { viewModel.exportData(it) }
    }

    // 导入支持 JSON 和 ZIP 格式
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { viewModel.importData(it) }
    }

    LaunchedEffect(uiState.exportMessage) {
        uiState.exportMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearExportMessage()
        }
    }

    LaunchedEffect(uiState.importMessage) {
        uiState.importMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearImportMessage()
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
                title = { Text("设置") }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Theme settings
            SettingsSection(title = "外观设置") {
                ThemeSelector(
                    selectedMode = uiState.themeMode,
                    onModeSelected = viewModel::setThemeMode
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 心情图标包选择
                MoodIconPackSelector(
                    selectedPackId = uiState.moodIconPackId,
                    onPackSelected = viewModel::setMoodIconPack
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // AI Settings
            SettingsSection(title = "AI 伙伴") {
                ListItem(
                    headlineContent = { Text("AI 设置") },
                    supportingContent = { Text("配置 AI 服务和基础开关") },
                    leadingContent = {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onAISettingsClick() }
                )

                HorizontalDivider()

                ListItem(
                    headlineContent = { Text("记忆中心") },
                    supportingContent = { Text("确认候选记忆、管理长期记忆和查看自我画像") },
                    leadingContent = {
                        Icon(Icons.Default.Psychology, contentDescription = null)
                    },
                    trailingContent = {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onAIMemoryCenterClick() }
                )

                HorizontalDivider()

                ListItem(
                    headlineContent = { Text("Soul 设置") },
                    supportingContent = { Text("配置 AI 人格、称呼和相处方式") },
                    leadingContent = {
                        Icon(Icons.Default.SelfImprovement, contentDescription = null)
                    },
                    trailingContent = {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onAISoulConfigClick() }
                )

                HorizontalDivider()

                // 通知权限提示
                if (!uiState.hasNotificationPermission) {
                    NotificationPermissionCard(
                        onRequestPermission = {
                            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                            }
                            context.startActivity(intent)
                        }
                    )
                    HorizontalDivider()
                }

                // 深夜回信设置
                MidnightReviewSettings(
                    config = uiState.midnightReviewConfig,
                    aiConfigured = uiState.aiConfigured,
                    onEnabledChange = viewModel::setMidnightReviewEnabled,
                    onTimeChange = viewModel::setMidnightReviewTime,
                    onPersonaChange = viewModel::setMidnightReviewPersona,
                    onHistoryClick = onMidnightReviewHistoryClick,
                    onTestNow = viewModel::testMidnightReview
                )

                HorizontalDivider()

                // 沉默唤醒设置
                SilenceBreakSettings(
                    config = uiState.silenceBreakConfig,
                    emailConfig = uiState.emailConfig,
                    isTestingEmail = uiState.isTestingEmail,
                    onEnabledChange = viewModel::setSilenceBreakEnabled,
                    onHoursChange = viewModel::setSilenceBreakHours,
                    onEmailEnabledChange = viewModel::setSilenceEmailEnabled,
                    onEmailThresholdDaysChange = viewModel::setSilenceEmailThresholdDays,
                    onEmailConfigChange = viewModel::setEmailConfig,
                    onTestEmail = viewModel::testEmailConnection,
                    onTestNow = viewModel::testSilenceBreak
                )

                HorizontalDivider()

                // 日记分析设置
                DiaryAnalysisSettings(
                    enabled = uiState.diaryAnalysisEnabled,
                    aiConfigured = uiState.aiConfigured,
                    onEnabledChange = viewModel::setDiaryAnalysisEnabled
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // WebDAV settings
            SettingsSection(title = "数据同步") {
                ListItem(
                    headlineContent = { Text("WebDAV 同步") },
                    supportingContent = {
                        Text(if (uiState.webDavConfig.isConfigured) "已配置" else "未配置")
                    },
                    leadingContent = {
                        Icon(Icons.Default.Cloud, contentDescription = null)
                    },
                    trailingContent = {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                    },
                    modifier = Modifier.clickable { onWebDAVSettingsClick() }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Data Management
            SettingsSection(title = "数据管理") {
                DataManagementSettings(
                    onTagsClick = onTagsClick,
                    onExportClick = {
                        val timestamp = System.currentTimeMillis()
                        exportLauncher.launch("mindtrace_backup_$timestamp.zip")
                    },
                    onImportClick = {
                        importLauncher.launch(arrayOf("application/json", "application/zip", "application/octet-stream"))
                    },
                    isExporting = uiState.isExporting,
                    isImporting = uiState.isImporting
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // About
            SettingsSection(title = "关于") {
                ListItem(
                    headlineContent = { Text("MindTrace") },
                    supportingContent = { Text("版本 1.0.0") },
                    leadingContent = {
                        Icon(Icons.Default.Info, contentDescription = null)
                    }
                )
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        content()
    }
}

@Composable
private fun ThemeSelector(
    selectedMode: ThemeMode,
    onModeSelected: (ThemeMode) -> Unit
) {
    Column {
        Text(
            text = "主题模式",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        ThemeMode.entries.forEach { mode ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                RadioButton(
                    selected = mode == selectedMode,
                    onClick = { onModeSelected(mode) }
                )
                Text(
                    text = when (mode) {
                        ThemeMode.SYSTEM -> "跟随系统"
                        ThemeMode.LIGHT -> "浅色"
                        ThemeMode.DARK -> "深色"
                    },
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MoodIconPackSelector(
    selectedPackId: String,
    onPackSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedPack = MoodIconPacks.getById(selectedPackId)

    Column {
        Text(
            text = "心情图标包",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedPack.name,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                leadingIcon = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        MoodLevel.entries.take(3).forEach { mood ->
                            Image(
                                painter = painterResource(id = selectedPack.getIconRes(mood)),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                MoodIconPacks.allPacks.forEach { pack ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // 显示所有5个表情预览
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    MoodLevel.entries.forEach { mood ->
                                        Image(
                                            painter = painterResource(id = pack.getIconRes(mood)),
                                            contentDescription = null,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(pack.name)
                            }
                        },
                        onClick = {
                            onPackSelected(pack.id)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DataManagementSettings(
    onTagsClick: () -> Unit,
    onExportClick: () -> Unit,
    onImportClick: () -> Unit,
    isExporting: Boolean,
    isImporting: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        // 标签管理
        ListItem(
            headlineContent = { Text("标签管理") },
            supportingContent = { Text("查看和管理所有标签") },
            leadingContent = {
                Icon(Icons.AutoMirrored.Filled.Label, contentDescription = null)
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onTagsClick() }
        )

        HorizontalDivider()

        // 导出数据
        ListItem(
            headlineContent = { Text("导出数据") },
            supportingContent = { Text("导出到 ZIP 文件（包含图片）") },
            leadingContent = {
                if (isExporting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.Upload, contentDescription = null)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedButton(
            onClick = onExportClick,
            enabled = !isExporting,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isExporting) "导出中..." else "导出数据")
        }

        Spacer(modifier = Modifier.height(8.dp))

        HorizontalDivider()

        // 导入数据
        ListItem(
            headlineContent = { Text("导入数据") },
            supportingContent = { Text("从 ZIP 或 JSON 文件恢复") },
            leadingContent = {
                if (isImporting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.Download, contentDescription = null)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedButton(
            onClick = onImportClick,
            enabled = !isImporting,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isImporting) "导入中..." else "导入数据")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MidnightReviewSettings(
    config: com.mindtrace.diary.core.datastore.MidnightReviewConfig,
    aiConfigured: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    onTimeChange: (Int, Int) -> Unit,
    onPersonaChange: (String) -> Unit,
    onHistoryClick: () -> Unit,
    onTestNow: () -> Unit
) {
    var showTimePickerDialog by remember { mutableStateOf(false) }
    var showPersonaDialog by remember { mutableStateOf(false) }

    val personas = com.mindtrace.diary.domain.model.MidnightReviewPersona.entries
    val currentPersona = com.mindtrace.diary.domain.model.MidnightReviewPersona.fromId(config.persona)

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        // 开关
        ListItem(
            headlineContent = { Text("深夜回信") },
            supportingContent = {
                Text(
                    if (!aiConfigured) "请先配置 AI 服务"
                    else "每晚收到 AI 的温暖回信"
                )
            },
            leadingContent = {
                Icon(Icons.Default.Nightlight, contentDescription = null)
            },
            trailingContent = {
                Switch(
                    checked = config.enabled,
                    onCheckedChange = onEnabledChange,
                    enabled = aiConfigured
                )
            }
        )

        if (config.enabled) {
            // 时间设置
            ListItem(
                headlineContent = { Text("回信时间") },
                supportingContent = {
                    Text(String.format("%02d:%02d", config.hour, config.minute))
                },
                leadingContent = {
                    Icon(Icons.Default.Schedule, contentDescription = null)
                },
                modifier = Modifier.clickable { showTimePickerDialog = true }
            )

            // Persona 设置
            ListItem(
                headlineContent = { Text("回信视角") },
                supportingContent = { Text(currentPersona.displayName) },
                leadingContent = {
                    Icon(Icons.Default.Person, contentDescription = null)
                },
                modifier = Modifier.clickable { showPersonaDialog = true }
            )

            // 查看历史
            ListItem(
                headlineContent = { Text("回信历史") },
                supportingContent = { Text("查看收到的所有回信") },
                leadingContent = {
                    Icon(Icons.Default.History, contentDescription = null)
                },
                modifier = Modifier.clickable { onHistoryClick() }
            )

            // 测试按钮
            OutlinedButton(
                onClick = onTestNow,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text("立即测试")
            }
        }
    }

    // 时间选择对话框
    if (showTimePickerDialog) {
        val timePickerState = rememberTimePickerState(
            initialHour = config.hour,
            initialMinute = config.minute
        )

        AlertDialog(
            onDismissRequest = { showTimePickerDialog = false },
            title = { Text("选择回信时间") },
            text = {
                TimePicker(state = timePickerState)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onTimeChange(timePickerState.hour, timePickerState.minute)
                        showTimePickerDialog = false
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePickerDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    // Persona 选择对话框
    if (showPersonaDialog) {
        AlertDialog(
            onDismissRequest = { showPersonaDialog = false },
            title = { Text("选择回信视角") },
            text = {
                Column {
                    personas.forEach { persona ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onPersonaChange(persona.id)
                                    showPersonaDialog = false
                                }
                                .padding(vertical = 8.dp)
                        ) {
                            RadioButton(
                                selected = persona.id == config.persona,
                                onClick = {
                                    onPersonaChange(persona.id)
                                    showPersonaDialog = false
                                }
                            )
                            Column(modifier = Modifier.padding(start = 8.dp)) {
                                Text(
                                    text = persona.displayName,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = persona.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPersonaDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun SilenceBreakSettings(
    config: com.mindtrace.diary.core.datastore.SilenceBreakConfig,
    emailConfig: com.mindtrace.diary.core.datastore.EmailConfig,
    isTestingEmail: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    onHoursChange: (Int) -> Unit,
    onEmailEnabledChange: (Boolean) -> Unit,
    onEmailThresholdDaysChange: (Int) -> Unit,
    onEmailConfigChange: (com.mindtrace.diary.core.datastore.EmailConfig) -> Unit,
    onTestEmail: () -> Unit,
    onTestNow: () -> Unit
) {
    var showHoursDialog by remember { mutableStateOf(false) }
    var showEmailConfigDialog by remember { mutableStateOf(false) }
    var showEmailThresholdDialog by remember { mutableStateOf(false) }

    val hoursOptions = listOf(24, 48, 72, 96, 120, 168) // 1天, 2天, 3天, 4天, 5天, 7天
    val emailThresholdDaysOptions = listOf(3, 5, 7, 14, 30) // 邮件通知阈值选项

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        // 开关
        ListItem(
            headlineContent = { Text("沉默唤醒") },
            supportingContent = { Text("长时间未活跃时发送提醒") },
            leadingContent = {
                Icon(Icons.Default.NotificationsActive, contentDescription = null)
            },
            trailingContent = {
                Switch(
                    checked = config.enabled,
                    onCheckedChange = onEnabledChange
                )
            }
        )

        if (config.enabled) {
            // 时间阈值设置
            ListItem(
                headlineContent = { Text("通知提醒阈值") },
                supportingContent = {
                    val days = config.hours / 24
                    val remainingHours = config.hours % 24
                    Text(
                        if (remainingHours == 0) "${days} 天未活跃"
                        else "${days} 天 ${remainingHours} 小时未活跃"
                    )
                },
                leadingContent = {
                    Icon(Icons.Default.Timer, contentDescription = null)
                },
                modifier = Modifier.clickable { showHoursDialog = true }
            )

            HorizontalDivider(modifier = Modifier.padding(start = 56.dp))

            // 邮件通知开关
            ListItem(
                headlineContent = { Text("邮件通知") },
                supportingContent = {
                    Text(
                        if (emailConfig.isConfigured) "长时间未活跃时发送邮件提醒"
                        else "点击配置邮箱"
                    )
                },
                leadingContent = {
                    Icon(Icons.Default.Email, contentDescription = null)
                },
                trailingContent = {
                    Switch(
                        checked = config.emailEnabled,
                        onCheckedChange = onEmailEnabledChange,
                        enabled = emailConfig.isConfigured
                    )
                }
            )

            // 邮件配置
            ListItem(
                headlineContent = { Text("邮箱配置") },
                supportingContent = {
                    Text(
                        if (emailConfig.isConfigured) "已配置: ${emailConfig.recipient}"
                        else "未配置"
                    )
                },
                leadingContent = {
                    Icon(Icons.Default.Settings, contentDescription = null)
                },
                modifier = Modifier.clickable { showEmailConfigDialog = true }
            )

            if (config.emailEnabled && emailConfig.isConfigured) {
                // 邮件阈值设置
                ListItem(
                    headlineContent = { Text("邮件提醒阈值") },
                    supportingContent = { Text("${config.emailThresholdDays} 天未活跃时发送邮件") },
                    leadingContent = {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null)
                    },
                    modifier = Modifier.clickable { showEmailThresholdDialog = true }
                )
            }

            // 测试按钮
            OutlinedButton(
                onClick = onTestNow,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text("立即测试")
            }
        }
    }

    // 时间阈值选择对话框
    if (showHoursDialog) {
        AlertDialog(
            onDismissRequest = { showHoursDialog = false },
            title = { Text("选择提醒阈值") },
            text = {
                Column {
                    hoursOptions.forEach { hours ->
                        val days = hours / 24
                        val label = "${days} 天"
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onHoursChange(hours)
                                    showHoursDialog = false
                                }
                                .padding(vertical = 8.dp)
                        ) {
                            RadioButton(
                                selected = hours == config.hours,
                                onClick = {
                                    onHoursChange(hours)
                                    showHoursDialog = false
                                }
                            )
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showHoursDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    // 邮件阈值选择对话框
    if (showEmailThresholdDialog) {
        AlertDialog(
            onDismissRequest = { showEmailThresholdDialog = false },
            title = { Text("选择邮件提醒阈值") },
            text = {
                Column {
                    emailThresholdDaysOptions.forEach { days ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onEmailThresholdDaysChange(days)
                                    showEmailThresholdDialog = false
                                }
                                .padding(vertical = 8.dp)
                        ) {
                            RadioButton(
                                selected = days == config.emailThresholdDays,
                                onClick = {
                                    onEmailThresholdDaysChange(days)
                                    showEmailThresholdDialog = false
                                }
                            )
                            Text(
                                text = "$days 天",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showEmailThresholdDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    // 邮箱配置对话框
    if (showEmailConfigDialog) {
        EmailConfigDialog(
            config = emailConfig,
            isTestingEmail = isTestingEmail,
            onDismiss = { showEmailConfigDialog = false },
            onSave = { newConfig ->
                onEmailConfigChange(newConfig)
                showEmailConfigDialog = false
            },
            onTestEmail = onTestEmail
        )
    }
}

@Composable
private fun EmailConfigDialog(
    config: com.mindtrace.diary.core.datastore.EmailConfig,
    isTestingEmail: Boolean,
    onDismiss: () -> Unit,
    onSave: (com.mindtrace.diary.core.datastore.EmailConfig) -> Unit,
    onTestEmail: () -> Unit
) {
    var smtpHost by remember { mutableStateOf(config.smtpHost) }
    var smtpPort by remember { mutableStateOf(config.smtpPort.toString()) }
    var username by remember { mutableStateOf(config.username) }
    var password by remember { mutableStateOf(config.password) }
    var recipient by remember { mutableStateOf(config.recipient) }
    var useSSL by remember { mutableStateOf(config.useSSL) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("邮箱配置") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "配置 SMTP 服务器用于发送沉默唤醒邮件通知",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = smtpHost,
                    onValueChange = { smtpHost = it },
                    label = { Text("SMTP 服务器") },
                    placeholder = { Text("如 smtp.qq.com") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = smtpPort,
                    onValueChange = { smtpPort = it.filter { c -> c.isDigit() } },
                    label = { Text("端口") },
                    placeholder = { Text("如 465 或 587") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = useSSL,
                        onCheckedChange = { useSSL = it }
                    )
                    Text("使用 SSL 加密")
                }

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("发件邮箱") },
                    placeholder = { Text("你的邮箱地址") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("授权码/密码") },
                    placeholder = { Text("邮箱授权码") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = recipient,
                    onValueChange = { recipient = it },
                    label = { Text("收件邮箱") },
                    placeholder = { Text("接收通知的邮箱") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // 测试连接按钮
                OutlinedButton(
                    onClick = {
                        // 先保存配置再测试
                        onSave(
                            com.mindtrace.diary.core.datastore.EmailConfig(
                                smtpHost = smtpHost,
                                smtpPort = smtpPort.toIntOrNull() ?: 465,
                                username = username,
                                password = password,
                                recipient = recipient,
                                useSSL = useSSL
                            )
                        )
                        onTestEmail()
                    },
                    enabled = !isTestingEmail && smtpHost.isNotBlank() && username.isNotBlank() && password.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isTestingEmail) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(if (isTestingEmail) "测试中..." else "测试连接")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(
                        com.mindtrace.diary.core.datastore.EmailConfig(
                            smtpHost = smtpHost,
                            smtpPort = smtpPort.toIntOrNull() ?: 465,
                            username = username,
                            password = password,
                            recipient = recipient,
                            useSSL = useSSL
                        )
                    )
                }
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
private fun DiaryAnalysisSettings(
    enabled: Boolean,
    aiConfigured: Boolean,
    onEnabledChange: (Boolean) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        ListItem(
            headlineContent = { Text("记忆反刍") },
            supportingContent = {
                Text(
                    if (!aiConfigured) "请先配置 AI 服务"
                    else "保存日记时自动生成摘要和情感分析"
                )
            },
            leadingContent = {
                Icon(Icons.Default.Psychology, contentDescription = null)
            },
            trailingContent = {
                Switch(
                    checked = enabled,
                    onCheckedChange = onEnabledChange,
                    enabled = aiConfigured
                )
            }
        )

        if (enabled) {
            Text(
                text = "开启后，每次保存日记时会自动分析内容，生成摘要、情感分数和关键词标签。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

@Composable
private fun NotificationPermissionCard(
    onRequestPermission: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable { onRequestPermission() }
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.NotificationsOff,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "通知权限未开启",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text = "点击前往设置开启，以接收深夜回信和沉默唤醒通知",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}
