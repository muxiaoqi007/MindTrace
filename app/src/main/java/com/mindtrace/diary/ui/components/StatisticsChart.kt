package com.mindtrace.diary.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.mindtrace.diary.domain.model.MoodLevel
import com.mindtrace.diary.ui.theme.LocalMoodIconPack

@Composable
fun MoodDistributionChart(
    moodDistribution: Map<MoodLevel, Int>,
    modifier: Modifier = Modifier
) {
    val iconPack = LocalMoodIconPack.current

    if (moodDistribution.isEmpty()) {
        EmptyChartPlaceholder(
            text = "暂无心情数据",
            modifier = modifier
        )
        return
    }

    val total = moodDistribution.values.sum().toFloat()

    Column(modifier = modifier) {
        Text(
            text = "心情分布",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Simple bar chart representation
        moodDistribution.entries.sortedByDescending { it.value }.forEach { (mood, count) ->
            val percentage = if (total > 0) count / total else 0f

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Image(
                    painter = painterResource(id = iconPack.getIconRes(mood)),
                    contentDescription = mood.label,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(24.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(percentage)
                            .clip(RoundedCornerShape(4.dp))
                            .background(mood.getColor())
                    )
                }

                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 8.dp).width(32.dp)
                )
            }
        }
    }
}

@Composable
fun MoodTrendChart(
    moodTrend: List<Pair<String, Int>>,
    modifier: Modifier = Modifier
) {
    if (moodTrend.isEmpty()) {
        EmptyChartPlaceholder(
            text = "暂无趋势数据",
            modifier = modifier
        )
        return
    }

    Column(modifier = modifier) {
        Text(
            text = "心情趋势",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Simple text-based display for trend
        Text(
            text = "最近${moodTrend.size}天记录",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun WritingFrequencyChart(
    frequencyData: List<Pair<String, Int>>,
    modifier: Modifier = Modifier
) {
    if (frequencyData.isEmpty()) {
        EmptyChartPlaceholder(
            text = "暂无写作数据",
            modifier = modifier
        )
        return
    }

    Column(modifier = modifier) {
        Text(
            text = "写作频率",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "本月共${frequencyData.sumOf { it.second }}篇",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun StatisticsSummary(
    totalEntries: Int,
    totalWords: Int,
    writingDays: Int,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = modifier.fillMaxWidth()
    ) {
        StatItem(
            label = "总记录",
            value = totalEntries.toString()
        )
        StatItem(
            label = "总字数",
            value = totalWords.toString()
        )
        StatItem(
            label = "写作天数",
            value = writingDays.toString()
        )
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
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
private fun EmptyChartPlaceholder(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .height(150.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
