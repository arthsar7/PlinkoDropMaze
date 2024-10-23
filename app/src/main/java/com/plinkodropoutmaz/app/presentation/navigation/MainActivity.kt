package com.plinkodropoutmaz.app.presentation.navigation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ancient.flow.game.presentation.navigation.Screen
import com.ancient.flow.game.presentation.navigation.navigatePopUpInclusive
import com.pinrushcollect.app.data.Prefs
import com.plinkodropoutmaz.app.data.DailyBonusManager
import com.plinkodropoutmaz.app.data.SoundManager
import com.plinkodropoutmaz.app.presentation.view.CrystalGameScreen
import com.plinkodropoutmaz.app.presentation.view.MenuScreen
import com.plinkodropoutmaz.app.presentation.view.PlinkoGame
import com.plinkodropoutmaz.app.ui.theme.PlinkoDropMazeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        Prefs.init(applicationContext)
        SoundManager.init(applicationContext)
        val bonusManager = DailyBonusManager(applicationContext)
        super.onCreate(savedInstanceState)
        setContent {
            PlinkoDropMazeTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination =
                    if (bonusManager.isBonusClaimedInLastSixHours()) Screen.MainMenuScreen.route else Screen.CrystalScreen.route
                ) {
                    composable(Screen.MainMenuScreen.route) {
                        MenuScreen(
                            onShop = { /*TODO*/ },
                            onPlay = { navController.navigatePopUpInclusive(Screen.PinkoScreen)},
                            onPrivacy = {},
                            bonusManager = bonusManager,
                            onCrystalsCollected = { navController.navigatePopUpInclusive(Screen.CrystalScreen) }
                        )
                    }
                    composable(Screen.CrystalScreen.route) {
                        CrystalGameScreen(navController::navigatePopUpInclusive, bonusManager)
                    }
                    composable(Screen.PinkoScreen.route){
                        PlinkoGame(navController::navigatePopUpInclusive)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        SoundManager.resumeMusic()
    }

    override fun onPause() {
        super.onPause()
        SoundManager.pauseMusic()
    }

    override fun onDestroy() {
        super.onDestroy()
        SoundManager.onDestroy()
    }
}