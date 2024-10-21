package com.plinkodropmazze.app.presentation.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.ancient.flow.game.presentation.navigation.Screen
import com.pinrushcollect.app.data.Prefs
import com.plinkodropmazze.app.R
import com.plinkodropmazze.app.data.DailyBonusManager
import kotlin.random.Random


@Composable
fun CrystalGameScreen(onNext:(Screen ) -> Unit,dailyBonusManager: DailyBonusManager) {
    var lastBonusTime by remember { mutableStateOf(dailyBonusManager.getLastBonusTime()) }
    var selectedCrystal by remember { mutableStateOf(-1) }
    var winningAmount by remember { mutableStateOf(0) }
    var isCrystalSelected by remember { mutableStateOf(dailyBonusManager.isBonusClaimedToday()) } // Переменная для блокировки
    var showDialog by remember { mutableStateOf(false) } // Показывает, открыт ли диалог
    val crystalImage =
        painterResource(id = R.drawable.crystal) // Замените на ваше изображение кристалла
    val gridBackground =
        painterResource(id = R.drawable.panel_settings_bg) // Замените на ваше фоновое изображение

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(id = R.drawable.bg),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(340.dp, 450.dp)
                    .clip(RoundedCornerShape(0.dp))
                    .padding(8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = gridBackground,
                    contentDescription = "Grid Background",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.fillMaxSize()
                )

                // Выбор кристаллов
                GridOfCrystals(
                    crystalImage = crystalImage,
                    selectedCrystal = selectedCrystal,
                    onCrystalClick = { index ->
                        if (!isCrystalSelected) {
                            selectedCrystal = index
                            val winAmounts = listOf(100, 200, 300, 400, 500)
                            val coinsWon = winAmounts.random()
                            winningAmount = coinsWon
                            Prefs.coin += coinsWon
                            lastBonusTime = System.currentTimeMillis()
                            dailyBonusManager.saveLastBonusTime(lastBonusTime)
                            isCrystalSelected = true // Блокируем повторный выбор кристалла
                            showDialog = true // Показываем диалог
                        }
                    }
                )
            }
        }
        Box(modifier = Modifier.align(Alignment.BottomCenter)) {
            Text(
                text = "Pick the Crystal",
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        if(showDialog) {
            Image(
                painter = painterResource(id = R.drawable.top_image), // Ваше изображение сверху
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth(), // Размер изображения
                contentScale = ContentScale.Crop
            )

            // Нижнее изображение
            Image(
                painter = painterResource(id = R.drawable.bottom_image), // Ваше изображение снизу
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(), // Размер изображения
                contentScale = ContentScale.Crop
            )
        }

        // Диалог с информацией о выигрыше
        if (showDialog) {
            WinDialog(winningAmount) {
                showDialog = false
                onNext(Screen.MainMenuScreen)
            }
        }
    }
}
@Composable
fun WinDialog(winningAmount: Int, onContinue: () -> Unit) {
    Dialog(onDismissRequest = { /* Ничего не делаем, чтобы игрок не мог закрыть диалог снаружи */ }) {
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = "+$winningAmount",
                            fontSize = 90.sp,
                            color = Color.White
                        )
                        Image(
                            painter = painterResource(id = R.drawable.coin),
                            contentDescription = null,
                            modifier = Modifier
                                .width(90.dp)
                                .height(90.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Image(
                        painter = painterResource(id = R.drawable.button), contentDescription = null,
                        modifier = Modifier
                            .clickableNoRipple(onClick = onContinue)
                    )


                }
            }
        }
    }
}

//@Composable
//fun WinDialog(winningAmount: Int, onContinue: () -> Unit) {
//    Dialog(onDismissRequest = { /* Ничего не делаем, чтобы игрок не мог закрыть диалог снаружи */ }) {
//        // Box для кастомного фона диалога
//        Box(
//            modifier = Modifier
//                .fillMaxWidth()
//                .fillMaxHeight(0.4f)
//                .clip(RoundedCornerShape(0.dp)),
//            contentAlignment = Alignment.Center
//        ) {
//            // Фон — ваше изображение
//            Image(
//                painter = painterResource(id = R.drawable.panel_settings_bg), // Замените на ваше изображение
//                contentDescription = null,
//                modifier = Modifier.fillMaxWidth(),
//                contentScale = ContentScale.FillBounds
//            )
//
//            // Контент диалога поверх изображения
//            Column(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(16.dp),
//                verticalArrangement = Arrangement.Center,
//                horizontalAlignment = Alignment.CenterHorizontally
//            ) {
//                Row(
//                    verticalAlignment = Alignment.CenterVertically,
//                    horizontalArrangement = Arrangement.Center,
//                ) {
//                    Text(
//                        text = "+$winningAmount",
//                        fontSize = 90.sp,
//                        color = Color.White
//                    )
//                    Image(
//                        painter = painterResource(id = R.drawable.coin),
//                        contentDescription = null,
//                        modifier = Modifier
//                            .width(90.dp)
//                            .height(90.dp)
//                    )
//                }
//
//                Spacer(modifier = Modifier.height(16.dp))
//                Image(
//                    painter = painterResource(id = R.drawable.button), contentDescription = null,
//                    modifier = Modifier
//                        .clickableNoRipple(onClick = onContinue)
//                )
//
//
//            }
//        }
//    }
//}


@Composable
fun GridOfCrystals(
    crystalImage: Painter,
    selectedCrystal: Int,
    onCrystalClick: (Int) -> Unit
) {
    Column {
        for (row in 0 until 3) {
            Row {
                for (col in 0 until 3) {
                    val index = row * 3 + col
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .size(80.dp)
                            .clickable { onCrystalClick(index) },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = crystalImage,
                            contentDescription = "Crystal",
                            modifier = Modifier.fillMaxSize(),
                            colorFilter = if (selectedCrystal == index)
                                ColorFilter.tint(Color.Yellow)
                            else
                                null
                        )
                    }
                }
            }
        }
    }
}

