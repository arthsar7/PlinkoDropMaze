package com.plinkodropmazze.app.presentation.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.paint
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.pinrushcollect.app.data.Prefs
import com.plinkodropmazze.app.R

@Composable
fun MenuScreen(
    onShop: () -> Unit,
    onPlay: () -> Unit,
    onPrivacy: () -> Unit
) {
    var isShowSettings by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .appBg()
            .verticalScroll(rememberScrollState())
            .fillMaxSize()
            .then(
                if (isShowSettings) Modifier.blur(30.dp) else Modifier
            ),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TopBar(onSettings = { isShowSettings = true }, onShop = onShop)
        Image(
            painter = painterResource(id = R.drawable.plinko_logo),
            contentDescription = null,
            modifier = Modifier.size(300.dp)
        )
        Image(
            painter = painterResource(id = R.drawable.play_btn),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clickableNoRipple { onPlay() }
        )
        Image(
            painter = painterResource(id = R.drawable.privacy_policy_btn),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clickableNoRipple { onPrivacy() }
        )
    }

    if (isShowSettings) {
        SettingsDialog(onDismiss = { isShowSettings = false })
    }
}

@Composable
fun SettingsDialog(
    onDismiss: () -> Unit
) {
    var musicValue by remember { mutableFloatStateOf(0.5f) }
    var soundValue by remember { mutableFloatStateOf(0.5f) }
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
                Slider(value = musicValue, onValueChange = { musicValue = it })
                Text(text = "Sound Effects")
                Slider(value = soundValue, onValueChange = { soundValue = it })
                Text(text = "SELECT GAME PANEL")
                Row {
                    list.forEach {
                        Image(
                            painter = painterResource(id = it),
                            contentDescription = null,
                            modifier = Modifier
                                .weight(1f)
                                .padding(3.dp)
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
            painter = painterResource(id = R.drawable.bg),
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

@Preview
@Composable
fun MenuScreenPreview() {
    MenuScreen(onShop = {}, onPlay = {}, onPrivacy = {})
}