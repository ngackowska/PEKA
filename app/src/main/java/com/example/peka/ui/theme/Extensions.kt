package com.example.peka.ui.theme

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent // ZMIENIONY IMPORT
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
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



fun Modifier.insetNeumorphicShadow(
    cornerRadius: Dp = 20.dp,
    shadowRadius: Dp = 10.dp,
    offsetX: Dp = 5.dp,
    offsetY: Dp = 5.dp,
    darkShadowColor: Color = DarkBottomShadow,
    lightShadowColor: Color = DarkTopShadow
) = this.then(
    Modifier.drawWithContent { // <-- ZMIANA TUTAJ

        // 1. NAJPIERW RYSUIJEMY KARTĘ I JEJ ZAWARTOŚĆ
        drawContent() // <-- ZMIANA TUTAJ (Kluczowe!)

        val width = size.width
        val height = size.height
        val cornerRadiusPx = cornerRadius.toPx()
        val offsetXN = offsetX.toPx()
        val offsetYN = offsetY.toPx()
        val shadowRadiusPx = shadowRadius.toPx()

        val componentShape = Path().apply {
            addRoundRect(
                RoundRect(
                    left = 0f, top = 0f, right = width, bottom = height,
                    cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)
                )
            )
        }

        val boundingBox = Path().apply {
            addRect(Rect(-2000f, -2000f, width + 2000f, height + 2000f))
        }

        val shadowCastingShape = Path().apply {
            op(boundingBox, componentShape, PathOperation.Difference)
        }

        drawIntoCanvas { canvas ->
            canvas.save()
            canvas.clipPath(componentShape)

            val darkPaint = Paint().apply {
                color = Color.Transparent
                asFrameworkPaint().setShadowLayer(
                    shadowRadiusPx, offsetXN, offsetYN, darkShadowColor.toArgb()
                )
            }

            val lightPaint = Paint().apply {
                color = Color.Transparent
                asFrameworkPaint().setShadowLayer(
                    shadowRadiusPx, -offsetXN, -offsetYN, lightShadowColor.toArgb()
                )
            }

            // 2. DOPIERO TERAZ RZUCAMY CIENIE DO ŚRODKA NA WIERZCH KARTY
            canvas.drawPath(shadowCastingShape, darkPaint)
//            canvas.drawPath(shadowCastingShape, lightPaint)

            canvas.restore()
        }
    }
)