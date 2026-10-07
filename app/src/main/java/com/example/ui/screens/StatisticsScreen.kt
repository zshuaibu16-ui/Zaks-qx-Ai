package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SignalResult
import com.example.ui.components.MetricStatCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.TradingUiState

@Composable
fun StatisticsScreen(
    uiState: TradingUiState,
    modifier: Modifier = Modifier
) {
    val stats = uiState.stats
    val completedSignals = remember(uiState.signalsHistory) {
        uiState.signalsHistory.filter { it.result == SignalResult.WIN || it.result == SignalResult.LOSS }.reversed()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("statistics_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "PERFORMANCE ANALYTICS",
                    color = GoldPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Audited metrics derived exclusively from recorded journal trade outcomes.",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
        }

        // Today's Performance
        item {
            Text(
                text = "TODAY'S PERFORMANCE",
                color = TextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricStatCard(
                    title = "TODAY'S WIN RATE",
                    value = if (stats.todayWins + stats.todayLosses > 0)
                        "${String.format(java.util.Locale.US, "%.1f", stats.todayWinRate)}%"
                    else "0.0%",
                    subtitle = "${stats.todayWins} wins / ${stats.todayLosses} losses",
                    valueColor = if (stats.todayWinRate >= 70.0) BullishGreen else GoldPrimary,
                    modifier = Modifier.weight(1f)
                )

                MetricStatCard(
                    title = "TODAY'S P&L",
                    value = if (stats.todayPnl >= 0)
                        "+$${String.format(java.util.Locale.US, "%.2f", stats.todayPnl)}"
                    else "-$${String.format(java.util.Locale.US, "%.2f", kotlin.math.abs(stats.todayPnl))}",
                    subtitle = "Realized profit",
                    valueColor = if (stats.todayPnl >= 0) BullishGreen else BearishRed,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Periodic Performance Comparison: Weekly & Monthly
        item {
            Text(
                text = "HISTORICAL TIMEFRAME COMPARISON",
                color = TextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricStatCard(
                    title = "7-DAY WEEKLY WIN RATE",
                    value = if (stats.weeklyWinRate > 0)
                        "${String.format(java.util.Locale.US, "%.1f", stats.weeklyWinRate)}%"
                    else "N/A",
                    subtitle = "Past 7 calendar days",
                    valueColor = CyanAccent,
                    modifier = Modifier.weight(1f)
                )

                MetricStatCard(
                    title = "30-DAY MONTHLY WIN RATE",
                    value = if (stats.monthlyWinRate > 0)
                        "${String.format(java.util.Locale.US, "%.1f", stats.monthlyWinRate)}%"
                    else "N/A",
                    subtitle = "Past 30 calendar days",
                    valueColor = GoldLight,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // All-Time Summary
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkCardBorder, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "ALL-TIME PORTFOLIO STATS",
                        color = GoldPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("ALL-TIME WIN RATE", color = TextSecondary, fontSize = 10.sp)
                            Text(
                                text = "${String.format(java.util.Locale.US, "%.1f", stats.winRate)}%",
                                color = BullishGreen,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Column {
                            Text("RECORDED W/L", color = TextSecondary, fontSize = 10.sp)
                            Text(
                                text = "${stats.wins}W / ${stats.losses}L",
                                color = TextPrimary,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("TOTAL REALIZED P&L", color = TextSecondary, fontSize = 10.sp)
                            Text(
                                text = if (stats.totalPnl >= 0)
                                    "+$${String.format(java.util.Locale.US, "%.2f", stats.totalPnl)}"
                                else "-$${String.format(java.util.Locale.US, "%.2f", kotlin.math.abs(stats.totalPnl))}",
                                color = if (stats.totalPnl >= 0) BullishGreen else BearishRed,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    // Win / Loss Proportion bar
                    val totalRecorded = stats.wins + stats.losses
                    val winFraction = if (totalRecorded > 0) stats.wins.toFloat() / totalRecorded else 0.5f

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Wins: ${stats.wins}", color = BullishGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("Losses: ${stats.losses}", color = BearishRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(winFraction.coerceAtLeast(0.01f))
                                    .fillMaxHeight()
                                    .background(BullishGreen)
                            )
                            Box(
                                modifier = Modifier
                                    .weight((1f - winFraction).coerceAtLeast(0.01f))
                                    .fillMaxHeight()
                                    .background(BearishRed)
                            )
                        }
                    }
                }
            }
        }

        // Real Equity Curve Canvas Chart
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkCardBorder, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "EQUITY GROWTH CURVE",
                            color = GoldPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "${completedSignals.size} completed trades",
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                    }

                    // Canvas Equity Chart
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .background(DarkSurfaceVariant, RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        if (completedSignals.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = "Record signal outcomes in Journal to plot real equity curve",
                                    color = TextTertiary,
                                    fontSize = 11.sp
                                )
                            }
                        } else {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                var runningPnl = 0.0
                                val pnlPoints = mutableListOf<Double>()
                                pnlPoints.add(0.0)
                                completedSignals.forEach { s ->
                                    runningPnl += s.pnl
                                    pnlPoints.add(runningPnl)
                                }

                                val minPnl = minOf(pnlPoints.min(), -10.0)
                                val maxPnl = maxOf(pnlPoints.max(), 10.0)
                                val range = if (maxPnl > minPnl) maxPnl - minPnl else 1.0

                                val path = Path()
                                val stepX = size.width / (pnlPoints.size - 1).coerceAtLeast(1)

                                pnlPoints.forEachIndexed { idx, point ->
                                    val x = idx * stepX
                                    val y = ((1.0 - ((point - minPnl) / range)) * size.height).toFloat()
                                    if (idx == 0) path.moveTo(x, y) else path.lineTo(x, y)
                                }

                                // Zero line
                                val zeroY = ((1.0 - ((0.0 - minPnl) / range)) * size.height).toFloat()
                                drawLine(
                                    color = Color(0x33FFFFFF),
                                    start = Offset(0f, zeroY),
                                    end = Offset(size.width, zeroY),
                                    strokeWidth = 1f
                                )

                                drawPath(
                                    path = path,
                                    color = if (runningPnl >= 0) BullishGreen else BearishRed,
                                    style = Stroke(width = 3f)
                                )
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
}
