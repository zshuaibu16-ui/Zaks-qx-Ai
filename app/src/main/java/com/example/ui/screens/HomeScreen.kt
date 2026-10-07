package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MarketType
import com.example.model.TradingSignal
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppNavTab
import com.example.ui.viewmodel.TradingUiState

@Composable
fun HomeScreen(
    uiState: TradingUiState,
    onNavigate: (AppNavTab) -> Unit,
    onSaveSignal: (TradingSignal) -> Unit,
    onRecordOutcome: (String, com.example.model.SignalResult, Double) -> Unit,
    onToggleSound: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Header
        item {
            TradingHeader(
                primarySession = uiState.primarySession,
                isForexOpen = uiState.isForexOpen,
                soundEnabled = uiState.soundEnabled,
                onToggleSound = onToggleSound
            )
        }

        // Live Market Sessions Overview
        item {
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "GLOBAL MARKET SESSIONS",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "UTC CLOCK",
                            color = GoldPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        uiState.sessions.forEach { session ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (session.isOpen) BullishGreenBg else DarkSurfaceVariant)
                                    .border(
                                        1.dp,
                                        if (session.isOpen) BullishGreen.copy(alpha = 0.5f) else DarkBorderSubtle,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = session.sessionName,
                                        color = if (session.isOpen) BullishGreen else TextSecondary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (session.isOpen) "OPEN" else "CLOSED",
                                        color = if (session.isOpen) BullishGreenDark else TextTertiary,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Real Performance Metrics Cards
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "REAL RECORDED PERFORMANCE",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricStatCard(
                        title = "TODAY'S WIN RATE",
                        value = if (uiState.stats.todayWins + uiState.stats.todayLosses > 0)
                            "${String.format(java.util.Locale.US, "%.1f", uiState.stats.todayWinRate)}%"
                        else "No signals",
                        subtitle = "${uiState.stats.todayWins}W / ${uiState.stats.todayLosses}L recorded",
                        valueColor = if (uiState.stats.todayWinRate >= 70.0) BullishGreen else GoldPrimary,
                        modifier = Modifier.weight(1f)
                    )

                    MetricStatCard(
                        title = "TODAY'S P&L",
                        value = if (uiState.stats.todayPnl >= 0)
                            "+$${String.format(java.util.Locale.US, "%.2f", uiState.stats.todayPnl)}"
                        else "-$${String.format(java.util.Locale.US, "%.2f", kotlin.math.abs(uiState.stats.todayPnl))}",
                        subtitle = "Real journal result",
                        valueColor = if (uiState.stats.todayPnl >= 0) BullishGreen else BearishRed,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricStatCard(
                        title = "HISTORICAL WIN RATE",
                        value = if (uiState.stats.wins + uiState.stats.losses > 0)
                            "${String.format(java.util.Locale.US, "%.1f", uiState.stats.winRate)}%"
                        else "0%",
                        subtitle = "${uiState.stats.wins} wins of ${uiState.stats.wins + uiState.stats.losses} total",
                        valueColor = GoldAccent,
                        modifier = Modifier.weight(1f)
                    )

                    MetricStatCard(
                        title = "RECORDED SIGNALS",
                        value = "${uiState.stats.totalSignals}",
                        subtitle = "${uiState.stats.pendingCount} pending outcome",
                        valueColor = CyanAccent,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Quick Signal Modules Navigation
        item {
            Text(
                text = "TRADING ANALYSIS ENGINES",
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                EngineShortcutCard(
                    title = "BINARY SYSTEM",
                    desc = "1M - 5M Expiry analysis",
                    badge = "CALL / PUT",
                    badgeColor = GoldPrimary,
                    onClick = { onNavigate(AppNavTab.BINARY) },
                    modifier = Modifier.weight(1f)
                )

                EngineShortcutCard(
                    title = "FOREX SYSTEM",
                    desc = "5M - 1H Entry, SL, TP1, TP2",
                    badge = "1:2 R:R",
                    badgeColor = CyanAccent,
                    onClick = { onNavigate(AppNavTab.FOREX) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                EngineShortcutCard(
                    title = "OTC SCANNER",
                    desc = "7 OTC pairs confluence scan",
                    badge = "24/7",
                    badgeColor = BullishGreen,
                    onClick = { onNavigate(AppNavTab.OTC_SCAN) },
                    modifier = Modifier.weight(1f)
                )

                EngineShortcutCard(
                    title = "SYNTHETIC INDICES",
                    desc = "Vol 75, Boom & Crash 500",
                    badge = "ALGO",
                    badgeColor = GoldLight,
                    onClick = { onNavigate(AppNavTab.SYNTHETIC) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Live Spotlight Signal Card
        item {
            Text(
                text = "LATEST HIGH-CONFLUENCE SIGNAL",
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }

        item {
            val spotlightSignal = uiState.currentBinarySignal ?: uiState.currentForexSignal
            if (spotlightSignal != null) {
                SignalCard(
                    signal = spotlightSignal,
                    onSaveToHistory = onSaveSignal,
                    onRecordOutcome = { result, pnl ->
                        onRecordOutcome(spotlightSignal.id, result, pnl)
                    },
                    expirySecondsLeft = uiState.binaryExpirySecondsLeft
                )
            }
        }

        // Mini Price Chart Preview
        item {
            val pair = uiState.currentBinarySignal?.pair ?: "EUR/USD"
            val entry = uiState.currentBinarySignal?.entryPrice ?: 1.08450
            CandlestickMiniChart(
                pair = pair,
                entryPrice = entry,
                stopLoss = uiState.currentForexSignal?.stopLoss ?: 0.0,
                takeProfit1 = uiState.currentForexSignal?.takeProfit1 ?: 0.0
            )
        }

        // Safety & Disclaimer Footer
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurfaceVariant)
                    .border(1.dp, DarkBorderSubtle, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(16.dp))
                        Text(
                            text = "RISK TRANSPARENCY NOTICE",
                            color = GoldPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "ZAKS QX AI is an educational market analysis platform. No signal is guaranteed to win. All statistics are strictly derived from real recorded trades in your journal. Trade responsibly.",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        lineHeight = 14.sp
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun EngineShortcutCard(
    title: String,
    desc: String,
    badge: String,
    badgeColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .border(1.dp, DarkCardBorder, RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = GoldLight,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(badgeColor.copy(alpha = 0.2f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = badge,
                        color = badgeColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
            Text(
                text = desc,
                color = TextSecondary,
                fontSize = 10.sp
            )
        }
    }
}
