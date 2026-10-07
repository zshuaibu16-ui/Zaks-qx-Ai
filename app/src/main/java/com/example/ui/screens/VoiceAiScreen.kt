package com.example.ui.screens

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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

data class SupportedLanguage(
    val code: String,
    val name: String,
    val flag: String,
    val samplePrompt: String
)

@Composable
fun VoiceAiScreen(
    uiState: TradingUiState,
    onSelectLanguage: (String) -> Unit,
    onSendQuery: (String) -> Unit,
    onSpeakResult: () -> Unit,
    onStopSpeaking: () -> Unit,
    onSaveSignal: (TradingSignal) -> Unit,
    modifier: Modifier = Modifier
) {
    val languages = listOf(
        SupportedLanguage("en", "English", "🇺🇸", "Analyze EUR/USD on 15 minutes"),
        SupportedLanguage("ha", "Hausa", "🇳🇬", "Bincika EUR/USD a minti 15"),
        SupportedLanguage("ar", "Arabic (العربية)", "🇸🇦", "تحليل EUR/USD على 15 دقيقة"),
        SupportedLanguage("fr", "French (Français)", "🇫🇷", "Analyser EUR/USD sur 15 minutes")
    )

    var inputPrompt by remember { mutableStateOf("") }

    // Speech-to-Text launcher
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = matches?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                inputPrompt = spokenText
                onSendQuery(spokenText)
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("voice_ai_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section Header
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "VOICE ANALYSIS & AI ADVISOR",
                    color = GoldPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Ask questions in English, Hausa, Arabic, or French. Receive institutional technical breakdown and audio voice readout.",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
        }

        // Language Selector
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "SELECT ANALYSIS LANGUAGE",
                    color = TextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(languages) { lang ->
                        val isSelected = uiState.voiceLanguage == lang.code
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onSelectLanguage(lang.code) }
                                .background(if (isSelected) GoldPrimary else DarkSurfaceVariant)
                                .border(
                                    1.dp,
                                    if (isSelected) GoldLight else DarkBorderSubtle,
                                    RoundedCornerShape(10.dp)
                                )
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "${lang.flag} ${lang.name}",
                                color = if (isSelected) DarkBackground else TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Voice Command Input & Microphone
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
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "VOICE PROMPT OR TEXT QUERY",
                        color = GoldPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )

                    OutlinedTextField(
                        value = inputPrompt,
                        onValueChange = { inputPrompt = it },
                        placeholder = {
                            val activeSample = languages.firstOrNull { it.code == uiState.voiceLanguage }?.samplePrompt
                                ?: "Analyze EUR/USD on 15 minutes"
                            Text(activeSample, color = TextTertiary, fontSize = 13.sp)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        trailingIcon = {
                            if (inputPrompt.isNotBlank()) {
                                IconButton(onClick = { inputPrompt = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextSecondary)
                                }
                            }
                        }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Microphone Trigger Button
                        Button(
                            onClick = {
                                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                    val localeCode = when (uiState.voiceLanguage) {
                                        "ha" -> "ha-NG"
                                        "ar" -> "ar-SA"
                                        "fr" -> "fr-FR"
                                        else -> "en-US"
                                    }
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, localeCode)
                                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak asset & timeframe (e.g., Analyze EUR/USD on 15 minutes)")
                                }
                                speechLauncher.launch(intent)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("mic_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DarkSurfaceVariant,
                                contentColor = GoldPrimary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(GoldPrimary))
                        ) {
                            Icon(Icons.Default.Mic, contentDescription = "Speak", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Speak Voice", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        // Send / Run Analysis
                        Button(
                            onClick = {
                                if (inputPrompt.isNotBlank()) {
                                    onSendQuery(inputPrompt)
                                } else {
                                    val sample = languages.firstOrNull { it.code == uiState.voiceLanguage }?.samplePrompt
                                        ?: "Analyze EUR/USD on 15 minutes"
                                    inputPrompt = sample
                                    onSendQuery(sample)
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("run_voice_analysis_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GoldPrimary,
                                contentColor = DarkBackground
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Run AI Analysis", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                        }
                    }

                    // Preset prompt chips in selected language
                    Text("SAMPLE COMMANDS (TAP TO RUN):", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        val presets = when (uiState.voiceLanguage) {
                            "ha" -> listOf("Bincika EUR/USD a minti 15", "Bincika GBP/JPY a minti 5", "Bincika EUR/USD OTC a minti 1")
                            "ar" -> listOf("تحليل EUR/USD على 15 دقيقة", "تحليل GBP/USD على 5 دقائق", "تحليل USD/JPY على ساعة")
                            "fr" -> listOf("Analyser EUR/USD sur 15 minutes", "Analyser GBP/USD sur 5 minutes", "Analyser EUR/USD OTC sur 3 minutes")
                            else -> listOf("Analyze EUR/USD on 15 minutes", "Analyze GBP/USD on 5 minutes", "Analyze EUR/USD OTC on 1 minute")
                        }
                        presets.forEach { preset ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        inputPrompt = preset
                                        onSendQuery(preset)
                                    }
                                    .background(DarkSurfaceVariant)
                                    .padding(horizontal = 10.dp, vertical = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(preset, color = TextPrimary, fontSize = 11.sp)
                                    Icon(Icons.Default.ArrowForward, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Voice Analysis Output Panel
        val voiceRes = uiState.voiceResult
        if (voiceRes != null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, GoldPrimary.copy(alpha = 0.6f), RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.Psychology, contentDescription = null, tint = GoldPrimary)
                                Text(
                                    text = "AI TECHNICAL SYNTHESIS",
                                    color = GoldPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                IconButton(onClick = onSpeakResult) {
                                    Icon(Icons.Default.VolumeUp, contentDescription = "Listen", tint = BullishGreen)
                                }
                                IconButton(onClick = onStopSpeaking) {
                                    Icon(Icons.Default.Stop, contentDescription = "Stop", tint = BearishRed)
                                }
                            }
                        }

                        // Structured AI response
                        Text(
                            text = voiceRes.spokenText,
                            color = TextPrimary,
                            fontSize = 13.sp,
                            lineHeight = 20.sp
                        )

                        Divider(color = DarkCardBorder)

                        // Technical points breakdown
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row {
                                Text("• TREND: ", color = GoldLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text(voiceRes.signal.marketStructure.trend, color = TextPrimary, fontSize = 11.sp)
                            }
                            Row {
                                Text("• STRUCTURE: ", color = GoldLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("${voiceRes.signal.marketStructure.structure} (${voiceRes.signal.marketStructure.keyEvent})", color = TextPrimary, fontSize = 11.sp)
                            }
                            Row {
                                Text("• CONFLUENCE: ", color = GoldLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("${voiceRes.signal.confidenceScore}% (${voiceRes.signal.confidenceGrade.label})", color = BullishGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Row {
                                Text("• RISK NOTICE: ", color = BearishRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("Never risk more than 1-2% account equity per trade.", color = TextSecondary, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // Signal card generated from the voice query
            item {
                SignalCard(
                    signal = voiceRes.signal,
                    onSaveToHistory = onSaveSignal
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
