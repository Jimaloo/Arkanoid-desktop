package domain

import androidx.compose.ui.graphics.Color
import data.models.Brick

fun createBricks(screenWidth: Float, screenHeight: Float): List<Brick> {
    val bricks = mutableListOf<Brick>()
    val brickWidth = 60f
    val brickHeight = 25f
    val padding = 5f
    val numCols = ((screenWidth - padding) / (brickWidth + padding)).toInt()
    val startX = (screenWidth - (numCols * (brickWidth + padding) - padding)) / 2

    val colors = listOf(
        Color(0xFFFF595E),  // Red
        Color(0xFFFFCA3A),  // Yellow
        Color(0xFF8AC926),  // Green
        Color(0xFF1982C4),  // Blue
        Color(0xFF6A4C93)   // Purple
    )

    val points = listOf(50, 40, 30, 20, 10)

    for (row in 0 until 5) {
        for (col in 0 until numCols) {
            val isStone = (row == 2 && col % 3 == 0) // Example: Place stone bricks in row 2 at specific columns
            bricks.add(
                Brick(
                    x = startX + col * (brickWidth + padding),
                    y = row * (brickHeight + padding) + 50f,
                    width = brickWidth,
                    height = brickHeight,
                    color = if (isStone) Color.Gray else colors[row],
                    points = if (isStone) 100 else points[row],
                    hitPoints = if (isStone) 3 else 1 // Stone bricks require 3 hits
                )
            )
        }
    }
    return bricks
}
