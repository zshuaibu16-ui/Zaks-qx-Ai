package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*

@Composable
fun SignalCard(
    signal: TradingSignal,
    onSaveToHistory: (TradingSignal) -> Unit,
    onRecordOutcome: ((SignalResult, Double) -> Unit)? = null,
    expirySecondsLeft: Int = 0,
    onStartExpiryTimer: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showOutcomeDialog by remember { mutableStateOf(false) }
    var selectedOutcome by remember { mutableStateOf(SignalResult.WIN) }
    var pnlInput by remember { mutableStateOf("10.0") }
    var isSaved by remember { mutableStateOf(false) }

    val isBull = signal.direction.isBullish
    val isNoTrade = signal.direction == SignalDirection.NO_TRADE

    val accentColor = when {
        isNoTrade -> GoldPrimary
        isBull -> BullishGreen
        else -> BearishRed
    }

    val cardBorderBrush = Brush.verticalGradient(
        listOf(
            accentColor.copy(alpha = 0.8f),
            DarkCardBorder,
            GoldPrimary.copy(alpha = 0.3f)
        )
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("signal_card_${signal.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = CardDefaults.outlinedCardBorder().copy(brush = cardBorderBrush)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header: Brand & Asset
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ZAKS QX AI",
                        color = GoldPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = signal.pair,
                            color = TextPrimary,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (signal.isOTC) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(GoldPrimary.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "OTC",
                                    color = GoldPrimary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Direction Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            when {
                                isNoTrade -> DarkSurfaceVariant
                                isBull -> BullishGreenBg
                                else -> BearishRedBg
                            }
                        )
                        .border(
                            1.dp,
                            when {
                                isNoTrade -> GoldPrimary
                                isBull -> BullishGreen
                                else -> BearishRed
                            },
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = signal.direction.displayName,
                        color = accentColor,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Pricing & Parameters Grid
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurfaceVariant, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    Text("TIMEFRAME", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(signal.timeframe, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("ENTRY", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(signal.formattedEntry, color = GoldLight, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                if (signal.marketType == MarketType.FOREX || signal.marketType == MarketType.SYNTHETIC) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("STOP LOSS", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text(signal.formattedSL, color = BearishRed, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("TAKE PROFIT", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text(signal.formattedTP1, color = BullishGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Column(horizontalAlignment = Alignment.End) {
                        Text("EXPIRY", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text("${signal.expiryMinutes} MIN", color = CyanAccent, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Binary Expiry Countdown Timer if active
            if (signal.marketType == MarketType.BINARY && expirySecondsLeft > 0) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(GoldPrimary.copy(alpha = 0.15f))
                        .border(1.dp, GoldPrimary, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Timer, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(16.dp))
                        Text("EXPIRY COUNTDOWN", color = GoldLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    val mins = expirySecondsLeft / 60
                    val secs = expirySecondsLeft % 60
                    Text(
                        text = String.format(java.util.Locale.US, "%02d:%02d", mins, secs),
                        color = GoldPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Confidence Score Meter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "CONFIDENCE",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                when (signal.confidenceGrade) {
                                    SignalGrade.VERY_STRONG -> BullishGreenBg
                                    SignalGrade.STRONG -> GoldPrimary.copy(alpha = 0.2f)
                                    SignalGrade.MODERATE -> CyanAccent.copy(alpha = 0.2f)
                                    SignalGrade.NO_TRADE -> BearishRedBg
                                }
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = signal.confidenceGrade.label,
                            color = when (signal.confidenceGrade) {
                                SignalGrade.VERY_STRONG -> BullishGreen
                                SignalGrade.STRONG -> GoldPrimary
                                SignalGrade.MODERATE -> CyanAccent
                                SignalGrade.NO_TRADE -> BearishRed
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                Text(
                    text = "${signal.confidenceScore}%",
                    color = accentColor,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Progress Bar for Confidence
            LinearProgressIndicator(
                progress = { (signal.confidenceScore / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = accentColor,
                trackColor = DarkSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Indicators Confluence Checklist
            Text(
                text = "TECHNICAL CONFLUENCE CHECKLIST",
                color = TextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Multi-indicator pills
            FlowIndicatorPills(checks = signal.indicatorChecks)

            if (signal.detectedPattern.isNotBlank() && signal.detectedPattern != "None") {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkSurfaceVariant, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("PATTERN DETECTED:", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("${signal.detectedPattern} ✅", color = BullishGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Market Structure & Reason
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurfaceVariant, RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("MARKET STRUCTURE:", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "${signal.marketStructure.trend} • ${signal.marketStructure.keyEvent}",
                        color = GoldLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "REASON: ${signal.reason}",
                    color = TextPrimary,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Copy Telegram Format Button
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Telegram Signal", signal.toTelegramFormat())
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Telegram signal copied to clipboard! 📋", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("copy_telegram_button_${signal.id}"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkSurfaceVariant,
                        contentColor = CyanAccent
                    ),
                    shape = RoundedCornerShape(10.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(CyanAccent, BlueAccent)))
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Telegram", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Save to Signal Journal / History
                Button(
                    onClick = {
                        onSaveToHistory(signal)
                        isSaved = true
                        Toast.makeText(context, "Saved to Signal Journal! 📈", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("save_journal_button_${signal.id}"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSaved) DarkSurfaceVariant else GoldPrimary,
                        contentColor = if (isSaved) GoldPrimary else DarkBackground
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = if (isSaved) Icons.Default.Check else Icons.Default.BookmarkAdd,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isSaved) "Recorded" else "Record", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Optional Outcome recorder or Binary Timer
                if (signal.marketType == MarketType.BINARY && expirySecondsLeft == 0 && onStartExpiryTimer != null) {
                    Button(
                        onClick = onStartExpiryTimer,
                        modifier = Modifier
                            .height(44.dp)
                            .testTag("start_timer_button_${signal.id}"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DarkSurfaceVariant,
                            contentColor = GoldAccent
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }

                if (onRecordOutcome != null) {
                    Button(
                        onClick = { showOutcomeDialog = true },
                        modifier = Modifier
                            .height(44.dp)
                            .testTag("record_outcome_button_${signal.id}"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DarkSurfaceVariant,
                            contentColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Result", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Dialog to record actual real trading outcome
    if (showOutcomeDialog) {
        AlertDialog(
            onDismissRequest = { showOutcomeDialog = false },
            title = {
                Text("Record Signal Result", color = GoldPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Record the real performance result for ${signal.pair}. Win rates update automatically in statistics.",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { selectedOutcome = SignalResult.WIN },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedOutcome == SignalResult.WIN) BullishGreen else DarkSurfaceVariant
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("WIN 🟢", fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { selectedOutcome = SignalResult.LOSS },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedOutcome == SignalResult.LOSS) BearishRed else DarkSurfaceVariant
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("LOSS 🔴", fontWeight = FontWeight.Bold)
                        }
                    }
                    OutlinedTextField(
                        value = pnlInput,
                        onValueChange = { pnlInput = it },
                        label = { Text("Profit / Loss ($)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = DarkCardBorder
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = pnlInput.toDoubleOrNull() ?: 0.0
                        val signedPnl = if (selectedOutcome == SignalResult.WIN) amount else -kotlin.math.abs(amount)
                        onRecordOutcome?.invoke(selectedOutcome, signedPnl)
                        showOutcomeDialog = false
                        Toast.makeText(context, "Outcome recorded! Statistics updated.", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = DarkBackground)
                ) {
                    Text("Save Result", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showOutcomeDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = DarkSurface
        )
    }
}

@Composable
fun FlowIndicatorPills(checks: List<IndicatorCheck>, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        checks.chunked(3).forEach { rowList ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                rowList.forEach { check ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (check.confirmed) BullishGreenBg else BearishRedBg
                            )
                            .border(
                                1.dp,
                                if (check.confirmed) BullishGreen.copy(alpha = 0.6f) else BearishRed.copy(alpha = 0.4f),
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${check.name} ${if (check.confirmed) "✅" else "⚠️"}",
                            color = if (check.confirmed) BullishGreen else BearishRed,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
