package com.mindtrace.diary.ui.screens.lexicon

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mindtrace.diary.domain.model.LexiconEntry
import com.mindtrace.diary.domain.model.LexiconStatus
import com.mindtrace.diary.domain.model.LexiconType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalLexiconScreen(
    onNavigateBack: () -> Unit,
    onOpenDiary: (String) -> Unit,
    viewModel: PersonalLexiconViewModel = hiltViewModel()
) {
    val entries by viewModel.entries.collectAsState()
    val candidates = entries.filter { it.status == LexiconStatus.CANDIDATE }
    val confirmed = entries.filter { it.status == LexiconStatus.CONFIRMED }
    var correctionTarget by remember { mutableStateOf<LexiconEntry?>(null) }

    Scaffold(topBar = {
        TopAppBar(title = { Text("我的词典") }, navigationIcon = {
            IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回") }
        }, actions = {
            IconButton(onClick = viewModel::discover) { Icon(Icons.Default.AutoAwesome, "发现词条") }
        })
    }) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("只有你确认过的意义，才算你的词典。", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("人物、地点、个人用语和愿望都保留日记证据；你的修正会优先用于 AI 上下文。", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Button(onClick = viewModel::discover, modifier = Modifier.padding(top = 10.dp)) { Text("从日记发现词条") }
            }
            if (candidates.isNotEmpty()) {
                item { Text("待确认", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                items(candidates, key = LexiconEntry::id) { entry ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text("${entry.type.label} · ${entry.term}", fontWeight = FontWeight.Bold)
                            Text(entry.generatedMeaning)
                            Text("${entry.evidence.size} 条原始证据", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TextButton(onClick = { viewModel.reject(entry.id) }) { Text("不收录") }
                                Button(onClick = { viewModel.confirm(entry.id) }) { Text("收录") }
                            }
                        }
                    }
                }
            }
            LexiconType.entries.forEach { type ->
                val typeEntries = confirmed.filter { it.type == type }
                if (typeEntries.isNotEmpty()) {
                    item { Text(type.label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                    items(typeEntries, key = LexiconEntry::id) { entry ->
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp)) {
                                Row {
                                    Column(Modifier.weight(1f)) {
                                        Text(entry.term, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        Text(entry.displayMeaning)
                                    }
                                    IconButton(onClick = { correctionTarget = entry }) { Icon(Icons.Default.EditNote, "修正释义") }
                                }
                                Text("意义证据", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 8.dp))
                                entry.evidence.sortedBy { it.date }.forEach { evidence ->
                                    Column(Modifier.fillMaxWidth().clickable { onOpenDiary(evidence.diaryId) }.padding(vertical = 6.dp)) {
                                        Text(evidence.date.toString(), style = MaterialTheme.typography.labelSmall)
                                        Text(evidence.excerpt.ifBlank { "查看原日记" }, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (confirmed.isEmpty() && candidates.isEmpty()) item { Text("还没有词条，可以从日记标签、位置和愿望句中发现。", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }

    correctionTarget?.let { entry ->
        var meaning by remember(entry.id) { mutableStateOf(entry.displayMeaning) }
        AlertDialog(
            onDismissRequest = { correctionTarget = null },
            title = { Text("修正“${entry.term}”的意义") },
            text = { OutlinedTextField(meaning, { meaning = it }, minLines = 3, modifier = Modifier.fillMaxWidth(), supportingText = { Text("这个版本会覆盖自动说明并供 AI 使用") }) },
            confirmButton = { TextButton(onClick = { viewModel.correct(entry.id, meaning); correctionTarget = null }, enabled = meaning.isNotBlank()) { Text("保存修正") } },
            dismissButton = { TextButton(onClick = { correctionTarget = null }) { Text("取消") } }
        )
    }
}
