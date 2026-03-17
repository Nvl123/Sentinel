package com.dicoding.sentinel.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "urge_logs")
data class UrgeLog(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val protocolsUsed: String, // Comma separated IDs
    val isDefeated: Boolean = true
)
