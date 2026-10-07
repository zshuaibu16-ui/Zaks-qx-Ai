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
fun ForexScreen(
    uiState: TradingUiState,
    onSelectPair: (String) -> Unit,
    onSelectTimeframe: (String) -> Unit,
    onGenerateSignal: () -> Unit,
    onSaveSignal: (TradingSignal) -> Unit,
    onRecordOutcome: (String, com.example.model.SignalResult, Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val forexPairs = MarketDataService.FOREX_PAIRS
    val forexTimeframes = listOf("5M", "15M", "30M", "1H")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("forex_screen"),
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
                        text = "FOREX SIGNAL ENGINE",
                        color = GoldPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (uiState.isForexOpen) BullishGreenBg else BearishRedBg)
                            .border(1.dp, if (uiState.isForexOpen) BullishGreen else BearishRed, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (uiState.isForexOpen) "MARKET OPEN 🟢" else "WEEKEND CLOSED 🔴",
                            color = if (uiState.isForexOpen) BullishGreen else BearishRed,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Text(
                    text = "Institutional setup generator with dynamic Stop Loss, Take Profit 1 & 2 (1:2 R:R) and multi-factor validation.",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
        }

        // Currency Pair Selection Row
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "CURRENCY PAIR",
                    color = TextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(forexPairs) { pair ->
                        val isSelected = uiState.forexPair == pair
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onSelectPair(pair) }
                                .background(if (isSelected) GoldPrimary else DarkSurfaceVariant)
                                .border(
                                    1.dp,
                                    if (isSelected) GoldLight else DarkBorderSubtle,
                                    RoundedCornerShape(10.dp)
                                )
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = pair,
                                color = if (isSelected) DarkBackground else TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Timeframe Selector Row (5M, 15M, 30M, 1H)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "CHART TIMEFRAME",
                    color = TextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    forexTimeframes.forEach { tf ->
                        val isSelected = uiState.forexTimeframe == tf
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

        // Generate Analysis Action Button
        item {
            Button(
                onClick = onGenerateSignal,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("analyze_forex_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldPrimary,
                    contentColor = DarkBackground
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.QueryStats, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ANALYZE FOREX SETUP",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }

        // Generated Forex Signal Card
        item {
            val signal = uiState.currentForexSignal
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

        // Live Technical Price Action & Key Levels
        item {
            val signal = uiState.currentForexSignal
            if (signal != null) {
                CandlestickMiniChart(
                    pair = signal.pair,
                    entryPrice = signal.entryPrice,
                    stopLoss = signal.stopLoss,
                    takeProfit1 = signal.takeProfit1
                )
            }
        }

        // Technical Breakdown Explanation
        item {
            val signal = uiState.currentForexSignal
            if (signal != null) {
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
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Psychology, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(18.dp))
                            Text(
                                text = "DETAILED TECHNICAL EXPLANATION",
                                color = GoldPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = signal.technicalExplanation,
                            color = TextPrimary,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
