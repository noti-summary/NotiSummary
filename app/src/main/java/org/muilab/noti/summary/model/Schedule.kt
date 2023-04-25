package org.muilab.noti.summary.model

import android.content.Context
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.muilab.noti.summary.R
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.*

private const val everyDay = 0b1111111
private const val everyWeekend = 0b0000011
private const val everyWeekday = 0b1111100

@Entity(tableName = "time_events")
data class Schedule(
    @PrimaryKey(autoGenerate = true)
    var primaryKey: Int = 0,
    var hour: Int,
    var minute: Int,
    var week: Int = 0b1111111
    // 7-bit binary number for the week, with each bit representing a day from Monday to Sunday.
) {
    fun getTime(): String {
        return String.format("%02d:%02d", hour, minute)
    }

    fun getWeekString(context: Context): String {
        return when (week) {
            everyDay -> context.getString(R.string.everyDay)
            everyWeekend -> context.getString(R.string.everyWeekend)
            everyWeekday -> context.getString(R.string.everyWeekday)
            else -> {
                val days = mutableListOf<Int>()
                val weekdaysShort = DayOfWeek.values().map {
                    var weekText = it.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                    if (weekText[weekText.length - 1] in 'a'..'z')
                        weekText += '.'
                    weekText
                }
                val weekdaysLong = DayOfWeek.values().map {
                    it.getDisplayName(TextStyle.FULL, Locale.getDefault())
                }
                for (i in weekdaysShort.indices) {
                    if ((week and (1 shl (6 - i))) != 0) {
                        days.add(i)
                    }
                }
                if (week.countOneBits() == 1)
                    "${context.getString(R.string.every)}${weekdaysLong[days[0]]}"
                else
                    days.joinToString(" ") { weekdaysShort[it] }
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
