package com.mindtrace.diary.ui.screens.magazine

import android.content.Intent
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowLeft
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mindtrace.diary.core.export.WeeklyMagazineExporter
import com.mindtrace.diary.domain.model.WeeklyMagazine

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyMagazineScreen(onNavigateBack: () -> Unit, viewModel: WeeklyMagazineViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    Scaffold(topBar = {
        TopAppBar(title = { Text("每周生活杂志") }, navigationIcon = {
            IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回") }
        })
    }) { padding ->
        state.magazine?.let { value ->
            LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        IconButton(onClick = viewModel::previous) { Icon(Icons.AutoMirrored.Filled.ArrowLeft, "上一周") }
                        Text("${value.weekStart} — ${value.weekEnd}", style = MaterialTheme.typography.titleMedium)
                        IconButton(onClick = viewModel::next, enabled = value.weekEnd.isBefore(java.time.LocalDate.now())) { Icon(Icons.AutoMirrored.Filled.ArrowRight, "下一周") }
                    }
                }
                item {
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(20.dp)) {
                            Text("MINDTRACE WEEKLY", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                            Text("一周的心情、片段和待续之事")
                            Spacer(Modifier.height(16.dp))
                            Text(value.moodStrip.joinToString("  ") { "${it.date.dayOfWeek.name.take(1)} ${it.averageScore?.let { s -> "%.1f".format(s) } ?: "-"}" })
                        }
                    }
                }
                item { MagazineSection("本周片段", value.topMoments) }
                item { MagazineSection("闪念剪辑", value.flashExcerpts) }
                item { MagazineSection("已完成", value.completedGoals) }
                item { MagazineSection("带去下周", value.unresolvedThreads) }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(onClick = { context.startActivity(Intent.createChooser(WeeklyMagazineExporter.createShareIntent(context, value, false), "分享杂志图片")) }) {
                            Icon(Icons.Default.Share, null); Text("  图片")
                        }
                        OutlinedButton(onClick = { context.startActivity(Intent.createChooser(WeeklyMagazineExporter.createShareIntent(context, value, true), "分享杂志 PDF")) }) {
                            Icon(Icons.Default.PictureAsPdf, null); Text("  PDF")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MagazineSection(title: String, values: List<String>) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            if (values.isEmpty()) Text("这周还没有留下这类记录。", color = MaterialTheme.colorScheme.onSurfaceVariant)
            else values.forEach { Text("· $it", modifier = Modifier.padding(vertical = 3.dp)) }
        }
    }
}
