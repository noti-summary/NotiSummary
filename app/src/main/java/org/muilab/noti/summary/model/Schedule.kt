package org.muilab.noti.summary.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Calendar

@Entity(tableName = "time_events")
data class Schedule(
    @PrimaryKey(autoGenerate = true)
    var primaryKey: Int = 0,
    var hour: Int,
    var minute: Int,
    var week: Int = 0b1111111
    // 7-bit binary number for the week, with each bit representing a day from Monday to Sunday.
) {
    private val everyDay = 0b1111111
    private val everyWeekend = 0b0000011
    private val everyWeekday = 0b1111100
    fun getTime(): String {
        return String.format("%02d:%02d", hour, minute)
    }

    fun getWeekString(): String {
        return when (week) {
            everyDay -> "every day"
            everyWeekend -> "every weekend"
            everyWeekday -> "every weekday"
            else -> {
                val days = mutableListOf<String>()
                val weekdays = listOf("Mon.", "Tue.", "Wed.", "Thu.", "Fri.", "Sat.", "Sun.")
                for (i in weekdays.indices) {
                    if ((week and (1 shl (6 - i))) != 0) {
                        days.add(weekdays[i])
                    }
                }
                if (days.size == 1) "every ${days[0]}" else days.joinToString(" ")
            }
        }
    }

    fun isEveryDay(): Boolean {
        return when (week) {
            everyDay -> true
            else -> false
        }
    }

    fun calendarWeek(): List<Int> {
        val weekdays = listOf(
            Calendar.MONDAY,
            Calendar.TUESDAY,
            Calendar.WEDNESDAY,
            Calendar.THURSDAY,
            Calendar.FRIDAY,
            Calendar.SATURDAY,
            Calendar.SUNDAY
        )
        val days = mutableListOf<Int>()
        for (i in weekdays.indices) {
            if ((week and (1 shl (6 - i))) != 0) {
                days.add(weekdays[i])
            }
        }
        return days
    }
}
