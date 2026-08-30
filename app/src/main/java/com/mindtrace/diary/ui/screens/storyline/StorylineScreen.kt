package com.mindtrace.diary.ui.screens.storyline

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.CallMerge
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.mindtrace.diary.domain.model.Storyline
import com.mindtrace.diary.domain.model.StorylineSource
import com.mindtrace.diary.domain.model.StorylineStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorylineScreen(
    onNavigateBack: () -> Unit,
    onOpenDiary: (String) -> Unit,
    viewModel: StorylineViewModel = hiltViewModel()
) {
    val all by viewModel.storylines.collectAsState()
    val candidates = all.filter { it.status == StorylineStatus.CANDIDATE }
    val confirmed = all.filter { it.status == StorylineStatus.CONFIRMED }
    var renameTarget by remember { mutableStateOf<Storyline?>(null) }
    var mergeTarget by remember { mutableStateOf<Storyline?>(null) }
    var splitTarget by remember { mutableStateOf<StorylineSource?>(null) }

    Scaffold(topBar = {
        TopAppBar(title = { Text("人生故事线") }, navigationIcon = {
            IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回") }
        }, actions = {
            IconButton(onClick = viewModel::discover) { Icon(Icons.Default.AutoGraph, "发现候选故事线") }
        })
    }) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("你来确认哪些经历构成一条故事。", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("候选只来至重复标签，每个节点都能回到原日记；拒绝后不再提醒。", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                Button(onClick = viewModel::discover) { Text("从现有日记发现候选") }
            }
            if (candidates.isNotEmpty()) {
                item { Text("待你确认", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                items(candidates, key = Storyline::id) { line ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text("${line.type.label} · ${line.name}", fontWeight = FontWeight.Bold)
                            Text("${line.sources.size} 篇日记提到它", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TextButton(onClick = { viewModel.reject(line.id) }) { Text("不是故事线") }
                                Button(onClick = { viewModel.confirm(line.id) }) { Text("确认") }
                            }
                        }
                    }
                }
            }
            item { Text("我的故事线", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
            if (confirmed.isEmpty()) item { Text("还没有已确认的故事线。", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            items(confirmed, key = Storyline::id) { line ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Row {
                            Column(Modifier.weight(1f)) {
                                Text(line.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("${line.type.label} · ${line.sources.size} 个证据节点", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = { renameTarget = line }) { Icon(Icons.Default.Edit, "重命名") }
                            IconButton(onClick = { mergeTarget = line }) { Icon(Icons.Default.CallMerge, "合并") }
                            IconButton(onClick = { viewModel.archive(line.id) }) { Icon(Icons.Default.Archive, "归档") }
                        }
                        line.sources.sortedBy(StorylineSource::date).forEach { source ->
                            Column(Modifier.fillMaxWidth().clickable { onOpenDiary(source.diaryId) }.padding(vertical = 8.dp)) {
                                Text(source.date.toString(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                                Text(source.excerpt.ifBlank { "查看原日记" }, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                Row {
                                    TextButton(onClick = { viewModel.removeSource(source.id) }) { Text("移除错误来源") }
                                    TextButton(onClick = { splitTarget = source }) { Text("拆成新故事") }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    renameTarget?.let { line -> NameDialog("重命名故事线", line.name, { renameTarget = null }) { viewModel.rename(line.id, it); renameTarget = null } }
    splitTarget?.let { source -> NameDialog("新故事线名称", "", { splitTarget = null }) { viewModel.splitSource(source.id, it); splitTarget = null } }
    mergeTarget?.let { source ->
        AlertDialog(
            onDismissRequest = { mergeTarget = null },
            title = { Text("合并到…") },
            text = { Column { confirmed.filterNot { it.id == source.id }.forEach { target -> OutlinedButton(onClick = { viewModel.merge(source.id, target.id); mergeTarget = null }, modifier = Modifier.fillMaxWidth()) { Text(target.name) } } } },
            confirmButton = {}, dismissButton = { TextButton(onClick = { mergeTarget = null }) { Text("取消") } }
        )
    }
}

@Composable
private fun NameDialog(title: String, initial: String, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var value by remember(initial) { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss, title = { Text(title) },
        text = { OutlinedTextField(value, { value = it }, singleLine = true) },
        confirmButton = { TextButton(onClick = { onSave(value) }, enabled = value.isNotBlank()) { Text("保存") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}
