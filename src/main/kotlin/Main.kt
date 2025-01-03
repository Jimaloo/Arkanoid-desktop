import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import kotlinx.coroutines.delay
import kotlin.math.*

data class Ball(
    var x: Float,
    var y: Float,
    var angle: Float,
    val radius: Float = 8f,
    var speed: Float = 7f
)

data class Paddle(
    var x: Float,
    val y: Float,
    val width: Float = 100f,
    val height: Float = 15f
)

data class Brick(
    val x: Float,
    val y: Float,
    val width: Float = 60f,
    val height: Float = 25f,
    var isBroken: Boolean = false,
    val color: Color,
    val points: Int
)

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

@Composable
@Preview
fun App() {
    MaterialTheme {
        val density = LocalDensity.current
        val screenWidthPx = remember { with(density) { 800.dp.toPx() } }
        val screenHeightPx = remember { with(density) { 600.dp.toPx() } }
        var showLoading by remember { mutableStateOf(true) }
        var ball by remember {
            mutableStateOf(
                Ball(
                    screenWidthPx / 2,
                    screenHeightPx / 2,
                    (-PI / 4).toFloat()
                )
            )
        }
        var paddle by remember {
            mutableStateOf(
                Paddle(screenWidthPx / 2 - 50f, screenHeightPx - 50f)
            )
        }
        var bricks by remember { mutableStateOf(createBricks(screenWidthPx, screenHeightPx)) }
        var score by remember { mutableStateOf(0) }
        var isGameOver by remember { mutableStateOf(false) }
        var lives by remember { mutableStateOf(2) }
        var isPaused by remember { mutableStateOf(false) } // Pause state
        // For key press control
        val keyState = remember { mutableStateOf("") }
        val coroutineScope = rememberCoroutineScope()

        // Update paddle Y when screen size changes
        LaunchedEffect(keyState.value) {
            if (!isGameOver) {
                when (keyState.value) {
                    "LEFT" -> paddle = paddle.copy(
                        x = (paddle.x - 10f).coerceIn(0f, screenWidthPx - paddle.width)
                    )

                    "RIGHT" -> paddle = paddle.copy(
                        x = (paddle.x + 10f).coerceIn(0f, screenWidthPx - paddle.width)
                    )
                }
                delay(16) // Smooth movement
            }
        }

        LaunchedEffect(Unit) {
            while (true) {
                if (!isGameOver && !isPaused) {
                    val (newBall, broken) = updateBallPosition(
                        ball,
                        paddle,
                        bricks,
                        screenWidthPx,
                        screenHeightPx
                    )
                    ball = newBall

                    if (broken != null) {
                        bricks = bricks.map { if (it == broken) it.copy(isBroken = true) else it }
                        score += broken.points
                    }

                    if (ball.y + ball.radius > screenHeightPx) {
                        lives--
                        if (lives <= 0) {
                            isGameOver = true
                        } else {
                            ball = ball.copy(
                                x = screenWidthPx / 2,
                                y = screenHeightPx / 2,
                                angle = (-PI / 4).toFloat()
                            )
                        }
                    }
                }
                delay(16)
            }
        }
        if (showLoading) {
            // Show loading dialog for 2 seconds
            loadingDialog(onDismiss = { showLoading = false })
        } else {
            // Main game screen
            Column(modifier = Modifier.background(Color.Black)) {

                Column(
                    Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "SCORE: $score",
                            color = Color(0xFFFFFF00),
                            style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        )
                        Text(
                            "Pause",
                            color = Color(0xFFFFFF00),
                            style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                            modifier = Modifier.clickable {
                                isPaused = !isPaused
                            }
                        )


                        Text(
                            "LIVES: $lives",
                            color = Color(0xFFFF0000),
                            style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        )
                    }
                }

                Box(
                    Modifier.fillMaxSize()
                        .background(Color(0xFF1A1A1A)).onKeyEvent {
                            when {
                                it.key == Key.DirectionLeft -> {
                                    keyState.value = "LEFT"
                                    true
                                }

                                it.key == Key.DirectionRight -> {
                                    keyState.value = "RIGHT"
                                    true
                                }

                                it.key == Key.Spacebar -> { // Toggle pause with Spacebar
                                    isPaused = !isPaused
                                    true
                                }

                                else -> {
                                    println("key clicked = ${it.key}")
                                    false
                                }
                            }
                        }
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        // Draw background grid
                        for (i in 0..size.width.toInt() step 40) {
                            drawLine(
                                Color(0xFF2A2A2A),
                                Offset(i.toFloat(), 0f),
                                Offset(i.toFloat(), size.height),
                                1.dp.toPx()
                            )
                        }
                        for (i in 0..size.height.toInt() step 40) {
                            drawLine(
                                Color(0xFF2A2A2A),
                                Offset(0f, i.toFloat()),
                                Offset(size.width, i.toFloat()),
                                1.dp.toPx()
                            )
                        }

                        // Draw ball with glow effect
                        drawCircle(
                            Color.White.copy(alpha = 0.3f),
                            ball.radius * 1.5f,
                            Offset(ball.x, ball.y)
                        )
                        drawCircle(
                            Color.White,
                            ball.radius,
                            Offset(ball.x, ball.y)
                        )

                        // Draw paddle with gradient
                        val paddleGradient = Brush.horizontalGradient(
                            listOf(Color(0xFF00B4D8), Color(0xFF90E0EF))
                        )
                        drawRect(
                            paddleGradient,
                            Offset(paddle.x, paddle.y),
                            Size(paddle.width, paddle.height)
                        )

                        // Draw bricks with shadow effect
                        bricks.forEach { brick ->
                            if (!brick.isBroken) {
                                drawRect(
                                    Color.Black.copy(alpha = 0.2f),
                                    Offset(brick.x + 2, brick.y + 2),
                                    Size(brick.width, brick.height)
                                )
                                drawRect(
                                    brick.color,
                                    Offset(brick.x, brick.y),
                                    Size(brick.width, brick.height)
                                )
                            }
                        }
                    }

                    Box(
                        Modifier.fillMaxSize()
                            .pointerInput(Unit) {
                                detectHorizontalDragGestures { change, dragAmount ->
                                    paddle = paddle.copy(
                                        x = (paddle.x + dragAmount).coerceIn(0f, screenWidthPx - paddle.width)
                                    )
                                }
                            }
                    )



                    if (isGameOver) {
                        Box(
                            Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "GAME OVER",
                                    color = Color(0xFFFF00FF),
                                    style = TextStyle(fontSize = 48.sp, fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    "FINAL SCORE: $score",
                                    color = Color(0xFFFFFF00),
                                    style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold)
                                )

                                // Restart Button
                                Button(
                                    onClick = {
                                        ball = Ball(screenWidthPx / 2, screenHeightPx / 2, (-PI / 4).toFloat())
                                        paddle = Paddle(screenWidthPx / 2 - 50f, screenHeightPx - 50f)
                                        bricks = createBricks(screenWidthPx, screenHeightPx)
                                        score = 0
                                        isGameOver = false
                                        lives = 3
                                        isPaused = false
                                    },
                                    modifier = Modifier.padding(top = 16.dp),
                                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF00FFFF))
                                ) {
                                    Text("RESTART", color = Color.Black)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

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
            brokenBrick = brick
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
        }
    }

    return Pair(newBall, brokenBrick)
}

@Composable
fun loadingDialog(onDismiss: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(2000) // Show dialog for 2 seconds
        onDismiss()
    }

    Box(
        Modifier.fillMaxSize().background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "ARKANOID",
                color = Color.Cyan,
                style = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator(color = Color.Cyan)
        }
    }
}

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
            bricks.add(
                Brick(
                    x = startX + col * (brickWidth + padding),
                    y = row * (brickHeight + padding) + 50f,
                    width = brickWidth,
                    height = brickHeight,
                    color = colors[row],
                    points = points[row]
                )
            )
        }
    }
    return bricks
}

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Arkanoid POC",
        state = WindowState(width = 800.dp, height = 800.dp)
    ) {
        App()
    }
}