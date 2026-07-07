package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke

@Composable
fun BuzzShieldZapperVisual(modifier: Modifier = Modifier, isPro: Boolean = false) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        // Colors
        val limeAccent = Color(0xFFA4FF3F)
        val purpleLight = Color(0xFFBB86FC)
        val gridColor = Color(0xFFE2E8F0)
        val protectiveSlatColor = Color(0xFF144F53)

        // 1. Draw UV Light Glow (Behind everything)
        val uvCenter = Offset(width / 2, height * 0.48f)
        val uvRadius = if (isPro) width * 0.55f else width * 0.42f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    purpleLight.copy(alpha = 0.6f * pulseAlpha),
                    purpleLight.copy(alpha = 0.25f * pulseAlpha),
                    Color.Transparent
                ),
                center = uvCenter,
                radius = uvRadius
            )
        )

        // 2. Hanging Loop
        if (!isPro) {
            drawArc(
                color = limeAccent,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(width * 0.4f, height * 0.05f),
                size = Size(width * 0.2f, height * 0.16f),
                style = Stroke(width = 8f, cap = StrokeCap.Round)
            )
        } else {
            drawArc(
                color = gridColor,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(width * 0.35f, height * 0.04f),
                size = Size(width * 0.3f, height * 0.2f),
                style = Stroke(width = 12f, cap = StrokeCap.Round)
            )
        }

        // 3. Top Cap
        val capHeight = height * 0.12f
        val capWidth = width * 0.7f
        val capLeft = width * 0.15f
        drawRoundRect(
            color = Color(0xFF144F53),
            topLeft = Offset(capLeft, height * 0.14f),
            size = Size(capWidth, capHeight),
            cornerRadius = CornerRadius(20f, 20f)
        )
        // Accent bar on Cap
        drawRoundRect(
            color = limeAccent,
            topLeft = Offset(width * 0.35f, height * 0.17f),
            size = Size(width * 0.3f, height * 0.02f),
            cornerRadius = CornerRadius(6f, 6f)
        )

        // 4. Bottom Base Tray
        val baseHeight = height * 0.14f
        val baseWidth = width * 0.76f
        val baseLeft = width * 0.12f
        drawRoundRect(
            color = Color(0xFF104448),
            topLeft = Offset(baseLeft, height * 0.70f),
            size = Size(baseWidth, baseHeight),
            cornerRadius = CornerRadius(24f, 24f)
        )
        // Collection Tray slot line
        drawRoundRect(
            color = Color(0xFF072123),
            topLeft = Offset(width * 0.22f, height * 0.75f),
            size = Size(width * 0.56f, height * 0.03f),
            cornerRadius = CornerRadius(10f, 10f)
        )

        // 5. Central UV Lamp Tube (Internal glowing core)
        val tubeWidth = if (isPro) width * 0.12f else width * 0.08f
        val tubeLeft = (width - tubeWidth) / 2
        val tubeTop = height * 0.24f
        val tubeHeight = height * 0.46f
        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color.White, purpleLight, Color(0xFF7A24F2))
            ),
            topLeft = Offset(tubeLeft, tubeTop),
            size = Size(tubeWidth, tubeHeight),
            cornerRadius = CornerRadius(15f, 15f)
        )

        // 6. Inner High-Voltage Grid Coil (Silver spirals surrounding the tube)
        val gridTop = height * 0.25f
        val gridHeight = height * 0.45f
        val gridWidth = width * 0.46f
        val gridLeft = (width - gridWidth) / 2

        val linesCount = 14
        val spacing = gridHeight / linesCount
        for (i in 0..linesCount) {
            val y = gridTop + i * spacing
            drawLine(
                color = gridColor.copy(alpha = 0.75f),
                start = Offset(gridLeft, y),
                end = Offset(gridLeft + gridWidth, y),
                strokeWidth = 3f
            )
        }

        // Draw vertical support rods for grid
        drawLine(
            color = gridColor,
            start = Offset(gridLeft, gridTop),
            end = Offset(gridLeft, gridTop + gridHeight),
            strokeWidth = 5f
        )
        drawLine(
            color = gridColor,
            start = Offset(gridLeft + gridWidth, gridTop),
            end = Offset(gridLeft + gridWidth, gridTop + gridHeight),
            strokeWidth = 5f
        )

        // 7. Outer Protective Grill Slats (Solid physical barrier)
        val barrierWidth = width * 0.56f
        val barrierLeft = (width - barrierWidth) / 2
        val outerLinesCount = 5
        val outerSpacing = barrierWidth / (outerLinesCount + 1)
        for (i in 1..outerLinesCount) {
            val x = barrierLeft + i * outerSpacing
            drawLine(
                color = protectiveSlatColor,
                start = Offset(x, gridTop - 6f),
                end = Offset(x, gridTop + gridHeight + 6f),
                strokeWidth = 10f,
                cap = StrokeCap.Round
            )
        }

        // Draw horizontal outer safety bands
        drawRoundRect(
            color = protectiveSlatColor,
            topLeft = Offset(barrierLeft - 10f, gridTop + gridHeight * 0.28f),
            size = Size(barrierWidth + 20f, height * 0.03f),
            cornerRadius = CornerRadius(8f, 8f)
        )
        drawRoundRect(
            color = protectiveSlatColor,
            topLeft = Offset(barrierLeft - 10f, gridTop + gridHeight * 0.68f),
            size = Size(barrierWidth + 20f, height * 0.03f),
            cornerRadius = CornerRadius(8f, 8f)
        )

        // Pro Active Lightning Spark Animation
        if (isPro) {
            val sparkPath = Path().apply {
                moveTo(width * 0.44f, height * 0.38f)
                lineTo(width * 0.49f, height * 0.41f)
                lineTo(width * 0.45f, height * 0.42f)
                lineTo(width * 0.53f, height * 0.47f)
                lineTo(width * 0.50f, height * 0.44f)
                lineTo(width * 0.55f, height * 0.43f)
                close()
            }
            drawPath(
                path = sparkPath,
                color = limeAccent.copy(alpha = pulseAlpha)
            )
        }
    }
}
