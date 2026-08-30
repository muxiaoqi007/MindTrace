package com.mindtrace.diary.ui.screens.diary

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.CancellationSignal
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mindtrace.diary.ui.components.BlockEditor
import com.mindtrace.diary.ui.components.MoodSelector
import com.mindtrace.diary.ui.components.TagSelector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryEditScreen(
    diaryId: String?,
    onNavigateBack: () -> Unit,
    viewModel: DiaryEditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val suggestedTags by viewModel.suggestedTags.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var showLocationDialog by remember { mutableStateOf(false) }

    fun readCurrentLocation() {
        val hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!hasPermission) return
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val provider = when {
            manager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
            manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
            else -> null
        }
        if (provider == null) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                manager.getCurrentLocation(provider, CancellationSignal(), context.mainExecutor) { location ->
                    location?.let { viewModel.setLocation(uiState.location ?: "当前位置", it.latitude, it.longitude) }
                }
            } else {
                @Suppress("DEPRECATION")
                manager.getLastKnownLocation(provider)?.let {
                    viewModel.setLocation(uiState.location ?: "当前位置", it.latitude, it.longitude)
                }
            }
        } catch (_: SecurityException) {
            // Permission can be revoked after the explicit check.
        }
    }

    val locationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grants ->
        if (grants.values.any { it }) readCurrentLocation()
    }

    fun requestCurrentLocation() {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (granted) readCurrentLocation()
        else locationPermission.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onNavigateBack()
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
                title = { Text(if (diaryId == null) "写日记" else "编辑日记") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.saveDiary() },
                        enabled = !uiState.isSaving && uiState.title.isNotBlank()
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Check, contentDescription = "保存")
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                contentAlignment = androidx.compose.ui.Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // 标题输入
                BasicTextField(
                    value = uiState.title,
                    onValueChange = viewModel::updateTitle,
                    textStyle = MaterialTheme.typography.headlineSmall.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    decorationBox = { innerTextField ->
                        Box {
                            if (uiState.title.isEmpty()) {
                                Text(
                                    text = "标题",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                            innerTextField()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )

                // 图文混排编辑器
                BlockEditor(
                    blocks = uiState.contentBlocks,
                    onBlocksChange = { viewModel.updateContentBlocks(it) },
                    onInsertTextBlock = { afterId -> viewModel.insertTextBlock(afterId) },
                    onInsertImageBlock = { afterId, path -> viewModel.insertImageBlock(afterId, path) },
                    onDeleteBlock = { blockId -> viewModel.deleteBlock(blockId) },
                    onUpdateText = { blockId, text -> viewModel.updateBlockText(blockId, text) },
                    onUpdateImageCaption = { blockId, caption -> viewModel.updateImageCaption(blockId, caption) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                )

                Spacer(modifier = Modifier.height(24.dp))

                MoodSelector(
                    selectedMood = uiState.mood,
                    onMoodSelected = viewModel::updateMood
                )

                Spacer(modifier = Modifier.height(24.dp))

                TagSelector(
                    selectedTags = uiState.tags,
                    suggestedTags = suggestedTags,
                    onTagAdd = viewModel::addTag,
                    onTagRemove = viewModel::removeTag
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text("地图足迹", style = MaterialTheme.typography.titleMedium)
                Text(
                    if (uiState.latitude != null && uiState.longitude != null) {
                        "${uiState.location ?: "未命名位置"}  ·  ${"%.5f".format(uiState.latitude)}, ${"%.5f".format(uiState.longitude)}"
                    } else "未保存坐标；应用不会在后台获取位置。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 6.dp)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { showLocationDialog = true }) { Text("手动输入") }
                    OutlinedButton(onClick = ::requestCurrentLocation) { Text("使用当前位置") }
                    if (uiState.latitude != null) TextButton(onClick = viewModel::clearLocationCoordinates) { Text("移除坐标") }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "隐私与回顾",
                    style = MaterialTheme.typography.titleMedium
                )
                PrivacySwitchRow(
                    title = "不参与 AI",
                    description = "不发送给 AI，也不用于摘要、记忆、画像和回信",
                    checked = uiState.excludeFromAI,
                    onCheckedChange = viewModel::setExcludeFromAI
                )
                PrivacySwitchRow(
                    title = "不在回顾中出现",
                    description = "不出现在随机漫步、每日小票和历史回顾中",
                    checked = uiState.excludeFromResurfacing,
                    onCheckedChange = viewModel::setExcludeFromResurfacing
                )

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    if (showLocationDialog) {
        var name by remember(uiState.location) { mutableStateOf(uiState.location.orEmpty()) }
        var latitude by remember(uiState.latitude) { mutableStateOf(uiState.latitude?.toString().orEmpty()) }
        var longitude by remember(uiState.longitude) { mutableStateOf(uiState.longitude?.toString().orEmpty()) }
        val lat = latitude.toDoubleOrNull()
        val lon = longitude.toDoubleOrNull()
        AlertDialog(
            onDismissRequest = { showLocationDialog = false },
            title = { Text("添加明确的地图坐标") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(name, { name = it }, label = { Text("地点名称（可选）") })
                    OutlinedTextField(latitude, { latitude = it }, label = { Text("纬度 -90 至 90") })
                    OutlinedTextField(longitude, { longitude = it }, label = { Text("经度 -180 至 180") })
                    Text("坐标只保存在这篇日记中，可随时单独移除。", style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                TextButton(
                    enabled = lat != null && lon != null && lat in -90.0..90.0 && lon in -180.0..180.0,
                    onClick = { viewModel.setLocation(name, lat!!, lon!!); showLocationDialog = false }
                ) { Text("保存坐标") }
            },
            dismissButton = { TextButton(onClick = { showLocationDialog = false }) { Text("取消") } }
        )
    }
}

@Composable
private fun PrivacySwitchRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.width(12.dp))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
