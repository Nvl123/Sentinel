package com.dicoding.sentinel.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.sentinel.domain.model.Protocol
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FirewallState(
    val isActive: Boolean = false,
    val needsSelection: Boolean = false,
    val currentProtocol: Protocol? = null,
    val usedProtocols: Set<Int> = emptySet(),
    val timerRemaining: Int = 0,
    val showCheckIn: Boolean = false,
    val isVictorious: Boolean = false,
    val usedProtocolIds: List<Int> = emptyList() // For logging
)

class FirewallViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(FirewallState())
    val uiState: StateFlow<FirewallState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    fun startFirewallFlow() {
        _uiState.update { it.copy(isActive = true, needsSelection = true) }
    }

    fun activateFirewall(protocol: Protocol) {
        _uiState.update {
            it.copy(
                isActive = true,
                needsSelection = false,
                currentProtocol = protocol,
                usedProtocols = it.usedProtocols + protocol.id,
                usedProtocolIds = it.usedProtocolIds + protocol.id,
                timerRemaining = protocol.durationSeconds,
                showCheckIn = false,
                isVictorious = false
            )
        }
        startTimerIfNecessary(protocol)
    }

    fun restoreState(protocolId: Int?, usedIdsStr: String) {
        if (protocolId == null || _uiState.value.isActive) return
        
        val protocol = Protocol.library.find { it.id == protocolId } ?: return
        val usedIds = usedIdsStr.split(",").filter { it.isNotEmpty() }.map { it.toInt() }.toSet()
        val usedList = usedIdsStr.split(",").filter { it.isNotEmpty() }.map { it.toInt() }

        _uiState.update {
            it.copy(
                isActive = true,
                needsSelection = false,
                currentProtocol = protocol,
                usedProtocols = usedIds,
                usedProtocolIds = usedList,
                timerRemaining = protocol.durationSeconds, // For now reset timer on restart, or we'd need start time
                showCheckIn = false
            )
        }
    }

    private fun startTimerIfNecessary(protocol: Protocol) {
        timerJob?.cancel()
        if (protocol.durationSeconds > 0) {
            timerJob = viewModelScope.launch {
                var remaining = protocol.durationSeconds
                while (remaining > 0) {
                    delay(1000)
                    remaining--
                    _uiState.update { it.copy(timerRemaining = remaining) }
                }
            }
        }
    }

    fun onProtocolFinished() {
        _uiState.update { it.copy(showCheckIn = true) }
    }

    fun onUrgeStillPresent(stillPresent: Boolean) {
        if (stillPresent) {
            val availableCount = Protocol.library.size - _uiState.value.usedProtocols.size
            if (availableCount > 0) {
                _uiState.update {
                    it.copy(
                        needsSelection = true,
                        showCheckIn = false
                    )
                }
            } else {
                _uiState.update { it.copy(isVictorious = true, showCheckIn = false) }
            }
        } else {
            _uiState.update { it.copy(isVictorious = true, showCheckIn = false) }
        }
    }

    fun dismissFirewall() {
        _uiState.update { FirewallState() }
        timerJob?.cancel()
    }
}
