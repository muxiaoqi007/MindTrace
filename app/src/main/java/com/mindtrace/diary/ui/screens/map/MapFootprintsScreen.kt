package com.mindtrace.diary.ui.screens.map

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowLeft
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mindtrace.diary.domain.model.FootprintCluster
import com.mindtrace.diary.domain.model.MapFootprint
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapFootprintsScreen(
    onNavigateBack: () -> Unit,
    onOpenDiary: (String) -> Unit,
    viewModel: MapFootprintsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    Scaffold(topBar = { TopAppBar(title = { Text("地图足迹") }, navigationIcon = {
        IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回") }
    }) }) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("只展示你主动保存的坐标。", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("这是本地离线概览，不请求第三方地图图片，也不会后台追踪。", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            item { MonthHeader(state.month, viewModel::previousMonth, viewModel::nextMonth) }
            item {
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    OfflineWorldMap(state.points, Modifier.fillMaxWidth().aspectRatio(1.8f).padding(12.dp))
                }
            }
            item { Text("地点聚类 · ${state.points.size} 篇日记", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
            if (state.clusters.isEmpty()) item { Text("这个月还没有主动保存的地图足迹。", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            items(state.clusters, key = FootprintCluster::key) { cluster ->
                ClusterCard(cluster, onOpenDiary, viewModel::removeCoordinates)
            }
        }
    }
}

@Composable
private fun OfflineWorldMap(points: List<MapFootprint>, modifier: Modifier) {
    val grid = MaterialTheme.colorScheme.outlineVariant
    val dot = MaterialTheme.colorScheme.primary
    val background = MaterialTheme.colorScheme.surfaceVariant
    Canvas(modifier) {
        drawRect(background)
        for (i in 1 until 6) drawLine(grid, Offset(size.width * i / 6f, 0f), Offset(size.width * i / 6f, size.height), 1f)
        for (i in 1 until 3) drawLine(grid, Offset(0f, size.height * i / 3f), Offset(size.width, size.height * i / 3f), 1f)
        points.forEach { point ->
            val x = ((point.longitude + 180.0) / 360.0 * size.width).toFloat()
            val y = ((90.0 - point.latitude) / 180.0 * size.height).toFloat()
            drawCircle(Color.White, 9f, Offset(x, y)); drawCircle(dot, 6f, Offset(x, y))
        }
    }
}

@Composable
private fun ClusterCard(cluster: FootprintCluster, openDiary: (String) -> Unit, remove: (String) -> Unit) {
    val context = LocalContext.current
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(cluster.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("${cluster.points.size} 次记录", color = MaterialTheme.colorScheme.onSurfaceVariant)
            cluster.points.forEach { point ->
                Column(Modifier.fillMaxWidth().padding(top = 10.dp)) {
                    Text(point.date.toString(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    Text(point.excerpt.ifBlank { "查看原日记" }, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Row {
                        TextButton(onClick = { openDiary(point.diaryId) }) { Text("打开日记") }
                        OutlinedButton(onClick = {
                            val uri = Uri.parse("geo:${point.latitude},${point.longitude}?q=${point.latitude},${point.longitude}(${Uri.encode(point.name)})")
                            runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, uri)) }
                        }) { Icon(Icons.Default.Map, null); Text("  系统地图") }
                        IconButton(onClick = { remove(point.diaryId) }) { Icon(Icons.Default.LocationOff, "移除坐标") }
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthHeader(month: YearMonth, previous: () -> Unit, next: () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        IconButton(onClick = previous) { Icon(Icons.AutoMirrored.Filled.ArrowLeft, "上一月") }
        Text(month.format(DateTimeFormatter.ofPattern("yyyy年 M月", Locale.CHINA)), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 12.dp))
        IconButton(onClick = next, enabled = month.isBefore(YearMonth.now())) { Icon(Icons.AutoMirrored.Filled.ArrowRight, "下一月") }
    }
}
