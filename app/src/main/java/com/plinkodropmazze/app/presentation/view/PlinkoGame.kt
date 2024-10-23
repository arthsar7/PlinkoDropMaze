package com.plinkodropmazze.app.presentation.view

import android.graphics.Paint
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.graphics.drawscope.Circle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.ancient.flow.game.presentation.navigation.Screen
import com.pinrushcollect.app.data.Prefs
import com.plinkodropmazze.app.R
import com.plinkodropmazze.app.ui.theme.Typography
import kotlin.random.Random

@Composable
fun PlinkoGame(onNext: (Screen) -> Unit) {
    var coinCount by remember { mutableStateOf(0.0) }
    var ballPosition by remember { mutableStateOf<Pair<Float, Float>?>(null) }
    var isBallDropping by remember { mutableStateOf(false) }
    var winningSlot by remember { mutableStateOf<Int?>(null) }
    var nowcoinCount by remember { mutableStateOf(Prefs.coin) }

    // Coefficient mapping based on the number of balls at the bottom
    val coefficientsMap = mapOf(
        9 to listOf(3f, 1f, 0.4f, 0.2f, 1f, 0.2f, 0.4f, 1f, 3f),
        11 to listOf(4f, 2f, 1f, 0.5f, 0.2f, 1f, 0.2f, 0.5f, 1f, 2f, 4f),
        13 to listOf(5f, 3f, 2f, 1f, 0.5f, 0.2f, 1f, 0.2f, 0.5f, 1f, 2f, 3f, 5f),
        15 to listOf(7f, 4f, 3f, 2f, 1f, 0.5f, 0.2f, 1f, 0.2f, 0.5f, 1f, 2f, 3f, 4f, 7f),
        17 to listOf(9f, 5f, 4f, 3f, 2f, 1f, 0.5f, 0.2f, 1f, 0.2f, 0.5f, 1f, 2f, 3f, 4f, 5f, 9f)
    )
    val rows = mapOf(
        9 to 7, 11 to 9, 13 to 11, 15 to 13, 17 to 15
    )
    BackHandler {
        onNext(Screen.MainMenuScreen)
    }
    // State to hold current number of balls
    var numberOfBalls by remember { mutableStateOf(9) } // Default to 9 balls
    var isShowSettings by remember { mutableStateOf(false) }
    var isShowInfo by remember { mutableStateOf(false) }

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
                ), contentScale = ContentScale.FillBounds
            ),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TopBar2(onSettings = { isShowSettings = true }, onShop = { },nowcoinCount = nowcoinCount)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            rows[numberOfBalls]?.let {
                PlinkoBoard(
                    ballPosition = ballPosition,
                    winningSlot = winningSlot,
                    rows = it,
                    cols = numberOfBalls,
                    slotCoefficients = coefficientsMap[numberOfBalls]!!

                )
            }
        }

        // Button to change the structure of the board
        val style = LocalTextStyle.current
        BottomBar(
            onPut = {
                Prefs.coin += coinCount.toInt()
                nowcoinCount = Prefs.coin
                coinCount = 0.0
            },
            onLeft = {},
            onRight = {},
            onInfo = {isShowInfo = true},
            onField = {
                if (!isBallDropping) {
                    numberOfBalls = when (numberOfBalls) {
                        9 -> 11
                        11 -> 13
                        13 -> 15
                        15 -> 17
                        else -> 9
                    }
                }

            },
            onThrow = {
                if (!isBallDropping) {
                    Prefs.coin -= 20
                    nowcoinCount = Prefs.coin
                    ballPosition = Pair(0.5f, 0f)
                    isBallDropping = true
                    winningSlot = null
                }
            },
            coinCount = coinCount
        )

    }

    if (isShowInfo) {
        InfoDialog( onOk = { isShowInfo = false })
    }
    if (isShowSettings) {
        SettingsDialog(onDismiss = { isShowSettings = false })
    }
    LaunchedEffect(isBallDropping) {
        if (isBallDropping) {
            while ((ballPosition?.second ?: 1f) < 0.9f) {
                ballPosition = ballPosition?.let {
                    Pair(it.first + Random.nextFloat() * 0.05f - 0.025f, it.second + 0.02f)
                }
                kotlinx.coroutines.delay(50)
            }
            winningSlot = ((ballPosition?.first ?: 0.5f) * numberOfBalls).toInt()
            coinCount += (20.toFloat() * coefficientsMap[numberOfBalls]?.get(winningSlot!!)!!)


            isBallDropping = false
        }
    }
}
@Composable
fun TopBar2(
    modifier: Modifier = Modifier,
    onSettings: () -> Unit,
    onShop: () -> Unit,
    nowcoinCount: Int
) {

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onSettings,
            modifier = Modifier.size(100.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.settings_btn),
                contentDescription = null,
                modifier = Modifier
                    .width(80.dp)
                    .height(80.dp)
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 4.dp)
                .height(70.dp)
                .paint(
                    painter = painterResource(id = R.drawable.balance_bar),
                    contentScale = ContentScale.FillBounds
                )
        ) {
            Text(text = nowcoinCount.toString())
            Image(
                painter = painterResource(id = R.drawable.coin),
                contentDescription = null,
                modifier = Modifier
                    .width(40.dp)
                    .height(40.dp)
            )
        }

        IconButton(
            onClick = onShop,
            modifier = Modifier.size(100.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.shop_btn),
                contentDescription = null,
                modifier = Modifier
                    .width(80.dp)
                    .height(80.dp)
            )
        }
    }
}

@Composable
fun BottomBar(
    onField: () -> Unit,
    onThrow: () -> Unit,
    onLeft: () -> Unit,
    onRight: () -> Unit,
    onPut: () -> Unit,
    onInfo: () -> Unit,
    coinCount: Double = 0.0
) {
    var isLeftPressed by remember { mutableStateOf(true) }
    var isRightPressed by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()

        ) {

            Image(
                painter = painterResource(id = R.drawable.field),
                contentDescription = null,
                modifier = Modifier
                    .width(110.dp)
                    .height(60.dp)
                    .clickable { onField() }
            )


            Image(
                painter = painterResource(id = R.drawable.th),
                contentDescription = null,
                modifier = Modifier
                    .width(110.dp)
                    .height(60.dp)
                    .clickable { onThrow() }
            )

            Box(Modifier.size(70.dp)) {
                Image(
                    painter = painterResource(id = if (isLeftPressed) R.drawable.left else R.drawable.passive),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .width(70.dp)
                        .height(70.dp)
                        .clickable {
                            if (!isLeftPressed) {
                                onLeft()
                                isRightPressed = false
                                isLeftPressed = true
                            }
                        }
                )
            }


            Box(Modifier.size(70.dp)) {
                Image(
                    painter = painterResource(id = if (isRightPressed) R.drawable.right else R.drawable.passive),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .width(70.dp)
                        .height(70.dp)
                        .clickable {
                            if (!isRightPressed) {
                                isRightPressed = true
                                isLeftPressed = false
                                onRight()
                            }
                        }
                )
            }


        }
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp)
                    .height(70.dp)
                    .paint(
                        painter = painterResource(id = R.drawable.balance_bar),
                        contentScale = ContentScale.FillBounds
                    )
            ) {
                Text(text = coinCount.toInt().toString())
                Image(
                    painter = painterResource(id = R.drawable.coin),
                    contentDescription = null,
                    modifier = Modifier
                        .width(40.dp)
                        .height(40.dp)
                )
            }
            Image(
                painter = painterResource(id = R.drawable.put),
                contentDescription = null,
                modifier = Modifier
                    .width(110.dp)
                    .height(60.dp)
                    .clickable { onPut() }
            )

            Image(
                painter = painterResource(id = R.drawable.info),
                contentDescription = null,
                modifier = Modifier
                    .width(70.dp)
                    .height(70.dp)
                    .clickable { onInfo() }
            )

        }
    }
}

@Composable
fun PlinkoBoard(
    ballPosition: Pair<Float, Float>?,
    winningSlot: Int? = null,
    rows: Int = 13,  // Увеличенное количество рядов для завершения с 3 белыми колоннами
    cols: Int = 8,   // Ширина базы пирамиды (должна быть четной для симметрии)
    slotCoefficients: List<Any> = listOf(
        6.0, 4.0, 2.0, 0.5, 0.5, 2.0, 4.0, 6.0
    ) // Коэффициенты по умолчанию
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
                    x = col * slotWidth, y = size.height - 70.dp.toPx()
                ),
                size = androidx.compose.ui.geometry.Size(
                    slotWidth, 40.dp.toPx()
                ),  // Уменьшенная высота ячеек
            )
        }
        for (col in 0 until cols) {
            drawRect(
                color = if (winningSlot == col) Color.Green else Color.White,
                topLeft = androidx.compose.ui.geometry.Offset(
                    x = col * slotWidth, y = size.height - 70.dp.toPx()
                ),
                size = androidx.compose.ui.geometry.Size(
                    slotWidth, 40.dp.toPx()
                ),  // Уменьшенная высота ячеек
                style = Stroke(width = 5.dp.toPx())
            )
        }
        for (col in 0 until cols) {
            drawContext.canvas.nativeCanvas.drawText(adjustedSlotCoefficients[col].toString(),
                col * slotWidth + slotWidth / 2,
                size.height - 45.dp.toPx(),  // Уменьшенная позиция для текста
                Paint().apply {
                    color = android.graphics.Color.WHITE
                    textAlign = android.graphics.Paint.Align.CENTER
                    textSize = 14.sp.toPx()  // Уменьшенный размер текста
                    isFakeBoldText = true
                })
        }

        // Рисуем шар, если он падает
        ballPosition?.let {
            drawCircle(
                color = Color(0xFFFF09C2), radius = 15.dp.toPx(),  // Уменьшенный радиус шара
                center = androidx.compose.ui.geometry.Offset(
                    x = it.first * size.width, y = it.second * size.height
                )
            )
        }
    }
}


@Composable
fun InfoDialog(onOk: () -> Unit) {
    Dialog(onDismissRequest = { onOk() }) {
        // Box для кастомного фона диалога
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.4f)
                .clip(RoundedCornerShape(0.dp)),
            contentAlignment = Alignment.Center
        ) {
            // Фон — ваше изображение
            Image(
                painter = painterResource(id = R.drawable.panel_settings_bg), // Замените на ваше изображение
                contentDescription = null,
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.FillBounds
            )

            // Добавляем изображения сверху и снизу
            Box(modifier = Modifier.fillMaxSize()) {
                // Верхнее изображение


                // Контент диалога поверх изображения
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "INFO")
                    Text(
                        text = "The objective is to win by dropping the ball and waiting for it to land in the highest value slot at the bottom of the board. The payout is calculated based on the paytable, and the result is added to your balance. Payment amounts range from low to high.\n" + "Click the Throw button to release the ball."
                    )
                    Image(
                        painter = painterResource(id = R.drawable.ok),
                        contentDescription = null,
                        modifier = Modifier
                            .clickableNoRipple(onClick = { onOk() })
                    )


                }
            }
        }
    }
}