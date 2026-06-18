package com.mindtrace.diary.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mindtrace.diary.domain.model.MoodLevel
import com.mindtrace.diary.domain.usecase.statistics.MoodTrendPoint
import com.mindtrace.diary.ui.theme.LocalMoodIconPack

/**
 * 心情趋势图 - 7天心情折线图
 */
@Composable
fun MoodTrendLineChart(
    trendData: List<MoodTrendPoint>,
    modifier: Modifier = Modifier
) {
    val iconPack = LocalMoodIconPack.current

    if (trendData.isEmpty() || trendData.all { it.mood == null }) {
        EmptyChartPlaceholderNew(
            text = "暂无心情记录",
            modifier = modifier.height(160.dp)
        )
        return
    }

    var animationProgress by remember { mutableFloatStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = animationProgress,
        animationSpec = tween(durationMillis = 800),
        label = "chart_animation"
    )

    LaunchedEffect(trendData) {
        animationProgress = 1f
    }

    Column(modifier = modifier) {
        Text(
            text = "心情趋势",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // 折线图
        val lineColor = MaterialTheme.colorScheme.primary
        val gridColor = MaterialTheme.colorScheme.outlineVariant

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
        ) {
            val width = size.width
            val height = size.height
            val padding = 20f
            val chartWidth = width - padding * 2
            val chartHeight = height - padding * 2

            // 绘制网格线
            for (i in 0..4) {
                val y = padding + (chartHeight / 4) * i
                drawLine(
                    color = gridColor,
                    start = Offset(padding, y),
                    end = Offset(width - padding, y),
                    strokeWidth = 1f
                )
            }

            // 绘制折线
            val validPoints = trendData.filter { it.mood != null }
            if (validPoints.size >= 2) {
                val path = Path()
                val pointWidth = chartWidth / (trendData.size - 1).coerceAtLeast(1)

                validPoints.forEachIndexed { index, point ->
                    val dataIndex = trendData.indexOf(point)
                    val x = padding + pointWidth * dataIndex * animatedProgress
                    val y = padding + chartHeight - (point.moodScore / 5f) * chartHeight

                    if (index == 0) {
                        path.moveTo(x, y)
                    } else {
                        path.lineTo(x, y)
                    }
                }

                drawPath(
                    path = path,
                    color = lineColor,
                    style = Stroke(width = 3f)
                )

                // 绘制数据点
                validPoints.forEach { point ->
                    val dataIndex = trendData.indexOf(point)
                    val x = padding + pointWidth * dataIndex * animatedProgress
                    val y = padding + chartHeight - (point.moodScore / 5f) * chartHeight

                    drawCircle(
                        color = lineColor,
                        radius = 6f,
                        center = Offset(x, y)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 3f,
                        center = Offset(x, y)
                    )
                }
            }
        }

        // X轴日期标签
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            trendData.forEach { point ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(36.dp)
                ) {
                    if (point.mood != null) {
                        Image(
                            painter = painterResource(id = iconPack.getIconRes(point.mood)),
                            contentDescription = point.mood.label,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Text(
                            text = "·",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "${point.date.dayOfMonth}日",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * 心情指数仪表盘
 */
@Composable
fun MoodIndexGauge(
    moodIndex: Float,
    dominantMood: MoodLevel?,
    modifier: Modifier = Modifier
) {
    val iconPack = LocalMoodIconPack.current
    val animatedIndex by animateFloatAsState(
        targetValue = moodIndex,
        animationSpec = tween(durationMillis = 1000),
        label = "mood_index"
    )

    val gaugeColor = when {
        moodIndex >= 80 -> Color(0xFF4CAF50)  // 绿色
        moodIndex >= 60 -> Color(0xFF8BC34A)  // 浅绿
        moodIndex >= 40 -> Color(0xFFFFC107)  // 黄色
        moodIndex >= 20 -> Color(0xFFFF9800)  // 橙色
        else -> Color(0xFFF44336)  // 红色
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(80.dp)
        ) {
            // 背景圆环
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 10f
                val radius = (size.minDimension - strokeWidth) / 2

                // 背景弧
                drawArc(
                    color = Color.Gray.copy(alpha = 0.2f),
                    startAngle = 135f,
                    sweepAngle = 270f,
                    useCenter = false,
                    topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth)
                )

                // 进度弧
                drawArc(
                    color = gaugeColor,
                    startAngle = 135f,
                    sweepAngle = 270f * (animatedIndex / 100f),
                    useCenter = false,
                    topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (dominantMood != null) {
                    Image(
                        painter = painterResource(id = iconPack.getIconRes(dominantMood)),
                        contentDescription = dominantMood.label,
                        modifier = Modifier.size(32.dp)
                    )
                } else {
                    Text(
                        text = "😊",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
                Text(
                    text = "${animatedIndex.toInt()}",
                    style = MaterialTheme.typography.titleLarge,
                    color = gaugeColor
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "心情指数",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 连续写作天数徽章
 */
@Composable
fun WritingStreakBadge(
    streak: Int,
    modifier: Modifier = Modifier
) {
    val isActive = streak > 0
    val badgeColor = if (isActive) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(badgeColor.copy(alpha = 0.15f))
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isActive) "🔥" else "💤",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "$streak",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isActive) badgeColor else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "连续天数",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 统计数字卡片
 */
@Composable
fun StatNumberCard(
    value: Int,
    label: String,
    emoji: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = emoji,
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EmptyChartPlaceholderNew(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
