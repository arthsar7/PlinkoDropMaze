package com.plinkodropmazze.app.data

import android.content.Context
import android.content.SharedPreferences
import java.util.Calendar
import java.util.concurrent.TimeUnit

class DailyBonusManager(context: Context) {

    private val preferences: SharedPreferences = context.getSharedPreferences("daily_bonus_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val CLAIMED_TIMES_KEY = "claimed_times"
        private const val LAST_BONUS_TIME_KEY = "last_bonus_time"
        private const val BONUS_INTERVAL_HOURS = 6L // Интервал в 6 часов
    }

    // Сохранить собранные дни
    fun saveClaimedTimes(claimedTimes: Set<Long>) {
        preferences.edit()
            .putStringSet(CLAIMED_TIMES_KEY, claimedTimes.map { it.toString() }.toSet())
            .apply()
    }

    // Получить собранные бонусы (временные отметки)
    fun getClaimedTimes(): Set<Long> {
        val times = preferences.getStringSet(CLAIMED_TIMES_KEY, emptySet()) ?: emptySet()
        return times.map { it.toLong() }.toSet()
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

    // Проверить, можно ли снова собрать бонус
    fun canClaimBonus(): Boolean {
        val lastBonusTime = getLastBonusTime()
        val currentTime = System.currentTimeMillis()
        return (currentTime - lastBonusTime) >= TimeUnit.HOURS.toMillis(BONUS_INTERVAL_HOURS)
    }

    // Новый метод: проверка, был ли бонус собран за последние 6 часов
    fun isBonusClaimedInLastSixHours(): Boolean {
        val lastBonusTime = getLastBonusTime()

        if (lastBonusTime == 0L) {
            return false // Если бонус ни разу не был собран
        }

        val currentTime = System.currentTimeMillis()
        // Проверяем, прошли ли 6 часов с момента последнего бонуса
        return (currentTime - lastBonusTime) < TimeUnit.HOURS.toMillis(BONUS_INTERVAL_HOURS)
    }
}

