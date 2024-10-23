package com.plinkodropmazze.app.presentation.view

import android.graphics.Paint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.*
import androidx.compose.material3.Button
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.graphics.drawscope.Circle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pinrushcollect.app.data.Prefs
import com.plinkodropmazze.app.R
import com.plinkodropmazze.app.ui.theme.Typography
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun PlinkoGame() {
    var ballPosition by remember { mutableStateOf<Pair<Float, Float>?>(null) }
    var isBallDropping by remember { mutableStateOf(false) }
    var winningSlot by remember { mutableStateOf<Int?>(null) }

    // Coefficient mapping based on the number of balls at the bottom
    val coefficientsMap = mapOf(
        9 to listOf(3, 1, 0.4, 0.2, 1, 0.2, 0.4, 1, 3),
        11 to listOf(4, 2, 1, 0.5, 0.2, 1, 0.2, 0.5, 1, 2, 4),
        13 to listOf(5, 3, 2, 1, 0.5, 0.2, 1, 0.2, 0.5, 1, 2, 3, 5),
        15 to listOf(7, 4, 3, 2, 1, 0.5, 0.2, 1, 0.2, 0.5, 1, 2, 3, 4, 7),
        17 to listOf(9, 5, 4, 3, 2, 1, 0.5, 0.2, 1, 0.2, 0.5, 1, 2, 3, 4, 5, 9)
    )


    // State to hold current number of balls
    var numberOfBalls by remember { mutableStateOf(9) } // Default to 9 balls
    var isShowSettings by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .paint(
                painter = painterResource(
                    id = when (Prefs.bg + 1) {
                        1 -> R.drawable.bg_1_1
                        2 -> R.drawable.bg_2_2
                        3 -> R.drawable.bg_3_3
                        else -> R.drawable.bg_1_1
                    }
                ),
                contentScale = ContentScale.FillBounds
            ),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TopBar(onSettings = { isShowSettings = true }, onShop = { })
        Text(
            text = "Plinko Game",
            style = Typography.headlineMedium,
            modifier = Modifier.padding(8.dp),
            color = Color.White
        )
        Text(
            text = if (winningSlot != null) {
                "${coefficientsMap[numberOfBalls]?.get(winningSlot!!)}x"
            } else {
                "Drop the ball to play!"
            },
            style = Typography.headlineMedium,
            modifier = Modifier.padding(8.dp),
            color = Color.White
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            PlinkoBoard(ballPosition = ballPosition, winningSlot = winningSlot, rows = 10, cols = numberOfBalls)
        }

        Button(
            onClick = {
                ballPosition = Pair(0.5f, 0f)
                isBallDropping = true
                winningSlot = null
            },
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Drop Ball")
        }

        // Button to change the structure of the board
        val style = LocalTextStyle.current
        Button(
            onClick = {
                // Cycle through different numbers of balls
                numberOfBalls = when (numberOfBalls) {
                    9 -> 11
                    11 -> 13
                    13 -> 15
                    15 -> 17
                    else -> 9
                }
            },
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Change Number of Balls",
                style = style
                )
        }
    }
    if (isShowSettings) {
        SettingsDialog(onDismiss = { isShowSettings = false })
    }
    LaunchedEffect(isBallDropping) {
        if (isBallDropping) {
            while ((ballPosition?.second ?: 1f) < 0.9f) {
                ballPosition = ballPosition?.let {
                    Pair(it.first + Random.nextFloat() * 0.1f - 0.05f, it.second + 0.02f)
                }
                kotlinx.coroutines.delay(50)
            }
            winningSlot = ((ballPosition?.first ?: 0.5f) * numberOfBalls).toInt()

            isBallDropping = false
        }
    }
}
@Composable
fun PlinkoBoard(
    ballPosition: Pair<Float, Float>?,
    winningSlot: Int? = null,
    rows: Int = 13,  // Увеличенное количество рядов для завершения с 3 белыми колоннами
    cols: Int = 8,   // Ширина базы пирамиды (должна быть четной для симметрии)
    slotCoefficients: List<Double> = listOf(6.0, 4.0, 2.0, 0.5, 0.5, 2.0, 4.0, 6.0) // Коэффициенты по умолчанию
) {
    // Обеспечьте, чтобы slotCoefficients содержал по крайней мере 'cols' элементов
    val adjustedSlotCoefficients = if (slotCoefficients.size < cols) {
        slotCoefficients + List(cols - slotCoefficients.size) { 0.0 } // Заполнение нулями или любым значением по умолчанию
    } else {
        slotCoefficients
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val pegRadius = 5.dp.toPx()  // Уменьшенный радиус пинов
        val pegSpacing = size.width / (cols + 1)

        // Рисуем инвертированные пины пирамиды
        for (row in 0 until rows) {
            val currentCols = cols - row // Уменьшение числа колонн для каждого ряда
            for (col in 0 until currentCols) {
                // Вычисление позиции x для центрирования инвертированной пирамиды
                val xPosition = (col + 1) * pegSpacing + (row * pegSpacing / 2)
                drawCircle(
                    color = Color.White,
                    radius = pegRadius,
                    center = androidx.compose.ui.geometry.Offset(
                        x = xPosition,
                        y = size.height - ((row + 1) * pegSpacing * 1.5f) // Инвертированная позиция y
                    )
                )
            }
        }

        // Рисуем выигрышные слоты
        val slotWidth = size.width / cols
        for (col in 0 until cols) {
            drawRect(
                color = Color.Blue,
                topLeft = androidx.compose.ui.geometry.Offset(
                    x = col * slotWidth,
                    y = size.height - 70.dp.toPx()
                ),
                size = androidx.compose.ui.geometry.Size(slotWidth, 40.dp.toPx()),  // Уменьшенная высота ячеек
            )
        }
        for (col in 0 until cols) {
            drawRect(
                color = if (winningSlot == col) Color.Green else Color.White,
                topLeft = androidx.compose.ui.geometry.Offset(
                    x = col * slotWidth,
                    y = size.height - 70.dp.toPx()
                ),
                size = androidx.compose.ui.geometry.Size(slotWidth, 40.dp.toPx()),  // Уменьшенная высота ячеек
                style = Stroke(width = 5.dp.toPx())
            )
        }
        for (col in 0 until cols) {
            drawContext.canvas.nativeCanvas.drawText(
                adjustedSlotCoefficients[col].toString(),
                col * slotWidth + slotWidth / 2,
                size.height - 25.dp.toPx(),  // Уменьшенная позиция для текста
                Paint().apply {
                    color = android.graphics.Color.WHITE
                    textAlign = android.graphics.Paint.Align.CENTER
                    textSize = 14.sp.toPx()  // Уменьшенный размер текста
                    isFakeBoldText = true
                }
            )
        }

        // Рисуем последние 3 белые колонки внизу
        for (col in (cols - 3) until cols) {
            val xPosition = (col + 1) * pegSpacing
            drawCircle(
                color = Color.White,
                radius = pegRadius,
                center = androidx.compose.ui.geometry.Offset(
                    x = xPosition,
                    y = size.height - ((rows + 1) * pegSpacing * 1.5f) // Нижний ряд
                )
            )
        }

        // Рисуем шар, если он падает
        ballPosition?.let {
            drawCircle(
                color = Color(0xFFFF09C2),
                radius = 15.dp.toPx(),  // Уменьшенный радиус шара
                center = androidx.compose.ui.geometry.Offset(
                    x = it.first * size.width,
                    y = it.second * size.height
                )
            )
        }
    }
}



//@Composable
//fun PlinkoGame() {
//    var ballY by remember { mutableStateOf(0f) }
//    var ballX by remember { mutableStateOf(0f) }
//    var isFalling by remember { mutableStateOf(false) }
//    var score by remember { mutableStateOf(0) }
//
//    val coroutineScope = rememberCoroutineScope()
//    BackHandler {
//
//    }
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            // .background(Color(0xFF002366))
//            .paint(
//                painter = painterResource(
//                    id = when (Prefs.bg) {
//                        1 -> R.drawable.bg_1_1
//                        2 -> R.drawable.bg_2_2
//                        3 -> R.drawable.bg_3_3
//                        else -> R.drawable.bg_1_1
//                    }
//                ),
//                contentScale = ContentScale.FillBounds
//            ), // Синий фон
//        contentAlignment = Alignment.TopCenter
//    ) {
//        // Игровое поле
//        PlinkoBoard()
//
//        // Шарик
//        Ball(x = ballX, y = ballY)
//
//        // Панель для отображения очков
//        BasicText(
//            text = "Score: $score",
//            modifier = Modifier
//                .align(Alignment.TopCenter)
//                .padding(top = 16.dp),
//            style = _root_ide_package_.com.plinkodropmazze.app.ui.theme.Typography.bodyMedium.copy(
//                color = Color.White,
//                fontWeight = FontWeight.Bold
//            )
//        )
//
//        // Кнопка для начала игры
//        Button(
//            onClick = {
//                if (!isFalling) {
//                    isFalling = true
//                    coroutineScope.launch {
//                        startPlinko(ballX, ballY, onPositionChange = { newX, newY ->
//                            ballX = newX
//                            ballY = newY
//                        }, onHitBottom = { points ->
//                            score += points
//                            isFalling = false
//                        })
//                    }
//                }
//            },
//            modifier = Modifier
//                .align(Alignment.BottomCenter)
//                .padding(16.dp)
//        ) {
//            BasicText(text = "Drop Ball")
//        }
//    }
//}
//
//// Отрисовка игрового поля с препятствиями и слотами для очков
//@Composable
//fun PlinkoBoard() {
//    Canvas(modifier = Modifier.fillMaxSize()) {
//        drawPins()
//        drawSlots()
//    }
//}
//
//// Отрисовка штырей в форме треугольника
//fun DrawScope.drawPins() {
//    val rows = 6
//    val pinRadius = 10f
//    val startX = size.width / 2
//    val spacing = size.width / 8
//
//    for (row in 0 until rows) {
//        for (col in 0..row) {
//            val offsetX = startX - (row * spacing / 2) + (col * spacing)
//            val offsetY = (row + 1) * spacing
//            drawCircle(Color.White, radius = pinRadius, center = Offset(offsetX, offsetY))
//        }
//    }
//}
//
//// Отрисовка слотов с мультипликаторами
//fun DrawScope.drawSlots() {
//    val slots = listOf(3f, 1f, 0.4f, 0.2f, 0.2f, 0.4f, 1f, 3f)
//    val slotWidth = size.width / slots.size
//
//    for (i in slots.indices) {
//        val slotX = i * slotWidth
//        val slotY = size.height - 80f
//        drawRect(
//            color = Color(0xFFE91E63), // Розовые слоты
//            topLeft = Offset(slotX, slotY),
//            size = androidx.compose.ui.geometry.Size(slotWidth, 80f),
//            style = Stroke(5f)
//        )
//        drawText(
//            text = "${slots[i]}x",
//            fontSize = 24,
//            color = Color.White,
//            centerX = slotX + slotWidth / 2,
//            centerY = slotY + 40f
//        )
//    }
//}
//
//// Отрисовка шарика
//@Composable
//fun Ball(x: Float, y: Float) {
//    Box(
//        modifier = Modifier
//            .offset(x.dp, y.dp)
//            .size(30.dp)
//            .background(Color.Magenta, shape = CircleShape)
//    )
//}
//
//// Анимация падения шарика
//suspend fun startPlinko(
//    startX: Float,
//    startY: Float,
//    onPositionChange: (Float, Float) -> Unit,
//    onHitBottom: (Int) -> Unit
//) {
//    var x = startX
//    var y = startY
//    val speed = 5f
//    val width = 500f // ширина поля (заменить на актуальные размеры)
//    val height = 800f // высота поля (заменить на актуальные размеры)
//    var direction = 1 // направление движения
//
//    while (y < height) {
//        delay(16L) // Задержка для плавной анимации (60fps)
//
//        y += speed
//        if (Math.random() > 0.5) {
//            direction = -direction
//        }
//
//        // Ограничиваем движение по горизонтали
//        x += direction * (Math.random().toFloat() * 10)
//        x = x.coerceIn(0f, width)
//
//        // Обновляем позицию шарика
//        onPositionChange(x, y)
//    }
//
//    // Попадание в лунку
//    val score = when {
//        x < width / 8 -> 3
//        x < width * 2 / 8 -> 1
//        x < width * 3 / 8 -> 0.4f.toInt()
//        x < width * 4 / 8 -> 0.2f.toInt()
//        x < width * 5 / 8 -> 0.2f.toInt()
//        x < width * 6 / 8 -> 0.4f.toInt()
//        x < width * 7 / 8 -> 1
//        else -> 3
//    }
//
//    onHitBottom(score)
//}
//
//// Вспомогательная функция для текста внутри слотов
//fun DrawScope.drawText(text: String, fontSize: Int, color: Color, centerX: Float, centerY: Float) {
//    drawContext.canvas.nativeCanvas.drawText(
//        text, centerX, centerY, android.graphics.Paint().apply {
//            this.color = android.graphics.Color.WHITE
//            this.textAlign = android.graphics.Paint.Align.CENTER
//            this.textSize = fontSize.toFloat()
//        }
//    )
//}