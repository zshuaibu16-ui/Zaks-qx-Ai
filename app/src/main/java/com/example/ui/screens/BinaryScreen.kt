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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.market.MarketDataService
import com.example.model.MarketType
import com.example.model.TradingSignal
import com.example.ui.components.CandlestickMiniChart
import com.example.ui.components.SignalCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.TradingUiState

@Composable
fun BinaryScreen(
    uiState: TradingUiState,
    onSelectPair: (String, Boolean) -> Unit,
    onSelectTimeframe: (String) -> Unit,
    onSelectDirectionTarget: (Boolean?) -> Unit,
    onGenerateSignal: () -> Unit,
    onStartCountdown: (TradingSignal) -> Unit,
    onSaveSignal: (TradingSignal) -> Unit,
    onRecordOutcome: (String, com.example.model.SignalResult, Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val timeframes = listOf("1M", "2M", "3M", "4M", "5M")
    val binaryPairs = MarketDataService.FOREX_PAIRS

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("binary_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section Header
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "BINARY SIGNAL ENGINE",
                        color = GoldPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )

                    // OTC Toggle Chip
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                onSelectPair(uiState.binaryPair, !uiState.binaryIsOtc)
                            }
                            .background(if (uiState.binaryIsOtc) GoldPrimary else DarkSurfaceVariant)
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "OTC MODE",
                            color = if (uiState.binaryIsOtc) DarkBackground else TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (uiState.binaryIsOtc) {
                            Text("●", color = DarkBackground, fontSize = 10.sp)
                        }
                    }
                }

                Text(
                    text = "High-precision 1–5 minute expiry options analysis with multi-indicator confluence.",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
        }

        // Currency Pair Selection Row
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "SELECT ASSET",
                    color = TextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(binaryPairs) { pair ->
                        val isSelected = uiState.binaryPair == pair
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onSelectPair(pair, uiState.binaryIsOtc) }
                                .background(if (isSelected) GoldPrimary else DarkSurfaceVariant)
                                .border(
                                    1.dp,
                                    if (isSelected) GoldLight else DarkBorderSubtle,
                                    RoundedCornerShape(10.dp)
                                )
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = if (uiState.binaryIsOtc) "$pair OTC" else pair,
                                color = if (isSelected) DarkBackground else TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Timeframe Selector Row (1M, 2M, 3M, 4M, 5M)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "EXPIRY TIMEFRAME",
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
                        val isSelected = uiState.binaryTimeframe == tf
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

        // Direction Bias / Filter Selection (AUTO, CALL, PUT)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "DIRECTION ANALYSIS MODE",
                    color = TextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Auto Confluence
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onSelectDirectionTarget(null) }
                            .background(if (uiState.binaryDirectionTarget == null) GoldPrimary else DarkSurfaceVariant)
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "AUTO SCAN",
                            color = if (uiState.binaryDirectionTarget == null) DarkBackground else TextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // CALL Only
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onSelectDirectionTarget(true) }
                            .background(if (uiState.binaryDirectionTarget == true) BullishGreen else DarkSurfaceVariant)
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "CALL 🟢",
                            color = if (uiState.binaryDirectionTarget == true) DarkBackground else BullishGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // PUT Only
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onSelectDirectionTarget(false) }
                            .background(if (uiState.binaryDirectionTarget == false) BearishRed else DarkSurfaceVariant)
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "PUT 🔴",
                            color = if (uiState.binaryDirectionTarget == false) DarkBackground else BearishRed,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Refresh / Analyze Action Button
        item {
            Button(
                onClick = onGenerateSignal,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("analyze_binary_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldPrimary,
                    contentColor = DarkBackground
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "RUN CONFLUENCE ANALYSIS",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }

        // Generated Binary Signal Card
        item {
            val signal = uiState.currentBinarySignal
            if (signal != null) {
                SignalCard(
                    signal = signal,
                    onSaveToHistory = onSaveSignal,
                    onRecordOutcome = { result, pnl ->
                        onRecordOutcome(signal.id, result, pnl)
                    },
                    expirySecondsLeft = uiState.binaryExpirySecondsLeft,
                    onStartExpiryTimer = { onStartCountdown(signal) }
                )
            }
        }

        // Live Mini Chart
        item {
            val signal = uiState.currentBinarySignal
            if (signal != null) {
                CandlestickMiniChart(
                    pair = signal.pair,
                    entryPrice = signal.entryPrice
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
