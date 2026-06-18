package com.mindtrace.diary.ui.screens.ai

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mindtrace.diary.domain.model.ChatPersona

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AISettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToMemory: () -> Unit = {},
    viewModel: AISettingsViewModel = hiltViewModel()
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

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI 设置") },
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
            // 启用开关
            Card {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = "启用 AI 伙伴",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "开启后可与 AI 进行对话",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = uiState.config.enabled,
                        onCheckedChange = viewModel::updateEnabled
                    )
                }
            }

            // API 配置
            Text(
                text = "API 配置",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
                value = uiState.config.baseUrl,
                onValueChange = viewModel::updateBaseUrl,
                label = { Text("API 地址 (Base URL)") },
                placeholder = { Text("https://api.openai.com/v1") },
                supportingText = { Text("支持 OpenAI 兼容格式的任意供应商") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            var showApiKey by remember { mutableStateOf(false) }
            OutlinedTextField(
                value = uiState.config.apiKey,
                onValueChange = viewModel::updateApiKey,
                label = { Text("API Key") },
                placeholder = { Text("sk-...") },
                singleLine = true,
                visualTransformation = if (showApiKey) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showApiKey = !showApiKey }) {
                        Icon(
                            imageVector = if (showApiKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (showApiKey) "隐藏" else "显示"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.config.model,
                onValueChange = viewModel::updateModel,
                label = { Text("模型名称") },
                placeholder = { Text("gpt-3.5-turbo") },
                supportingText = { Text("根据供应商填写对应的模型名称") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // 测试连接按钮
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
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(if (uiState.isTesting) "测试中..." else "测试连接")
                }

                Button(
                    onClick = viewModel::saveConfig,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("保存配置")
                }
            }

            HorizontalDivider()

            // 自动学习记忆开关
            Card {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "自动学习记忆",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "开启后，保存日记时 AI 会自动记住关于你的长期信息",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = uiState.memoryLearningEnabled,
                        onCheckedChange = viewModel::updateMemoryLearning
                    )
                }
            }

            // 记忆管理入口
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToMemory() }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Icon(
                        Icons.Default.Psychology,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "记忆管理",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "管理 AI 对你的长期记忆",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider()

            // AI 人格（Soul）
            Text(
                text = "AI 人格",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            ChatPersona.entries.forEach { persona ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.updateChatPersona(persona.id) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (uiState.chatPersonaId == persona.id)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        RadioButton(
                            selected = uiState.chatPersonaId == persona.id,
                            onClick = { viewModel.updateChatPersona(persona.id) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = persona.displayName,
                                style = MaterialTheme.typography.titleSmall
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

            HorizontalDivider()

            // 系统提示词
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "系统提示词",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                TextButton(onClick = viewModel::resetSystemPrompt) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("重置")
                }
            }

            OutlinedTextField(
                value = uiState.config.systemPrompt,
                onValueChange = viewModel::updateSystemPrompt,
                label = { Text("AI 人设提示词") },
                supportingText = { Text("定义 AI 的角色和行为方式") },
                minLines = 6,
                maxLines = 12,
                modifier = Modifier.fillMaxWidth()
            )

            // 说明卡片
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "使用说明",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "• 支持任何兼容 OpenAI API 格式的服务商",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "• 常见供应商：OpenAI、Azure、Claude API、本地 Ollama 等",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "• AI 会阅读你最近的日记，提供个性化的对话体验",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "• 你的数据仅在本地存储，API 调用时才会发送到服务商",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
