package com.dicoding.sentinel.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dicoding.sentinel.domain.model.Protocol
import com.dicoding.sentinel.domain.model.ProtocolType

@Composable
fun FirewallOverlay(
    isActive: Boolean,
    currentProtocol: Protocol?,
    timerRemaining: Int,
    showCheckIn: Boolean,
    isVictorious: Boolean,
    onUrgeStillPresent: (Boolean) -> Unit,
    onProtocolDone: () -> Unit,
    onProtocolAction: (Protocol) -> Unit,
    onVictoryConfirmed: () -> Unit,
    onDismiss: () -> Unit
) {
    if (isActive) {
        val infiniteTransition = rememberInfiniteTransition(label = "firewall_pulse")
        val alpha by infiniteTransition.animateFloat(
            initialValue = 0.85f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse_alpha"
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = alpha))
                .clickable(enabled = true, onClick = {}) // Consume all click events
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                when {
                    isVictorious -> VictoryView(onVictoryConfirmed)
                    showCheckIn -> CheckInView(onUrgeStillPresent)
                    else -> ProtocolView(currentProtocol, timerRemaining, onProtocolDone, onProtocolAction)
                }
            }
        }
    }
}

@Composable
fun VictoryView(onDismiss: () -> Unit) {
    Text(
        text = "VICTORY",
        style = MaterialTheme.typography.displayMedium.copy(
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Black,
            letterSpacing = 8.sp
        )
    )
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = "URGE DEFEATED. YOU ARE IN CONTROL.",
        color = Color.White,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(48.dp))
    Button(onClick = onDismiss) {
        Text("RETURN TO DASHBOARD")
    }
}

@Composable
fun ProtocolView(
    protocol: Protocol?,
    timerRemaining: Int,
    onProtocolDone: () -> Unit,
    onProtocolAction: (Protocol) -> Unit
) {
    if (protocol == null) return

    val timerFinished = timerRemaining == 0
    var showWarning by remember { mutableStateOf(false) }

    Text(
        text = protocol.name.uppercase(),
        style = MaterialTheme.typography.headlineSmall.copy(
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        ),
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(24.dp))

    Text(
        text = protocol.instruction,
        style = MaterialTheme.typography.bodyLarge.copy(
            color = Color.White,
            lineHeight = 28.sp
        ),
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(32.dp))

    // Action button for specific protocols (Grayscale)
    if (protocol.id == 2) {
        OutlinedButton(
            onClick = { onProtocolAction(protocol) },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("BUKA PENGATURAN")
        }
        Spacer(modifier = Modifier.height(24.dp))
    }

    // Grounding Inputs for 5-4-3-2-1
    var groundingInputs by remember { mutableStateOf(List(5) { "" }) }
    val isGrounding = protocol.id == 3
    if (isGrounding) {
        GroundingInputs(
            inputs = groundingInputs,
            onInputChange = { index, value ->
                groundingInputs = groundingInputs.toMutableList().apply { this[index] = value }
            }
        )
        Spacer(modifier = Modifier.height(24.dp))
    }

    if (protocol.type == ProtocolType.TIMER) {
        Text(
            text = String.format("%02d:%02d", timerRemaining / 60, timerRemaining % 60),
            style = MaterialTheme.typography.displayLarge.copy(
                color = if (timerFinished) MaterialTheme.colorScheme.primary else Color.White,
                fontWeight = FontWeight.Thin
            )
        )
        
        if (showWarning && !timerFinished) {
            Text(
                text = "Waktu belum selesai, silahkan selesaikan!",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }

    if (showWarning && isGrounding && groundingInputs.any { it.isBlank() }) {
        Text(
            text = "Mohon isi semua inputan grounding!",
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
    }

    Spacer(modifier = Modifier.height(32.dp))

    Button(
        onClick = {
            if (protocol.type == ProtocolType.TIMER && !timerFinished) {
                showWarning = true
            } else if (isGrounding && groundingInputs.any { it.isBlank() }) {
                showWarning = true
            } else {
                onProtocolDone()
            }
        },
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(0.7f).height(56.dp)
    ) {
        Text("SELESAI", fontWeight = FontWeight.Bold)
    }
}

@Composable
fun GroundingInputs(
    inputs: List<String>,
    onInputChange: (Int, String) -> Unit
) {
    val labels = listOf("5 Benda terlihat", "4 Suara terdengar", "3 Sentuhan fisik", "2 Aroma tercium", "1 Rasa dirasakan")
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        labels.forEachIndexed { index, label ->
            OutlinedTextField(
                value = inputs[index],
                onValueChange = { onInputChange(index, it) },
                label = { Text(label, color = Color.White.copy(alpha = 0.6f)) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                ),
                singleLine = true
            )
        }
    }
}

@Composable
fun CheckInView(onUrgeStillPresent: (Boolean) -> Unit) {
    Text(
        text = "APAKAH KEINGINAN MASIH ADA?",
        style = MaterialTheme.typography.headlineSmall.copy(
            color = Color.White,
            fontWeight = FontWeight.Bold
        ),
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(48.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Button(
            onClick = { onUrgeStillPresent(true) },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text("MASIH ADA (YA)")
        }

        Button(
            onClick = { onUrgeStillPresent(false) },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("SUDAH HILANG (TIDAK)")
        }
    }
}
