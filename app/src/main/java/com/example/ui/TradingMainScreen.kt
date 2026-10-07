package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppNavTab
import com.example.ui.viewmodel.TradingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TradingMainScreen(
    viewModel: TradingViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showMoreBottomSheet by remember { mutableStateOf(false) }

    // Intercept Back button on secondary screens to return to HOME
    BackHandler(enabled = uiState.currentTab != AppNavTab.HOME) {
        viewModel.selectTab(AppNavTab.HOME)
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground),
        containerColor = DarkBackground,
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkBackground)
            ) {
                // Top App Bar
                Surface(
                    color = DarkBackground,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        Brush.linearGradient(listOf(GoldPrimary, GoldDark))
                                    )
                                    .border(1.dp, GoldLight, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "QX",
                                    color = DarkBackground,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }

                            Column {
                                Text(
                                    text = "ZAKS QX AI",
                                    color = GoldPrimary,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "MARKET ANALYSIS SYSTEM",
                                    color = TextSecondary,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }

                        // Right quick buttons: Voice AI & Session
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            IconButton(
                                onClick = { viewModel.selectTab(AppNavTab.VOICE_AI) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(DarkSurfaceVariant, CircleShape)
                                    .testTag("top_voice_ai_button")
                            ) {
                                Icon(
                                    Icons.Default.Mic,
                                    contentDescription = "Voice AI",
                                    tint = if (uiState.currentTab == AppNavTab.VOICE_AI) GoldPrimary else TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (uiState.isForexOpen) BullishGreenBg else BearishRedBg)
                                    .border(1.dp, if (uiState.isForexOpen) BullishGreen.copy(alpha = 0.5f) else BearishRed.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (uiState.isForexOpen) "OPEN" else "CLOSED",
                                    color = if (uiState.isForexOpen) BullishGreen else BearishRed,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }

                // Top Navigation Bar (Section 1: HOME, BINARY, FOREX, OTC SCAN, SYNTHETIC, HISTORY, STATISTICS, SETTINGS)
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(AppNavTab.values()) { tab ->
                        val isSelected = uiState.currentTab == tab
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { viewModel.selectTab(tab) }
                                .background(if (isSelected) GoldPrimary else DarkSurface)
                                .border(
                                    1.dp,
                                    if (isSelected) GoldLight else DarkBorderSubtle,
                                    RoundedCornerShape(20.dp)
                                )
                                .padding(horizontal = 14.dp, vertical = 7.dp)
                                .testTag("top_nav_tab_${tab.name}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when (tab) {
                                    AppNavTab.HOME -> "HOME"
                                    AppNavTab.BINARY -> "BINARY"
                                    AppNavTab.FOREX -> "FOREX"
                                    AppNavTab.OTC_SCAN -> "OTC SCAN"
                                    AppNavTab.SYNTHETIC -> "SYNTHETIC"
                                    AppNavTab.SIGNAL_HISTORY -> "SIGNAL HISTORY"
                                    AppNavTab.STATISTICS -> "STATISTICS"
                                    AppNavTab.VOICE_AI -> "VOICE AI"
                                    AppNavTab.SETTINGS -> "SETTINGS"
                                },
                                color = if (isSelected) DarkBackground else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }

                Divider(color = DarkCardBorder, thickness = 1.dp)
            }
        },
        bottomBar = {
            // M3 Bottom Navigation Bar
            NavigationBar(
                containerColor = DarkSurface,
                contentColor = TextPrimary,
                tonalElevation = 8.dp,
                windowInsets = WindowInsets.navigationBars,
                modifier = Modifier
                    .border(1.dp, DarkCardBorder)
                    .testTag("app_bottom_nav_bar")
            ) {
                NavigationBarItem(
                    selected = uiState.currentTab == AppNavTab.HOME,
                    onClick = { viewModel.selectTab(AppNavTab.HOME) },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Home") },
                    label = { Text("Home", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = DarkBackground,
                        selectedTextColor = GoldPrimary,
                        indicatorColor = GoldPrimary,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )

                NavigationBarItem(
                    selected = uiState.currentTab == AppNavTab.BINARY,
                    onClick = { viewModel.selectTab(AppNavTab.BINARY) },
                    icon = { Icon(Icons.Default.HourglassTop, contentDescription = "Binary") },
                    label = { Text("Binary", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = DarkBackground,
                        selectedTextColor = GoldPrimary,
                        indicatorColor = GoldPrimary,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )

                NavigationBarItem(
                    selected = uiState.currentTab == AppNavTab.FOREX,
                    onClick = { viewModel.selectTab(AppNavTab.FOREX) },
                    icon = { Icon(Icons.Default.TrendingUp, contentDescription = "Forex") },
                    label = { Text("Forex", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = DarkBackground,
                        selectedTextColor = GoldPrimary,
                        indicatorColor = GoldPrimary,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )

                NavigationBarItem(
                    selected = uiState.currentTab == AppNavTab.OTC_SCAN,
                    onClick = { viewModel.selectTab(AppNavTab.OTC_SCAN) },
                    icon = { Icon(Icons.Default.Radar, contentDescription = "OTC") },
                    label = { Text("OTC", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = DarkBackground,
                        selectedTextColor = GoldPrimary,
                        indicatorColor = GoldPrimary,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )

                NavigationBarItem(
                    selected = uiState.currentTab == AppNavTab.SIGNAL_HISTORY,
                    onClick = { viewModel.selectTab(AppNavTab.SIGNAL_HISTORY) },
                    icon = { Icon(Icons.Default.History, contentDescription = "History") },
                    label = { Text("Journal", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = DarkBackground,
                        selectedTextColor = GoldPrimary,
                        indicatorColor = GoldPrimary,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )

                NavigationBarItem(
                    selected = uiState.currentTab in listOf(AppNavTab.SYNTHETIC, AppNavTab.STATISTICS, AppNavTab.VOICE_AI, AppNavTab.SETTINGS),
                    onClick = { showMoreBottomSheet = true },
                    icon = { Icon(Icons.Default.Menu, contentDescription = "More") },
                    label = { Text("More", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = DarkBackground,
                        selectedTextColor = GoldPrimary,
                        indicatorColor = GoldPrimary,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(DarkBackground)
        ) {
            when (uiState.currentTab) {
                AppNavTab.HOME -> {
                    HomeScreen(
                        uiState = uiState,
                        onNavigate = { viewModel.selectTab(it) },
                        onSaveSignal = { viewModel.saveSignalToHistory(it) },
                        onRecordOutcome = { id, res, pnl -> viewModel.recordSignalOutcome(id, res, pnl) },
                        onToggleSound = { viewModel.toggleSound(!uiState.soundEnabled) }
                    )
                }
                AppNavTab.BINARY -> {
                    BinaryScreen(
                        uiState = uiState,
                        onSelectPair = { p, otc -> viewModel.setBinaryPair(p, otc) },
                        onSelectTimeframe = { viewModel.setBinaryTimeframe(it) },
                        onSelectDirectionTarget = { viewModel.setBinaryDirectionTarget(it) },
                        onGenerateSignal = { viewModel.analyzeBinarySignal() },
                        onStartCountdown = { viewModel.startBinaryCountdown(it) },
                        onSaveSignal = { viewModel.saveSignalToHistory(it) },
                        onRecordOutcome = { id, res, pnl -> viewModel.recordSignalOutcome(id, res, pnl) }
                    )
                }
                AppNavTab.FOREX -> {
                    ForexScreen(
                        uiState = uiState,
                        onSelectPair = { viewModel.setForexPair(it) },
                        onSelectTimeframe = { viewModel.setForexTimeframe(it) },
                        onGenerateSignal = { viewModel.analyzeForexSignal() },
                        onSaveSignal = { viewModel.saveSignalToHistory(it) },
                        onRecordOutcome = { id, res, pnl -> viewModel.recordSignalOutcome(id, res, pnl) }
                    )
                }
                AppNavTab.OTC_SCAN -> {
                    OtcScanScreen(
                        uiState = uiState,
                        onSelectTimeframe = { viewModel.setOtcScanTimeframe(it) },
                        onRescan = { viewModel.scanOtcPairs() },
                        onSaveSignal = { viewModel.saveSignalToHistory(it) },
                        onRecordOutcome = { id, res, pnl -> viewModel.recordSignalOutcome(id, res, pnl) }
                    )
                }
                AppNavTab.SYNTHETIC -> {
                    SyntheticScreen(
                        uiState = uiState,
                        onSelectInstrument = { viewModel.setSyntheticInstrument(it) },
                        onSelectTimeframe = { viewModel.setSyntheticTimeframe(it) },
                        onGenerateSignal = { viewModel.analyzeSyntheticSignal() },
                        onSaveSignal = { viewModel.saveSignalToHistory(it) },
                        onRecordOutcome = { id, res, pnl -> viewModel.recordSignalOutcome(id, res, pnl) }
                    )
                }
                AppNavTab.SIGNAL_HISTORY -> {
                    SignalHistoryScreen(
                        uiState = uiState,
                        onFilterMarket = { viewModel.filterHistoryByMarket(it) },
                        onFilterResult = { viewModel.filterHistoryByResult(it) },
                        onRecordOutcome = { id, res, pnl -> viewModel.recordSignalOutcome(id, res, pnl) },
                        onDeleteSignal = { viewModel.deleteSignal(it) },
                        onClearAll = { viewModel.clearSignalHistory() }
                    )
                }
                AppNavTab.STATISTICS -> {
                    StatisticsScreen(uiState = uiState)
                }
                AppNavTab.VOICE_AI -> {
                    VoiceAiScreen(
                        uiState = uiState,
                        onSelectLanguage = { viewModel.setVoiceLanguage(it) },
                        onSendQuery = { viewModel.processVoiceQuery(it) },
                        onSpeakResult = { viewModel.speakVoiceResult() },
                        onStopSpeaking = { viewModel.stopSpeaking() },
                        onSaveSignal = { viewModel.saveSignalToHistory(it) }
                    )
                }
                AppNavTab.SETTINGS -> {
                    SettingsScreen(
                        uiState = uiState,
                        onSelectLanguage = { viewModel.setVoiceLanguage(it) },
                        onToggleSound = { viewModel.toggleSound(it) },
                        onSetRiskPreference = { viewModel.setRiskPreference(it) },
                        onUpdateTelegram = { token, chat -> viewModel.updateTelegramConfig(token, chat) },
                        onUpdateApiKey = { viewModel.updateApiKey(it) }
                    )
                }
            }
        }
    }

    // More destinations Bottom Sheet
    if (showMoreBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showMoreBottomSheet = false },
            containerColor = DarkSurface,
            scrimColor = Color.Black.copy(alpha = 0.6f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "ALL NAVIGATION MODULES",
                    color = GoldPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )

                val moreItems = listOf(
                    Triple(AppNavTab.SYNTHETIC, "Synthetic Indices", Icons.Default.AutoGraph),
                    Triple(AppNavTab.STATISTICS, "Performance Statistics", Icons.Default.BarChart),
                    Triple(AppNavTab.VOICE_AI, "Voice AI Analysis", Icons.Default.Mic),
                    Triple(AppNavTab.SETTINGS, "Settings & Integrations", Icons.Default.Settings)
                )

                moreItems.forEach { (tab, title, icon) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                viewModel.selectTab(tab)
                                showMoreBottomSheet = false
                            }
                            .background(if (uiState.currentTab == tab) DarkSurfaceVariant else DarkSurface)
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = if (uiState.currentTab == tab) GoldPrimary else TextSecondary
                        )
                        Text(
                            text = title,
                            color = if (uiState.currentTab == tab) GoldPrimary else TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}
