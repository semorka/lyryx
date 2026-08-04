package com.semorka.lyryx.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.semorka.lyryx.net.word_lyrics.LyricSegment
import com.semorka.lyryx.ui.theme.helveticaFontFamily
import kotlin.math.max

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CenteredLyricWordView(
    segment: LyricSegment,
    currentTime: Long,
    modifier: Modifier = Modifier
) {
    val lyricOffsetMs = 0L
    val targetTime = currentTime + lyricOffsetMs

    val contentLayer = rememberGraphicsLayer()

    var meshBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(contentLayer) {
        var frameCounter = 0
        while (true) {
            withFrameNanosSafe()
            frameCounter++
            meshBitmap = try {
                contentLayer.toImageBitmap()
            } catch (e: Exception) {
                meshBitmap
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .drawWithCache {
                val cols = 20
                val rows = 20
                val w = size.width
                val h = size.height

                val vertCount = (cols + 1) * (rows + 1)
                val verts = FloatArray(vertCount * 2)

                var index = 0
                for (i in 0..rows) {
                    for (j in 0..cols) {
                        val x = j * w / cols
                        val y = i * h / rows

                        val normX = (x / w) - 0.5f
                        val normY = (y / h) - 0.5f
                        val r2 = normX * normX + normY * normY

                        val distortion = 1.0f + r2 * 0.42f

                        verts[index * 2] = w * 0.5f + normX * w * distortion
                        verts[index * 2 + 1] = h * 0.5f + normY * h * distortion
                        index++
                    }
                }

                onDrawWithContent {
                    contentLayer.record {
                        this@onDrawWithContent.drawContent()
                    }

                    val bmp = meshBitmap
                    if (bmp != null && bmp.width > 0 && bmp.height > 0) {
                        // 2. Искажаем последний доступный снапшот через drawBitmapMesh.
                        drawIntoCanvas { canvas ->
                            canvas.nativeCanvas.drawBitmapMesh(
                                bmp.asAndroidBitmap(),
                                cols, rows,
                                verts, 0,
                                null, 0,
                                null
                            )
                        }
                    } else {
                        // Пока снапшот ещё не готов (первый кадр) — рисуем как есть,
                        // без искажения, чтобы не было пустого экрана.
                        drawLayer(contentLayer)
                    }
                }
            }
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        val baseFontSize = 30.sp

        WrappingWordsLayout(
            modifier = Modifier.fillMaxWidth(),
            lineSpacing = 0.dp,
            wordSpacing = 8.dp
        ) {
            segment.words.forEach { wordSegment ->
                val isVisible = targetTime >= wordSegment.timeMillis

                val scale by animateFloatAsState(
                    targetValue = if (isVisible) 1f else 1.3f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "scale_animation"
                )

                val textColor = if (isVisible) Color.Black else Color.Transparent
                val shadowColor = if (isVisible) Color.Black.copy(alpha = 0.2f) else Color.Transparent

                GlitchWord(
                    word = wordSegment.word,
                    isVisible = isVisible,
                    scale = scale,
                    fontSize = baseFontSize,
                    textColor = textColor,
                    shadowColor = shadowColor
                )
            }
        }
    }
}
@Composable
private fun GlitchWord(
    word: String,
    isVisible: Boolean,
    scale: Float,
    fontSize: androidx.compose.ui.unit.TextUnit,
    textColor: Color,
    shadowColor: Color
) {
    val glitch = remember(word) { Animatable(0f) }

    LaunchedEffect(isVisible) {
        if (isVisible) {
            glitch.snapTo(1f)
            glitch.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
            )
        }
    }

    val maxOffsetPx = 6f
    val maxFringeAlpha = 0.7f
    val offsetPx = glitch.value * maxOffsetPx
    val fringeAlpha = glitch.value * maxFringeAlpha

    Box(
        modifier = Modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
    ) {
        if (fringeAlpha > 0f) {
            Text(
                text = word,
                modifier = Modifier.graphicsLayer { translationX = -offsetPx },
                style = TextStyle(
                    fontFamily = helveticaFontFamily,
                    fontSize = fontSize,
                    fontWeight = FontWeight.Medium,
                    color = Color.Red.copy(alpha = fringeAlpha)
                )
            )
            Text(
                text = word,
                modifier = Modifier.graphicsLayer { translationX = offsetPx },
                style = TextStyle(
                    fontFamily = helveticaFontFamily,
                    fontSize = fontSize,
                    fontWeight = FontWeight.Medium,
                    color = Color.Cyan.copy(alpha = fringeAlpha)
                )
            )
        }

        Text(
            text = word,
            style = TextStyle(
                fontFamily = helveticaFontFamily,
                fontSize = fontSize,
                fontWeight = FontWeight.Medium,
                color = textColor,
                shadow = Shadow(
                    color = shadowColor,
                    offset = Offset.Zero,
                    blurRadius = 4f
                )
            )
        )
    }
}

private suspend fun withFrameNanosSafe() {
    androidx.compose.runtime.withFrameNanos { }
}

@Composable
private fun WrappingWordsLayout(
    modifier: Modifier = Modifier,
    lineSpacing: Dp = 4.dp,
    wordSpacing: Dp = 8.dp,
    onSingleWordLines: (Set<Int>) -> Unit = {},
    content: @Composable () -> Unit
) {
    Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        val maxWidth = constraints.maxWidth
        val gapPx = wordSpacing.roundToPx()
        val lineSpacingPx = lineSpacing.roundToPx()

        val wordConstraints = Constraints(maxWidth = maxWidth)
        val placeables = measurables.map { it.measure(wordConstraints) }

        data class LineInfo(val indices: List<Int>, val height: Int)

        val lines = mutableListOf<LineInfo>()
        var currentIndices = mutableListOf<Int>()
        var currentWidth = 0

        for (i in placeables.indices) {
            val p = placeables[i]
            val extra = if (currentIndices.isEmpty()) 0 else gapPx
            val widthIfAdded = currentWidth + extra + p.width

            if (currentIndices.isNotEmpty() && widthIfAdded > maxWidth) {
                val lineHeight = currentIndices.maxOf { placeables[it].height }
                lines += LineInfo(currentIndices, lineHeight)
                currentIndices = mutableListOf(i)
                currentWidth = p.width
            } else {
                currentIndices.add(i)
                currentWidth = widthIfAdded
            }
        }
        if (currentIndices.isNotEmpty()) {
            val lineHeight = currentIndices.maxOf { placeables[it].height }
            lines += LineInfo(currentIndices, lineHeight)
        }

        // Сообщаем наружу индексы слов, оказавшихся единственными в своей строке
        val singleWordIndices = lines
            .filter { it.indices.size == 1 }
            .map { it.indices.first() }
            .toSet()
        onSingleWordLines(singleWordIndices)

        val totalHeight = lines.sumOf { it.height } +
                lineSpacingPx * (max(0, lines.size - 1))

        layout(maxWidth, totalHeight) {
            var y = 0
            for (line in lines) {
                var x = 0
                for ((pos, idx) in line.indices.withIndex()) {
                    val placeable = placeables[idx]
                    placeable.placeRelative(x, y + (line.height - placeable.height) / 2)
                    x += placeable.width
                    if (pos < line.indices.size - 1) {
                        x += gapPx
                    }
                }
                y += line.height + lineSpacingPx
            }
        }
    }
}