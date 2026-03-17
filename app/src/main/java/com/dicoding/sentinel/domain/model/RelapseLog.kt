package com.dicoding.sentinel.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "relapse_logs")
data class RelapseLog(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val cause: String,
    val note: String = ""
)
