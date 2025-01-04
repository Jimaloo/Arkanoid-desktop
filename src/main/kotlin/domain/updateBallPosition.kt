package domain

import data.models.Ball
import data.models.Brick
import data.models.Paddle
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

fun updateBallPosition(
    ball: Ball,
    paddle: Paddle,
    bricks: List<Brick>,
    screenWidth: Float,
    screenHeight: Float
): Pair<Ball, Brick?> {
    var newBall = ball.copy(
        x = ball.x + ball.speed * cos(ball.angle),
        y = ball.y + ball.speed * sin(ball.angle)
    )
    var brokenBrick: Brick? = null

    // Wall collisions
    if (newBall.x - ball.radius < 0 || newBall.x + ball.radius > screenWidth) {
        newBall = newBall.copy(angle = PI.toFloat() - newBall.angle)
    }
    if (newBall.y - ball.radius < 0) {
        newBall = newBall.copy(angle = -newBall.angle)
    }

    // Paddle collision
    if (checkCollision(
            newBall.x, newBall.y, newBall.radius,
            paddle.x, paddle.y, paddle.width, paddle.height
        )
    ) {
        val hitPoint = (newBall.x - paddle.x) / paddle.width
        val angleOffset = (hitPoint - 0.5f) * PI.toFloat() / 3
        newBall = newBall.copy(
            angle = -PI.toFloat() / 2 + angleOffset,
            y = paddle.y - ball.radius
        )
    }

    // Brick collisions
    bricks.forEach { brick ->
        if (!brick.isBroken && checkCollision(
                newBall.x, newBall.y, newBall.radius,
                brick.x, brick.y, brick.width, brick.height
            )
        ) {
            val brickCenterX = brick.x + brick.width / 2
            val brickCenterY = brick.y + brick.height / 2

            // Determine if collision is more vertical or horizontal
            val dx = abs(newBall.x - brickCenterX)
            val dy = abs(newBall.y - brickCenterY)

            if (dx > dy) {
                newBall = newBall.copy(angle = PI.toFloat() - newBall.angle)
            } else {
                newBall = newBall.copy(angle = -newBall.angle)
            }

            // Handle brick hit
            if (brick.hitPoints > 1) {
                brick.hitPoints-- // Decrease hit points
            } else {
                brokenBrick = brick
                brick.isBroken = true // Mark as broken if hit points reach 0
            }
        }
    }

    return Pair(newBall, brokenBrick)
}