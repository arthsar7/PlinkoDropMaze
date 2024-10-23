package com.ancient.flow.game.presentation.navigation


sealed class Screen(
    val screenRoute: String,
) {
    open val route: String = screenRoute


    data object MainMenuScreen : Screen("main_menu_screen")
    data object SettingsScreen : Screen("settings_screen")
    data object LevelScreen : Screen("level_screen")
    data object GameEndScreen : Screen("game_end_screen/{level}/{isVictory}")
    data object CrystalScreen : Screen("crystal_screen")
    data object WelcomeBonusScreen : Screen("welcome_bonus_screen")
    data object InfoScreen : Screen("info_screen")
    data object PurchaseScreen : Screen("purchase_screen") {
        fun createRoute(levelKey: String, painterResourceId: Int, levelCost: Int): String {
            return "$route/$levelKey/$painterResourceId/$levelCost"
        }
    }
    data object PinkoScreen : Screen("pinko_screen")
    data object WinScreen : Screen("win_screen/{level}")
    data object LoseScreen : Screen("lose_screen/{level}")


}