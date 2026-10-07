package com.example.ui.screens

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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.market.MarketDataService
import com.example.model.TradingSignal
import com.example.ui.components.CandlestickMiniChart
import com.example.ui.components.SignalCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.TradingUiState

@Composable
fun SyntheticScreen(
    uiState: TradingUiState,
    onSelectInstrument: (String) -> Unit,
    onSelectTimeframe: (String) -> Unit,
    onGenerateSignal: () -> Unit,
    onSaveSignal: (TradingSignal) -> Unit,
    onRecordOutcome: (String, com.example.model.SignalResult, Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val instruments = MarketDataService.SYNTHETIC_INSTRUMENTS
    val timeframes = listOf("1M", "5M", "15M", "1H")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("synthetic_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section Header & Live Stream Status
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SYNTHETIC INDICES",
                        color = GoldPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(BullishGreenBg)
                            .border(1.dp, BullishGreen, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("ALGO GENERATED 24/7", color = BullishGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Text(
                    text = "Volatility indices and Crash/Boom spike pattern recognition engineered with cryptographic PRNG algorithms.",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
        }

        // Instrument Selector
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "SYNTHETIC INSTRUMENT",
                    color = TextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(instruments) { inst ->
                        val isSelected = uiState.syntheticInstrument == inst
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onSelectInstrument(inst) }
                                .background(if (isSelected) GoldPrimary else DarkSurfaceVariant)
                                .border(
                                    1.dp,
                                    if (isSelected) GoldLight else DarkBorderSubtle,
                                    RoundedCornerShape(10.dp)
                                )
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = inst,
                                color = if (isSelected) DarkBackground else TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Timeframe Selector
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "TIMEFRAME",
                    color = TextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    timeframes.forEach { tf ->
                        val isSelected = uiState.syntheticTimeframe == tf
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onSelectTimeframe(tf) }
                                .background(if (isSelected) CyanAccent else DarkSurfaceVariant)
                                .border(
                                    1.dp,
                                    if (isSelected) CyanAccent else DarkBorderSubtle,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tf,
                                color = if (isSelected) DarkBackground else TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Action Button
        item {
            Button(
                onClick = onGenerateSignal,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("analyze_synthetic_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldPrimary,
                    contentColor = DarkBackground
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.AutoGraph, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ANALYZE SYNTHETIC PATTERN",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }

        // Generated Signal Card
        item {
            val signal = uiState.currentSyntheticSignal
            if (signal != null) {
                SignalCard(
                    signal = signal,
                    onSaveToHistory = onSaveSignal,
                    onRecordOutcome = { result, pnl ->
                        onRecordOutcome(signal.id, result, pnl)
                    }
                )
            }
        }

        // Chart
        item {
            val signal = uiState.currentSyntheticSignal
            if (signal != null) {
                CandlestickMiniChart(
                    pair = signal.pair,
                    entryPrice = signal.entryPrice,
                    stopLoss = signal.stopLoss,
                    takeProfit1 = signal.takeProfit1
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
