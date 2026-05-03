package com.example.peka.ui.theme

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.neumorphicShadow(
    cornerRadius: Dp = 20.dp,
    shadowRadius: Dp = 25.dp,
    offsetX: Dp = 5.dp,
    offsetY: Dp = 5.dp,
    darkShadowColor: Color = DarkBottomShadow, // Ciemny (czarny 25% przezroczystości)
    lightShadowColor: Color = DarkTopShadow // Jasny (biały 80% przezroczystości)
) = this.then(
    Modifier.drawBehind {
        val cornerRadiusPx = cornerRadius.toPx()
        val offsetXN = offsetX.toPx()
        val offsetYN = offsetY.toPx()
        val shadowRadiusPx = shadowRadius.toPx()

        // Przygotowujemy "pędzel" dla ciemnego cienia
        val darkPaint = Paint().apply {
            color = Color.Transparent
            asFrameworkPaint().setShadowLayer(
                shadowRadiusPx, offsetXN, offsetYN, darkShadowColor.toArgb()
            )
        }

        // Przygotowujemy "pędzel" dla jasnego cienia
        val lightPaint = Paint().apply {
            color = Color.Transparent
            asFrameworkPaint().setShadowLayer(
                shadowRadiusPx, -offsetXN, -offsetYN, lightShadowColor.toArgb()
            )
        }

        drawIntoCanvas { canvas ->
            // Rysujemy jasny cień
            canvas.drawRoundRect(
                left = 0f, top = 0f, right = size.width, bottom = size.height,
                radiusX = cornerRadiusPx, radiusY = cornerRadiusPx, paint = lightPaint
            )
            // Rysujemy ciemny cień
            canvas.drawRoundRect(
                left = 0f, top = 0f, right = size.width, bottom = size.height,
                radiusX = cornerRadiusPx, radiusY = cornerRadiusPx, paint = darkPaint
            )
        }
    }
)