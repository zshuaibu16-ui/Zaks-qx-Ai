package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.ui.theme.*
import com.example.ui.viewmodel.TradingUiState

@Composable
fun SettingsScreen(
    uiState: TradingUiState,
    onSelectLanguage: (String) -> Unit,
    onToggleSound: (Boolean) -> Unit,
    onSetRiskPreference: (String) -> Unit,
    onUpdateTelegram: (String, String) -> Unit,
    onUpdateApiKey: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var botTokenInput by remember { mutableStateOf(uiState.telegramBotToken) }
    var chatIdInput by remember { mutableStateOf(uiState.telegramChatId) }
    var apiKeyInput by remember { mutableStateOf(uiState.apiKey) }

    val languages = listOf(
        "en" to "English 🇺🇸",
        "ha" to "Hausa 🇳🇬",
        "ar" to "Arabic 🇸🇦",
        "fr" to "French 🇫🇷"
    )

    val riskOptions = listOf(
        "Conservative (85% Min)",
        "Balanced (75% Min)",
        "Aggressive (70% Min)"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "SETTINGS & INTEGRATIONS",
                    color = GoldPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Customize market analysis parameters, audio notifications, APIs, and Telegram bot connectivity.",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
        }

        // Language & Localization
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
                        text = "APP & VOICE LANGUAGE",
                        color = GoldPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )

                    languages.forEach { (code, label) ->
                        val isSelected = uiState.voiceLanguage == code
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onSelectLanguage(code) }
                                .background(if (isSelected) DarkSurfaceVariant else DarkSurface)
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(label, color = TextPrimary, fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }

        // Risk Preference & Confluence Threshold
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
                        text = "RISK PROFILE & CONFIDENCE FILTER",
                        color = GoldPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )

                    riskOptions.forEach { opt ->
                        val isSelected = uiState.riskPreference.startsWith(opt.take(4))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onSetRiskPreference(opt) }
                                .background(if (isSelected) DarkSurfaceVariant else DarkSurface)
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(opt, color = TextPrimary, fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }

        // Audio Alerts & Sounds
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkCardBorder, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("SIGNAL AUDIO & TTS", color = GoldPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("Play sound effects and speak AI analysis", color = TextSecondary, fontSize = 11.sp)
                    }
                    Switch(
                        checked = uiState.soundEnabled,
                        onCheckedChange = { onToggleSound(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = GoldPrimary,
                            checkedTrackColor = GoldDark
                        )
                    )
                }
            }
        }

        // Telegram Bot Integration (Section 19)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CyanAccent.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, tint = CyanAccent)
                        Text(
                            text = "TELEGRAM BOT INTEGRATION",
                            color = CyanAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Text(
                        text = "Connect a Telegram Bot token and Channel ID to broadcast formatted signals with one tap.",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )

                    OutlinedTextField(
                        value = botTokenInput,
                        onValueChange = { botTokenInput = it },
                        label = { Text("Telegram Bot Token (e.g. 123456:ABC...)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanAccent,
                            unfocusedBorderColor = DarkCardBorder
                        )
                    )

                    OutlinedTextField(
                        value = chatIdInput,
                        onValueChange = { chatIdInput = it },
                        label = { Text("Channel Chat ID (e.g. @my_signals or -100...)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanAccent,
                            unfocusedBorderColor = DarkCardBorder
                        )
                    )

                    Button(
                        onClick = {
                            onUpdateTelegram(botTokenInput, chatIdInput)
                            Toast.makeText(context, "Telegram configuration saved! ✈️", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = DarkBackground),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Save Telegram Config", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // External Market Data API Configuration (Section 17)
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.CloudSync, contentDescription = null, tint = GoldPrimary)
                        Text(
                            text = "MARKET DATA SERVICE API",
                            color = GoldPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Text(
                        text = "Configurable gateway for external market providers (TwelveData, Finnhub, Alpha Vantage, Deriv WebSocket).",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )

                    OutlinedTextField(
                        value = apiKeyInput,
                        onValueChange = { apiKeyInput = it },
                        label = { Text("Custom Market API Key") },
                        placeholder = { Text("Configured via Secrets panel or enter custom key") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = DarkCardBorder
                        )
                    )

                    Button(
                        onClick = {
                            onUpdateApiKey(apiKeyInput)
                            Toast.makeText(context, "API Key updated successfully! 🔑", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = DarkBackground),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Save API Settings", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
