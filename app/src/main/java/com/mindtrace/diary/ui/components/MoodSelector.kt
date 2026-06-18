package com.mindtrace.diary.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mindtrace.diary.domain.model.MoodIconPack
import com.mindtrace.diary.domain.model.MoodIconPacks
import com.mindtrace.diary.domain.model.MoodLevel
import com.mindtrace.diary.ui.theme.LocalMoodIconPack

@Composable
fun MoodSelector(
    selectedMood: MoodLevel?,
    onMoodSelected: (MoodLevel?) -> Unit,
    modifier: Modifier = Modifier
) {
    val iconPack = LocalMoodIconPack.current

    Column(modifier = modifier) {
        Text(
            text = "今天心情怎么样？",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            MoodLevel.entries.forEach { mood ->
                MoodItem(
                    mood = mood,
                    iconRes = iconPack.getIconRes(mood),
                    isSelected = mood == selectedMood,
                    onClick = {
                        if (mood == selectedMood) {
                            onMoodSelected(null)
                        } else {
                            onMoodSelected(mood)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun MoodItem(
    mood: MoodLevel,
    iconRes: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val moodColor = mood.getColor()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .background(
                if (isSelected) moodColor.copy(alpha = 0.2f)
                else Color.Transparent
            )
            .then(
                if (isSelected) Modifier.border(
                    width = 2.dp,
                    color = moodColor,
                    shape = MaterialTheme.shapes.medium
                ) else Modifier
            )
            .padding(8.dp)
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = mood.label,
            modifier = Modifier.size(36.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = mood.label,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            color = if (isSelected) moodColor else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        // 选中指示器
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(
                    color = if (isSelected) moodColor else Color.Transparent,
                    shape = CircleShape
                )
        )
    }
}

@Composable
fun MoodChip(
    mood: MoodLevel,
    modifier: Modifier = Modifier
) {
    val iconPack = LocalMoodIconPack.current
    val moodColor = mood.getColor()

    Surface(
        color = moodColor.copy(alpha = 0.2f),
        shape = CircleShape,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Image(
                painter = painterResource(id = iconPack.getIconRes(mood)),
                contentDescription = mood.label,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = mood.label,
                style = MaterialTheme.typography.labelSmall,
                color = moodColor
            )
        }
    }
}

@Composable
fun getMoodColor(mood: MoodLevel): Color {
    return mood.getColor()
}
