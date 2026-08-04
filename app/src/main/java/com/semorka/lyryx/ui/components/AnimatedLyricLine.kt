package com.semorka.lyryx.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.semorka.lyryx.net.word_lyrics.LyricSegment

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AnimatedLyricLine(
    segment: LyricSegment,
    currentTime: Long,
    modifier: Modifier = Modifier
) {
    val lyricOffsetMs = 50L
    val targetTime = currentTime + lyricOffsetMs

    FlowRow(
        modifier = modifier,
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center
    ) {
        segment.words.forEach { wordSegment ->
            val isHighlighted = targetTime >= wordSegment.timeMillis

            val textColor = if (isHighlighted) {
                MaterialTheme.colorScheme.primary
            } else {
                Color.Gray.copy(alpha = 0.5f)
            }

            val fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal

            val baseStyle = TextStyle(
                fontSize = 24.sp,
                fontWeight = fontWeight
            )

            Box {
                Text(
                    text = "${wordSegment.word} ",
                    style = baseStyle.copy(
                        drawStyle = Stroke(
                            width = 12f,
                            join = StrokeJoin.Round
                        ),
                        color = Color.Black
                    )
                )
                Text(
                    text = "${wordSegment.word} ",
                    style = baseStyle.copy(
                        color = textColor
                    )
                )
            }
        }
    }
}