package com.dicoding.sentinel.util

import com.dicoding.sentinel.domain.model.RelapseLog
import java.time.*
import java.util.*

object StreakUtils {
    /**
     * Identifies which days in a specific year and month are "clean" and which are "relapse" days.
     * A day is a relapse day if at least one RelapseLog occurs on that date.
     * a day is "clean" if it is after the user's first recorded activity and has no relapses.
     */
    fun getMonthData(
        relapses: List<RelapseLog>,
        streakStartTime: Long?,
        year: Int,
        month: Int,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): Map<LocalDate, DayStatus> {
        val startOfMonth = LocalDate.of(year, month, 1)
        val endOfMonth = startOfMonth.plusMonths(1).minusDays(1)
        
        val data = mutableMapOf<LocalDate, DayStatus>()
        
        // Find all relapse days
        val relapseDates = relapses.map {
            Instant.ofEpochMilli(it.timestamp).atZone(zoneId).toLocalDate()
        }.toSet()
        
        // Determine the start boundary (earliest of first relapse or first streak start)
        val firstRelapseTime = relapses.minByOrNull { it.timestamp }?.timestamp
        val earliestActivityTime = listOfNotNull(firstRelapseTime, streakStartTime).minOrNull()
        val boundaryDate = earliestActivityTime?.let {
            Instant.ofEpochMilli(it).atZone(zoneId).toLocalDate()
        } ?: LocalDate.now(zoneId)

        val today = LocalDate.now(zoneId)

        var current = startOfMonth
        while (!current.isAfter(endOfMonth)) {
            val status = when {
                relapseDates.contains(current) -> DayStatus.RELAPSE
                current.isAfter(today) -> DayStatus.FUTURE
                current.isBefore(boundaryDate) -> DayStatus.NONE // Before user started using the app
                else -> DayStatus.CLEAN
            }
            data[current] = status
            current = current.plusDays(1)
        }
        
        return data
    }

    enum class DayStatus {
        CLEAN,   // No relapse
        RELAPSE, // Had a relapse
        FUTURE,  // Future day
        NONE     // Before app usage started
    }
}
