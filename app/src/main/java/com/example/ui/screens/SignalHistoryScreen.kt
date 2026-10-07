package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MarketType
import com.example.model.SignalResult
import com.example.model.TradingSignal
import com.example.ui.theme.*
import com.example.ui.viewmodel.TradingUiState
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SignalHistoryScreen(
    uiState: TradingUiState,
    onFilterMarket: (MarketType?) -> Unit,
    onFilterResult: (SignalResult?) -> Unit,
    onRecordOutcome: (String, SignalResult, Double) -> Unit,
    onDeleteSignal: (String) -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showClearConfirmDialog by remember { mutableStateOf(false) }
    var selectedSignalToRecord by remember { mutableStateOf<TradingSignal?>(null) }
    var outcomeResult by remember { mutableStateOf(SignalResult.WIN) }
    var pnlInput by remember { mutableStateOf("15.0") }

    val filteredList = remember(uiState.signalsHistory, uiState.historyFilterMarket, uiState.historyFilterResult) {
        uiState.signalsHistory.filter { sig ->
            (uiState.historyFilterMarket == null || sig.marketType == uiState.historyFilterMarket) &&
            (uiState.historyFilterResult == null || sig.result == uiState.historyFilterResult)
        }
    }

    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault()) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("signal_history_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SIGNAL JOURNAL",
                        color = GoldPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "${uiState.signalsHistory.size} total signals recorded in database",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }

                if (uiState.signalsHistory.isNotEmpty()) {
                    IconButton(
                        onClick = { showClearConfirmDialog = true },
                        modifier = Modifier.testTag("clear_history_button")
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Clear all", tint = TextTertiary)
                    }
                }
            }
        }

        // Market Filter Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = uiState.historyFilterMarket == null,
                        onClick = { onFilterMarket(null) },
                        label = { Text("ALL MARKETS") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GoldPrimary,
                            selectedLabelColor = DarkBackground
                        )
                    )
                }
                items(MarketType.values()) { mType ->
                    FilterChip(
                        selected = uiState.historyFilterMarket == mType,
                        onClick = { onFilterMarket(mType) },
                        label = { Text(mType.displayName) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GoldPrimary,
                            selectedLabelColor = DarkBackground
                        )
                    )
                }
            }
        }

        // Outcome Status Filter Chips (ALL, PENDING, WIN, LOSS, EXPIRED)
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = uiState.historyFilterResult == null,
                        onClick = { onFilterResult(null) },
                        label = { Text("ALL STATUS") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyanAccent,
                            selectedLabelColor = DarkBackground
                        )
                    )
                }
                items(SignalResult.values()) { res ->
                    FilterChip(
                        selected = uiState.historyFilterResult == res,
                        onClick = { onFilterResult(res) },
                        label = { Text(res.name) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = when (res) {
                                SignalResult.WIN -> BullishGreen
                                SignalResult.LOSS -> BearishRed
                                else -> GoldPrimary
                            },
                            selectedLabelColor = DarkBackground
                        )
                    )
                }
            }
        }

        // Empty state
        if (filteredList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(DarkSurface)
                        .border(1.dp, DarkCardBorder, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Icon(Icons.Default.HistoryEdu, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(48.dp))
                        Text(
                            text = "NO SIGNALS RECORDED",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Signals you generate and save in Binary, Forex, OTC, or Synthetics will be stored permanently here to calculate real win rates.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            // Signal History Cards
            items(filteredList, key = { it.id }) { sig ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, DarkCardBorder, RoundedCornerShape(14.dp)),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Top row: Date/Time + Result Badge
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = dateFormatter.format(Date(sig.timestamp)),
                                color = TextSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            when (sig.result) {
                                                SignalResult.WIN -> BullishGreenBg
                                                SignalResult.LOSS -> BearishRedBg
                                                SignalResult.PENDING -> GoldPrimary.copy(alpha = 0.2f)
                                                SignalResult.EXPIRED -> DarkSurfaceVariant
                                            }
                                        )
                                        .border(
                                            1.dp,
                                            when (sig.result) {
                                                SignalResult.WIN -> BullishGreen
                                                SignalResult.LOSS -> BearishRed
                                                SignalResult.PENDING -> GoldPrimary
                                                SignalResult.EXPIRED -> DarkBorderSubtle
                                            },
                                            RoundedCornerShape(6.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = sig.result.name,
                                        color = when (sig.result) {
                                            SignalResult.WIN -> BullishGreen
                                            SignalResult.LOSS -> BearishRed
                                            SignalResult.PENDING -> GoldPrimary
                                            SignalResult.EXPIRED -> TextSecondary
                                        },
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }

                                IconButton(
                                    onClick = { onDeleteSignal(sig.id) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Delete", tint = TextTertiary, modifier = Modifier.size(16.dp))
                                }
                            }
                        }

                        // Middle row: Asset, Direction, Timeframe
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = sig.pair,
                                        color = TextPrimary,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(DarkSurfaceVariant)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(sig.marketType.name, color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Text(
                                    text = "Entry: ${sig.formattedEntry} • TF: ${sig.timeframe}",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (sig.direction.isBullish) BullishGreenBg else BearishRedBg)
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = sig.direction.displayName,
                                    color = if (sig.direction.isBullish) BullishGreen else BearishRed,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Parameters row (SL / TP / Confidence)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(DarkSurfaceVariant, RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("CONFIDENCE: ${sig.confidenceScore}%", color = GoldAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            if (sig.stopLoss > 0) {
                                Text("SL: ${sig.formattedSL}", color = BearishRed, fontSize = 10.sp)
                                Text("TP: ${sig.formattedTP1}", color = BullishGreen, fontSize = 10.sp)
                            }
                            if (sig.pnl != 0.0) {
                                Text(
                                    text = if (sig.pnl > 0) "+$${sig.pnl}" else "-$${kotlin.math.abs(sig.pnl)}",
                                    color = if (sig.pnl > 0) BullishGreen else BearishRed,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Bottom Actions: Update result & Copy Telegram
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Telegram Signal", sig.toTelegramFormat()))
                                    Toast.makeText(context, "Telegram signal copied! 📋", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f).height(38.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant, contentColor = CyanAccent),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Copy Signal", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }

                            Button(
                                onClick = { selectedSignalToRecord = sig },
                                modifier = Modifier.weight(1f).height(38.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = DarkBackground),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Set Result", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Record Outcome Dialog
    if (selectedSignalToRecord != null) {
        val sig = selectedSignalToRecord!!
        AlertDialog(
            onDismissRequest = { selectedSignalToRecord = null },
            title = { Text("Update Outcome: ${sig.pair}", color = GoldPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Set the actual recorded trading result. Performance metrics update automatically.",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = { outcomeResult = SignalResult.WIN },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (outcomeResult == SignalResult.WIN) BullishGreen else DarkSurfaceVariant
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("WIN 🟢", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { outcomeResult = SignalResult.LOSS },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (outcomeResult == SignalResult.LOSS) BearishRed else DarkSurfaceVariant
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("LOSS 🔴", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { outcomeResult = SignalResult.EXPIRED },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (outcomeResult == SignalResult.EXPIRED) GoldPrimary else DarkSurfaceVariant
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("EXPIRED", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    OutlinedTextField(
                        value = pnlInput,
                        onValueChange = { pnlInput = it },
                        label = { Text("P&L Amount ($)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = pnlInput.toDoubleOrNull() ?: 0.0
                        val signedPnl = if (outcomeResult == SignalResult.WIN) amt else if (outcomeResult == SignalResult.LOSS) -kotlin.math.abs(amt) else 0.0
                        onRecordOutcome(sig.id, outcomeResult, signedPnl)
                        selectedSignalToRecord = null
                        Toast.makeText(context, "Result saved to journal! 📊", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = DarkBackground)
                ) {
                    Text("Save Outcome", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedSignalToRecord = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = DarkSurface
        )
    }

    // Clear confirmation dialog
    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = { Text("Clear Entire Signal Journal?", color = BearishRed, fontWeight = FontWeight.Bold) },
            text = {
                Text("This will delete all recorded signals and reset statistics to zero. This cannot be undone.", color = TextSecondary)
            },
            confirmButton = {
                Button(
                    onClick = {
                        onClearAll()
                        showClearConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BearishRed, contentColor = TextPrimary)
                ) {
                    Text("Clear All Data", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = DarkSurface
        )
    }
}
