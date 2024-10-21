package com.plinkodropmazze.app.data

import android.content.Context
import android.content.SharedPreferences
import java.util.Calendar
import java.util.concurrent.TimeUnit

class DailyBonusManager(context: Context) {

    private val preferences: SharedPreferences = context.getSharedPreferences("daily_bonus_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val CLAIMED_DAYS_KEY = "claimed_days"
        private const val LAST_BONUS_TIME_KEY = "last_bonus_time"
    }

    // Сохранить собранные дни
    fun saveClaimedDays(claimedDays: Set<Int>) {
        preferences.edit()
            .putStringSet(CLAIMED_DAYS_KEY, claimedDays.map { it.toString() }.toSet())
            .apply()
    }

    // Получить собранные дни
    fun getClaimedDays(): Set<Int> {
        val days = preferences.getStringSet(CLAIMED_DAYS_KEY, emptySet()) ?: emptySet()
        return days.map { it.toInt() }.toSet()
    }

    // Сохранить время последнего бонуса
    fun saveLastBonusTime(time: Long) {
        preferences.edit()
            .putLong(LAST_BONUS_TIME_KEY, time)
            .apply()
    }

    // Получить время последнего бонуса
    fun getLastBonusTime(): Long {
        return preferences.getLong(LAST_BONUS_TIME_KEY, 0L)
    }

    // Проверить, прошел ли день с момента последнего бонуса
    fun canClaimBonus(): Boolean {
        val lastBonusTime = getLastBonusTime()
        val currentTime = System.currentTimeMillis()
        return (currentTime - lastBonusTime) >= TimeUnit.HOURS.toMillis(6)
    }

    // Новый метод: проверка, был ли бонус уже собран сегодня
    fun isBonusClaimedToday(): Boolean {
        val lastBonusTime = getLastBonusTime()

        if (lastBonusTime == 0L) {
            return false // Если бонус ни разу не был собран
        }

        val calendar = Calendar.getInstance()
        val today = calendar.timeInMillis

        // Настроить календарь для последнего времени бонуса
        val lastBonusCalendar = Calendar.getInstance().apply {
            timeInMillis = lastBonusTime
        }


        // Сравниваем даты: год, месяц и день должны совпадать
        return (calendar.get(Calendar.YEAR) == lastBonusCalendar.get(Calendar.YEAR) &&
                calendar.get(Calendar.DAY_OF_YEAR) == lastBonusCalendar.get(Calendar.DAY_OF_YEAR))
    }
}
