package com.dicoding.sentinel.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.sentinel.data.local.RelapseDao
import com.dicoding.sentinel.domain.model.RelapseLog
import com.dicoding.sentinel.domain.model.UrgeLog
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RelapseViewModel(private val relapseDao: RelapseDao) : ViewModel() {

    val allRelapses: StateFlow<List<RelapseLog>> = relapseDao.getAllRelapses()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allUrgeLogs: StateFlow<List<UrgeLog>> = relapseDao.getAllUrgeLogs()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun logRelapse(cause: String, note: String = "") {
        viewModelScope.launch {
            relapseDao.insertRelapse(RelapseLog(cause = cause, note = note))
        }
    }

    fun logUrgeDefeated(protocols: List<Int>) {
        viewModelScope.launch {
            relapseDao.insertUrgeLog(UrgeLog(protocolsUsed = protocols.joinToString(",")))
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            relapseDao.deleteAllRelapses()
            relapseDao.deleteAllUrgeLogs()
        }
    }
}
