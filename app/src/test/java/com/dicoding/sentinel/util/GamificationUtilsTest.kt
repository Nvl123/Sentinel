package com.dicoding.sentinel.util

import org.junit.Assert.assertEquals
import org.junit.Test

class GamificationUtilsTest {

    @Test
    fun getLevel_neophyte_for_0_days() {
        val level = GamificationUtils.getLevel(0)
        assertEquals("Neophyte", level.name)
    }

    @Test
    fun getLevel_initiate_for_1_day() {
        val level = GamificationUtils.getLevel(1)
        assertEquals("Initiate", level.name)
    }

    @Test
    fun getLevel_apprentice_for_3_days() {
        val level = GamificationUtils.getLevel(3)
        assertEquals("Apprentice", level.name)
        val level2 = GamificationUtils.getLevel(6)
        assertEquals("Apprentice", level2.name)
    }

    @Test
    fun getLevel_sentry_for_7_days() {
        val level = GamificationUtils.getLevel(7)
        assertEquals("Sentry", level.name)
    }

    @Test
    fun getLevel_legend_for_365_days() {
        val level = GamificationUtils.getLevel(365)
        assertEquals("Legend", level.name)
        val level2 = GamificationUtils.getLevel(1000)
        assertEquals("Legend", level2.name)
    }

    @Test
    fun getNextLevel_returns_correct_level() {
        val next = GamificationUtils.getNextLevel(0)
        assertEquals("Initiate", next?.name)
        
        val next2 = GamificationUtils.getNextLevel(364)
        assertEquals("Legend", next2?.name)
        
        val next3 = GamificationUtils.getNextLevel(365)
        assertEquals(null, next3)
    }
}
