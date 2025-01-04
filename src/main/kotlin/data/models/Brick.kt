package data.models

import androidx.compose.ui.graphics.Color


data class Brick(
    val x: Float,
    val y: Float,
    val width: Float = 60f,
    val height: Float = 25f,
    var isBroken: Boolean = false,
    val color: Color,
    val points: Int,
    var hitPoints: Int = 1
)