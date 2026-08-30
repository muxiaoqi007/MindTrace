package com.mindtrace.diary.ui.screens.facets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mindtrace.diary.domain.model.FacetInsight
import com.mindtrace.diary.domain.model.LifeFacet
import com.mindtrace.diary.domain.usecase.facets.FacetCorrelationCalculator
import com.mindtrace.diary.ui.theme.FacetSwatchColors
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LifeFacetsScreen(
    onNavigateBack: () -> Unit,
    viewModel: LifeFacetsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var showAdd by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("生活切面") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { showAdd = true }) {
                        Icon(Icons.Default.Add, contentDescription = "创建切面")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text("今天是怎样的？", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(
                    "你定义维度，数据只在本地帮你发现可能的关联。",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (state.facets.isEmpty()) {
                item {
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(20.dp)) {
                            Text("先创建一个切面", fontWeight = FontWeight.Bold)
                            Text("例如：睡眠节奏（早睡 / 正常 / 晚睡）、独处时间、运动量。")
                            Spacer(Modifier.height(12.dp))
                            Button(onClick = { showAdd = true }) { Text("创建生活切面") }
                        }
                    }
                }
            }
            items(state.facets, key = LifeFacet::id) { facet ->
                FacetCard(
                    facet = facet,
                    selected = state.todayCheckIns[facet.id]?.option,
                    insight = state.insights[facet.id],
                    onSelect = { viewModel.selectOption(facet.id, it) },
                    onArchive = { viewModel.archiveFacet(facet.id) }
                )
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }

    if (showAdd) {
        AddFacetDialog(
            onDismiss = { showAdd = false },
            onSave = { name, icon, color, options ->
                viewModel.saveFacet(name, icon, color, options)
                showAdd = false
            }
        )
    }
}

@Composable
private fun FacetCard(
    facet: LifeFacet,
    selected: String?,
    insight: FacetInsight?,
    onSelect: (String) -> Unit,
    onArchive: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(facet.color).copy(alpha = .10f))
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(facet.icon, style = MaterialTheme.typography.headlineSmall)
                Text(facet.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp).weight(1f))
                IconButton(onClick = onArchive) { Icon(Icons.Default.Archive, contentDescription = "归档切面") }
            }
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                facet.options.forEach { option ->
                    AssistChip(
                        onClick = { onSelect(option) },
                        label = { Text(if (selected == option) "✓ $option" else option) }
                    )
                }
            }
            insight?.options?.filter { it.sampleCount > 0 }?.forEach { option ->
                Spacer(Modifier.height(8.dp))
                if (!option.hasEnoughEvidence) {
                    Text(
                        "${option.option}：${option.sampleCount} 次记录，还需 ${FacetCorrelationCalculator.MIN_SAMPLE_SIZE - option.sampleCount} 次才展示倾向",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    val delta = option.moodDifference ?: 0f
                    Text(
                        "${option.option} 与心情 ${if (delta >= 0) "+" else ""}${"%.1f".format(delta)} 分同时出现（${option.sampleCount} 天）",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "证据：${option.evidenceDates.take(5).joinToString { it.format(DateTimeFormatter.ofPattern("M/d")) }}。这是关联，不代表因果。",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun AddFacetDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, Long, List<String>) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var icon by remember { mutableStateOf("✦") }
    var options by remember { mutableStateOf("") }
    var color by remember { mutableLongStateOf(FacetSwatchColors.first()) }
    val parsedOptions = options.split(Regex("[,，/\\n]")).map(String::trim).filter(String::isNotEmpty)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("创建生活切面") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("名称") }, singleLine = true)
                OutlinedTextField(icon, { icon = it.take(4) }, label = { Text("图标 / Emoji") }, singleLine = true)
                OutlinedTextField(options, { options = it }, label = { Text("选项，用逗号分隔") }, supportingText = { Text("例：早睡，正常，晚睡") })
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FacetSwatchColors.forEach { value ->
                        Box(
                            Modifier.size(30.dp).background(Color(value), CircleShape).clickable { color = value },
                            contentAlignment = Alignment.Center
                        ) { if (color == value) Text("✓", color = Color.White) }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(name, icon, color, parsedOptions) }, enabled = name.isNotBlank() && parsedOptions.isNotEmpty()) { Text("创建") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}

