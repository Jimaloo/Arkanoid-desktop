package domain

// Helper functions for collision detection
fun checkCollision(
    ballX: Float, ballY: Float, ballRadius: Float,
    rectX: Float, rectY: Float, rectWidth: Float, rectHeight: Float
): Boolean {
    val closestX = ballX.coerceIn(rectX, rectX + rectWidth)
    val closestY = ballY.coerceIn(rectY, rectY + rectHeight)

    val distanceX = ballX - closestX
    val distanceY = ballY - closestY

    return (distanceX * distanceX + distanceY * distanceY) <= (ballRadius * ballRadius)
}