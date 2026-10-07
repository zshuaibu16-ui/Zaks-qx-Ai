package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.model.TradingSignal
import com.example.ui.components.SignalCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.TradingUiState

@Composable
fun OtcScanScreen(
    uiState: TradingUiState,
    onSelectTimeframe: (String) -> Unit,
    onRescan: () -> Unit,
    onSaveSignal: (TradingSignal) -> Unit,
    onRecordOutcome: (String, com.example.model.SignalResult, Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val timeframes = listOf("1M", "2M", "3M", "4M", "5M")
    var expandedSignalId by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("otc_scan_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section Header & OTC Disclaimer
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "OTC MARKET SCANNER",
                        color = GoldPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(GoldPrimary.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("24/7 OTC FEEDS", color = GoldPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Text(
                    text = "Automated scanner across all Over-The-Counter synthetic quotes. Identifies highest confluence opportunities.",
                    color = TextSecondary,
                    fontSize = 11.sp
                )

                // Explicit OTC Notice
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSurfaceVariant)
                        .border(1.dp, DarkBorderSubtle, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(16.dp))
                        Text(
                            text = "NOTICE: OTC assets are internal broker-synthesized quotations, not real central exchange prices.",
                            color = CyanAccent,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Timeframe Selection Row
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "SCAN TIMEFRAME",
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
                        val isSelected = uiState.otcScanTimeframe == tf
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onSelectTimeframe(tf) }
                                .background(if (isSelected) GoldPrimary else DarkSurfaceVariant)
                                .border(
                                    1.dp,
                                    if (isSelected) GoldLight else DarkBorderSubtle,
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

        // Refresh Scanner Action
        item {
            Button(
                onClick = onRescan,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("refresh_otc_scan_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldPrimary,
                    contentColor = DarkBackground
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (uiState.isOtcScanning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = DarkBackground,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("SCANNING OTC ASSETS...", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                } else {
                    Icon(Icons.Default.Radar, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("RE-SCAN ALL OTC PAIRS", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                }
            }
        }

        // OTC Pairs Scan Results List
        item {
            Text(
                text = "TOP CONFLUENCE RANKINGS (${uiState.otcScanResults.size} PAIRS)",
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }

        items(uiState.otcScanResults) { scanItem ->
            val sig = scanItem.signal
            val isExpanded = expandedSignalId == sig.id

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(DarkSurface)
                    .border(
                        1.dp,
                        if (sig.confidenceScore >= 80) GoldPrimary.copy(alpha = 0.6f) else DarkCardBorder,
                        RoundedCornerShape(14.dp)
                    )
            ) {
                // Row header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            expandedSignalId = if (isExpanded) null else sig.id
                        }
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = scanItem.pair,
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(GoldPrimary.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("OTC", color = GoldPrimary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Text(
                            text = "${sig.marketStructure.trend} • ${sig.detectedPattern}",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Direction badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (sig.direction.isBullish) BullishGreenBg else BearishRedBg)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = sig.direction.displayName,
                                color = if (sig.direction.isBullish) BullishGreen else BearishRed,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        // Confidence meter
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${sig.confidenceScore}%",
                                color = if (sig.confidenceScore >= 80) BullishGreen else GoldPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = sig.confidenceGrade.label,
                                color = TextTertiary,
                                fontSize = 9.sp
                            )
                        }

                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Toggle",
                            tint = TextSecondary
                        )
                    }
                }

                // If expanded, show complete SignalCard
                if (isExpanded) {
                    Divider(color = DarkCardBorder)
                    Box(modifier = Modifier.padding(12.dp)) {
                        SignalCard(
                            signal = sig,
                            onSaveToHistory = onSaveSignal,
                            onRecordOutcome = { result, pnl ->
                                onRecordOutcome(sig.id, result, pnl)
                            }
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
