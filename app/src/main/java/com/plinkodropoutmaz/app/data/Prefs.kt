package com.pinrushcollect.app.data

import android.content.Context
import android.content.SharedPreferences

object Prefs {
    private lateinit var prefs: SharedPreferences

    private const val KEY_START_STEP_COMPLETED = "StartStepCompleted"
    private const val DEFAULT_COIN_VALUE = 1000 // Значение по умолчанию для "coin"
    private const val DEFAULT_BG_VALUE  = 1
    // Свойство для проверки шага "StartStepCompleted"
    var startStepCompleted: Boolean
        get() = prefs.getBoolean(KEY_START_STEP_COMPLETED, false) // false - значение по умолчанию
        set(value) {
            prefs.edit().putBoolean(KEY_START_STEP_COMPLETED, value).apply()
        }

    // Инициализация SharedPreferences
    fun init(context: Context) {
        prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    }

    // Свойство для монет с значением по умолчанию
    var coin: Int
        get() = prefs.getInt("coin", DEFAULT_COIN_VALUE) // DEFAULT_COIN_VALUE - значение по умолчанию
        set(value) {
            prefs.edit().putInt("coin", value).apply()
        }


    var bg : Int
        get() = prefs.getInt("bg", 0)
        set(value) {
            prefs.edit().putInt("bg", value).apply()
        }


    var musicVolume: Float
        get() = prefs.getFloat("musicVolume", 0.5f)
        set(value) = prefs.edit().putFloat("musicVolume", value).apply()
    var soundVolume: Float
        get() = prefs.getFloat("soundVolume", 0.5f)
        set(value) = prefs.edit().putFloat("soundVolume", value).apply()
}
