import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import data.models.Ball
import data.models.Paddle
import domain.createBricks
import domain.updateBallPosition
import kotlinx.coroutines.delay
import presentation.loadingDialog
import kotlin.math.PI

@Composable
@Preview
fun ArkanoidGame() {
    MaterialTheme {
        val density = LocalDensity.current
        val screenWidthPx = remember { with(density) { 800.dp.toPx() } }
        val screenHeightPx = remember { with(density) { 600.dp.toPx() } }
        var showLoading by remember { mutableStateOf(true) }
        var ball by remember {
            mutableStateOf(
                Ball(
                    screenWidthPx / 2, screenHeightPx / 2, (-PI / 4).toFloat()
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
        var gameStarted by remember { mutableStateOf(false) } // New state variable
        val focusRequester = remember { FocusRequester() }
        var hasFocus by remember { mutableStateOf(false) } // Track focus
        var paddleX by remember { mutableStateOf(paddle.x) } // Track paddle X separately



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
        // Request focus on composition and when window gains focus
        SideEffect {
            focusRequester.requestFocus()
        }
        LaunchedEffect(hasFocus) {
            if (hasFocus) {
                focusRequester.requestFocus()
            }
        }
//        LaunchedEffect(Unit) {
//            while (true) {
//                if (!isGameOver && !isPaused) {
//                    val (newBall, broken) = updateBallPosition(
//                        ball,
//                        paddle,
//                        bricks,
//                        screenWidthPx,
//                        screenHeightPx
//                    )
//                    ball = newBall
//
//                    if (broken != null) {
//                        bricks = bricks.map { if (it == broken) it.copy(isBroken = true) else it }
//                        score += broken.points
//                    }
//
//                    if (ball.y + ball.radius > screenHeightPx) {
//                        lives--
//                        if (lives <= 0) {
//                            isGameOver = true
//                        } else {
//                            ball = ball.copy(
//                                x = screenWidthPx / 2,
//                                y = screenHeightPx / 2,
//                                angle = (-PI / 4).toFloat()
//                            )
//                        }
//                    }
//                }
//                delay(16)
//            }
//        }
        LaunchedEffect(gameStarted) {
            if (gameStarted && !isGameOver) {
                while (true) {
                    if (isGameOver) {
                        break
                    }

                    val (newBall, broken) = updateBallPosition(
                        ball, paddle, bricks, screenWidthPx, screenHeightPx
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
                                x = screenWidthPx / 2, y = paddle.y - ball.radius, angle = (-PI / 4).toFloat()
                            )
                        }
                    }
                    delay(16)
                }
            }
        }

        Box(
            modifier = Modifier.background(Color.Black)
                .fillMaxSize()
                .onKeyEvent { event ->
                    if (!gameStarted) {
                        if (event.type == KeyEventType.KeyDown) {
                            gameStarted = true
                            return@onKeyEvent true
                        }
                    } else {
                        if (event.type == KeyEventType.KeyDown) {
                            when (event.key) {
                                Key.A, Key.DirectionLeft -> { // Added 'A' key
                                    paddle = paddle.copy(x = (paddle.x - 30f).coerceIn(0f, screenWidthPx - paddle.width))
                                    return@onKeyEvent true
                                }
                                Key.D, Key.DirectionRight -> { // Added 'D' key
                                    paddle = paddle.copy(x = (paddle.x + 30f).coerceIn(0f, screenWidthPx - paddle.width))
                                    return@onKeyEvent true
                                }
                                Key.Spacebar -> {
                                    isPaused = !isPaused
                                    return@onKeyEvent true
                                }
                                else -> return@onKeyEvent false // Important: handle other keys
                            }
                        }
                    }
                    false // Don't consume if not handled
                }
                .focusRequester(focusRequester)
                .focusable()
                .onFocusEvent { focusState ->
                    hasFocus = focusState.isFocused
                }
        ) {

            if (showLoading) {
                // Show loading dialog for 2 seconds
                loadingDialog(onDismiss = { showLoading = false })
            } else {
                // Main game screen

                    Column(
                        Modifier.fillMaxWidth().padding(1.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            Modifier.fillMaxWidth().padding(vertical = 10.dp), horizontalArrangement = Arrangement.SpaceBetween
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
                                })


                            Text(
                                "LIVES: $lives",
                                color = Color(0xFFFF0000),
                                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            )
                        }


                        Box(
                            Modifier.fillMaxSize().background(Color(0xFF1A1A1A)).onKeyEvent {
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
                            }) {

                            Canvas(modifier = Modifier.fillMaxSize()) {
                                // Draw background grid
//                        for (i in 0..size.width.toInt() step 40) {
//                            drawLine(
//                                Color(0xFF2A2A2A),
//                                Offset(i.toFloat(), 0f),
//                                Offset(i.toFloat(), size.height),
//                                1.dp.toPx()
//                            )
//                        }
//                        for (i in 0..size.height.toInt() step 40) {
//                            drawLine(
//                                Color(0xFF2A2A2A),
//                                Offset(0f, i.toFloat()),
//                                Offset(size.width, i.toFloat()),
//                                1.dp.toPx()
//                            )
//                        }

                                // Draw ball with glow effect
                                drawCircle(
                                    Color.White.copy(alpha = 0.3f), ball.radius * 1.5f, Offset(ball.x, ball.y)
                                )
                                drawCircle(
                                    Color.White, ball.radius, Offset(ball.x, ball.y)
                                )

                                // Draw paddle with gradient
                                val paddleGradient = Brush.horizontalGradient(
                                    listOf(Color(0xFF00B4D8), Color(0xFF90E0EF))
                                )

                                drawRect(
                                    paddleGradient, Offset(paddle.x, paddle.y), Size(paddle.width, paddle.height)
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
                                            brick.color, Offset(brick.x, brick.y), Size(brick.width, brick.height)
                                        )
                                    }
                                }
                            }

                            Box(
                                Modifier.fillMaxSize().pointerInput(Unit) {
                                    detectHorizontalDragGestures { change, dragAmount ->
                                        paddle = paddle.copy(
                                            x = (paddle.x + dragAmount).coerceIn(0f, screenWidthPx - paddle.width)
                                        )
                                    }
                                })

                            if (isGameOver) {
                                Box(
                                    Modifier.fillMaxSize(), contentAlignment = Alignment.Center
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

                if (!gameStarted) {
                    Box(
                        Modifier
                            .size(width = 350.dp, height = 100.dp) // Set specific size
                            .clip(RoundedCornerShape(10.dp)) // Round the corners
                            .background(Color.Magenta)
                            .align(Alignment.Center),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Press Any Key To Start!",
                            color = Color.White,
                            style = TextStyle(
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(6.dp)
                        )
                    }
                }

            }
        }
    }
}