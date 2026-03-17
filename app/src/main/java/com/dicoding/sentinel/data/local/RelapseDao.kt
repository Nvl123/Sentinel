package com.dicoding.sentinel.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.dicoding.sentinel.domain.model.RelapseLog
import com.dicoding.sentinel.domain.model.UrgeLog
import kotlinx.coroutines.flow.Flow

@Dao
interface RelapseDao {
    @Insert
    suspend fun insertRelapse(relapse: RelapseLog)

    @Query("SELECT * FROM relapse_logs ORDER BY timestamp DESC")
    fun getAllRelapses(): Flow<List<RelapseLog>>

    @Insert
    suspend fun insertUrgeLog(urgeLog: UrgeLog)

    @Query("SELECT * FROM urge_logs ORDER BY timestamp DESC")
    fun getAllUrgeLogs(): Flow<List<UrgeLog>>

    @Query("DELETE FROM relapse_logs")
    suspend fun deleteAllRelapses()

    @Query("DELETE FROM urge_logs")
    suspend fun deleteAllUrgeLogs()
}
