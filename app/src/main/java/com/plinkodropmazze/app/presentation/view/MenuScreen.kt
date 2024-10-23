package com.plinkodropmazze.app.presentation.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.pinrushcollect.app.data.Prefs
import com.plinkodropmazze.app.R
import com.plinkodropmazze.app.data.DailyBonusManager
import com.plinkodropmazze.app.data.SoundManager
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

@Composable
fun MenuScreen(
    onShop: () -> Unit,
    onPlay: () -> Unit,
    onPrivacy: () -> Unit,
    bonusManager: DailyBonusManager,
    onCrystalsCollected: () -> Unit
) {
    var isShowSettings by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) } // Показывает, открыт ли диалог
    Column(
        modifier = Modifier
            .appBg()
            .verticalScroll(rememberScrollState())
            .fillMaxSize()
            .then(
                if (isShowSettings) Modifier.blur(30.dp) else Modifier
            ),
        verticalArrangement = Arrangement.Top, // Устанавливаем элементы сверху вниз
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            TopBar(onSettings = { isShowSettings = true }, onShop ={ showDialog = true })
        }
        // Лого приложения
        Spacer(modifier = Modifier.height(40.dp))
        Image(
            painter = painterResource(id = R.drawable.plinko_logo),
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier.fillMaxWidth(0.9f)
        )
        Spacer(modifier = Modifier.height(40.dp))
        // Таймер обратного отсчета для кристаллов
        CrystalCollector(bonusManager, onCrystalsCollected, onBuyCrystal = { showDialog = true })
        Spacer(modifier = Modifier.height(40.dp))
        // Кнопка "Играть"
        Image(
            painter = painterResource(id = R.drawable.play_btn),
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clickableNoRipple { onPlay() }
        )

        // Кнопка политики конфиденциальности
        Image(
            painter = painterResource(id = R.drawable.privacy_policy_btn),
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clickableNoRipple { onPrivacy() }
        )
    }
    if (showDialog) {
        BuyCrystalDialog(onYes = {
            if (Prefs.coin >= 200) {
                Prefs.coin -= 200
                showDialog = false
                onCrystalsCollected()
            }

        }, onNo = { showDialog = false })
    }
    // Диалог настроек
    if (isShowSettings) {
        SettingsDialog(onDismiss = { isShowSettings = false })
    }
}

@Composable
fun CrystalCollector(
    bonusManager: DailyBonusManager,
    onCrystalsCollected: () -> Unit,
    onBuyCrystal: () -> Unit
) {
    var remainingTime by remember { mutableStateOf(TimeUnit.HOURS.toMillis(6) - (System.currentTimeMillis() - bonusManager.getLastBonusTime())) }
    val canClaimBonus by remember { derivedStateOf { bonusManager.canClaimBonus() } }


    LaunchedEffect(key1 = remainingTime) {
        if (!canClaimBonus) {
            delay(1000L) // обновление каждую секунду
            remainingTime -= 1000L
        }
    }
    if (canClaimBonus) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(horizontal = 4.dp)
                .paint(
                    painter = painterResource(id = R.drawable.collect_button),
                    contentScale = ContentScale.FillWidth
                )
                .clickable {
                    onCrystalsCollected()
                    //  bonusManager.saveLastBonusTime(System.currentTimeMillis()) // Обновить время последнего сбора
                    remainingTime = TimeUnit.HOURS.toMillis(6) // Сброс таймера на 6 часов
                }
        ) {

            // Кнопка для сбора кристаллов

        }
    } else {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(horizontal = 4.dp)
                .paint(
                    painter = painterResource(id = R.drawable.timer),
                    contentScale = ContentScale.FillWidth
                )
//                .clickable {
//                    onBuyCrystal()
//                }
        ) {

            // Кнопка для сбора кристаллов


            // Таймер обратного отсчета
            Text(
                text = formatTime(remainingTime),
                modifier = Modifier.padding(16.dp),
                fontSize = 32.sp
            )

        }
    }

}

// Форматирование времени в часы:минуты:секунды
fun formatTime(timeMillis: Long): String {
    val hours = TimeUnit.MILLISECONDS.toHours(timeMillis)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(timeMillis) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(timeMillis) % 60
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}


@Composable
fun SettingsDialog(
    onDismiss: () -> Unit
) {
    var musicValue by remember { mutableFloatStateOf(Prefs.musicVolume) }
    var soundValue by remember { mutableFloatStateOf(Prefs.soundVolume) }
    val list = remember {
        listOf(R.drawable.bg_1, R.drawable.bg_2, R.drawable.bg_3)
    }
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .size(400.dp)
        ) {
            Column(
                modifier = Modifier
                    .paint(
                        painter = painterResource(id = R.drawable.panel_settings_bg),
                        contentScale = ContentScale.Crop
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Settings",
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Text(text = "Music")
                Slider(value = musicValue, onValueChange = { musicValue = it
                    Prefs.musicVolume = musicValue
                    SoundManager.setMusicVolume()
                })
                Text(text = "Sound Effects")
                Slider(value = soundValue, onValueChange = { soundValue = it
                    Prefs.soundVolume = soundValue
                    SoundManager.setSoundVolume()
                })
                Text(text = "SELECT GAME PANEL")
                Row {
                    list.forEachIndexed { index, item ->
                        Image(
                            painter = painterResource(id = item),
                            contentDescription = null,
                            modifier = Modifier
                                .weight(1f)
                                .padding(3.dp)
                                .clickable {
                                    Prefs.bg = index// Присваиваем индекс элемента
                                }
                        )

                    }

                }

            }

            Image(
                painter = painterResource(id = R.drawable.menu_btn),
                contentDescription = null,
                modifier = Modifier
                    .clickableNoRipple(onClick = onDismiss)
            )
        }
    }
}

@Composable
fun BuyCrystalDialog(onYes: () -> Unit, onNo: () -> Unit) {
    Dialog(onDismissRequest = { /* Ничего не делаем, чтобы игрок не мог закрыть диалог снаружи */ }) {
        // Box для кастомного фона диалога
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.5f)
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

                    Text(
                        text = "Are you sure you want to buy a crystal for 200 coins?",
                        fontSize = 30.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Image(
                        painter = painterResource(id = R.drawable.yes),
                        contentDescription = null,
                        modifier = Modifier
                            .clickableNoRipple(onClick = onYes)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Image(
                        painter = painterResource(id = R.drawable.no),
                        contentDescription = null,
                        modifier = Modifier
                            .clickableNoRipple(onClick = onNo)
                    )


                }
            }
        }
    }
}

@Composable
fun TopBar(
    modifier: Modifier = Modifier,
    onSettings: () -> Unit,
    onShop: () -> Unit
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
            Text(text = Prefs.coin.toString())
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
fun Modifier.appBg() = then(
    Modifier
        .paint(
            painter = painterResource(id =
                when (Prefs.bg) {
                    0 -> R.drawable.bg_1_1
                    1 -> R.drawable.bg_2_2
                    2 -> R.drawable.bg_3_3
                    else -> R.drawable.bg_1_1
                }
            ),
            contentScale = ContentScale.Crop
        )
)

@Composable
fun Modifier.clickableNoRipple(onClick: () -> Unit) = then(
    Modifier.clickable(
        indication = null,
        interactionSource = remember { MutableInteractionSource() },
        onClick = onClick
    )
)
