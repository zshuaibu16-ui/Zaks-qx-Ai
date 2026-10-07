package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.market.MarketDataService
import com.example.data.repository.SignalRepository
import com.example.data.voice.VoiceAiService
import com.example.data.voice.VoiceAnalysisResult
import com.example.model.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class AppNavTab(val title: String) {
    HOME("Home"),
    BINARY("Binary"),
    FOREX("Forex"),
    OTC_SCAN("OTC Scan"),
    SYNTHETIC("Synthetic"),
    SIGNAL_HISTORY("History"),
    STATISTICS("Statistics"),
    VOICE_AI("Voice AI"),
    SETTINGS("Settings")
}

data class OtcScanItem(
    val pair: String,
    val timeframe: String,
    val signal: TradingSignal,
    val isScanning: Boolean = false
)

data class TradingUiState(
    val currentTab: AppNavTab = AppNavTab.HOME,
    // Market Status
    val sessions: List<MarketSessionStatus> = emptyList(),
    val primarySession: String = "London",
    val isForexOpen: Boolean = true,
    val liveAnalysisActive: Boolean = true,
    // Performance Statistics (Strictly computed from real recorded database signals)
    val stats: PerformanceStats = PerformanceStats(),
    val signalsHistory: List<TradingSignal> = emptyList(),
    // Binary Tab State
    val binaryPair: String = "EUR/USD",
    val binaryIsOtc: Boolean = false,
    val binaryTimeframe: String = "3M",
    val binaryDirectionTarget: Boolean? = null,
    val currentBinarySignal: TradingSignal? = null,
    val binaryExpirySecondsLeft: Int = 0,
    // Forex Tab State
    val forexPair: String = "EUR/USD",
    val forexTimeframe: String = "15M",
    val currentForexSignal: TradingSignal? = null,
    // OTC Scan State
    val otcScanTimeframe: String = "3M",
    val otcScanResults: List<OtcScanItem> = emptyList(),
    val isOtcScanning: Boolean = false,
    // Synthetic Tab State
    val syntheticInstrument: String = "Volatility 75 (1s)",
    val syntheticTimeframe: String = "15M",
    val currentSyntheticSignal: TradingSignal? = null,
    // Voice AI State
    val voiceLanguage: String = "en",
    val isListening: Boolean = false,
    val voiceResult: VoiceAnalysisResult? = null,
    // History Filter
    val historyFilterMarket: MarketType? = null,
    val historyFilterResult: SignalResult? = null,
    // Settings
    val riskPreference: String = "Balanced (75%)",
    val soundEnabled: Boolean = true,
    val telegramBotToken: String = "",
    val telegramChatId: String = "",
    val apiKey: String = "",
    val isDarkTheme: Boolean = true
)

class TradingViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = SignalRepository(database.signalDao())
    val voiceService = VoiceAiService(application)

    private val _uiState = MutableStateFlow(TradingUiState())
    val uiState: StateFlow<TradingUiState> = _uiState.asStateFlow()

    private var binaryCountdownJob: Job? = null

    init {
        // Collect real signals from Room database
        viewModelScope.launch {
            repository.allSignals.collect { list ->
                val calculatedStats = repository.computeStats(list)
                _uiState.update { state ->
                    state.copy(
                        signalsHistory = list,
                        stats = calculatedStats
                    )
                }
            }
        }

        // Initialize market status
        updateMarketStatus()

        // Generate initial signals for preview
        analyzeBinarySignal()
        analyzeForexSignal()
        analyzeSyntheticSignal()
        scanOtcPairs()
    }

    fun selectTab(tab: AppNavTab) {
        _uiState.update { it.copy(currentTab = tab) }
    }

    private fun updateMarketStatus() {
        _uiState.update {
            it.copy(
                sessions = MarketDataService.getCurrentSessions(),
                primarySession = MarketDataService.getPrimarySessionName(),
                isForexOpen = MarketDataService.isForexMarketOpen()
            )
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Binary Operations
    // ─────────────────────────────────────────────────────────────

    fun setBinaryPair(pair: String, isOtc: Boolean = false) {
        _uiState.update { it.copy(binaryPair = pair, binaryIsOtc = isOtc) }
        analyzeBinarySignal()
    }

    fun setBinaryTimeframe(timeframe: String) {
        _uiState.update { it.copy(binaryTimeframe = timeframe) }
        analyzeBinarySignal()
    }

    fun setBinaryDirectionTarget(isCall: Boolean?) {
        _uiState.update { it.copy(binaryDirectionTarget = isCall) }
        analyzeBinarySignal()
    }

    fun analyzeBinarySignal() {
        val state = _uiState.value
        val pairToAnalyze = if (state.binaryIsOtc && !state.binaryPair.contains("OTC")) {
            "${state.binaryPair} OTC"
        } else state.binaryPair

        val signal = MarketDataService.analyzeSignal(
            pair = pairToAnalyze,
            marketType = if (pairToAnalyze.contains("OTC")) MarketType.OTC else MarketType.BINARY,
            timeframe = state.binaryTimeframe,
            requestedDirection = state.binaryDirectionTarget
        )

        _uiState.update { it.copy(currentBinarySignal = signal) }
        triggerHaptic()
    }

    fun startBinaryCountdown(signal: TradingSignal) {
        binaryCountdownJob?.cancel()
        val totalSecs = signal.expiryMinutes * 60
        _uiState.update { it.copy(binaryExpirySecondsLeft = totalSecs) }

        binaryCountdownJob = viewModelScope.launch {
            var left = totalSecs
            while (left > 0) {
                delay(1000)
                left--
                _uiState.update { it.copy(binaryExpirySecondsLeft = left) }
            }
            // Auto mark expired/ready to record
            _uiState.update { it.copy(binaryExpirySecondsLeft = 0) }
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Forex Operations
    // ─────────────────────────────────────────────────────────────

    fun setForexPair(pair: String) {
        _uiState.update { it.copy(forexPair = pair) }
        analyzeForexSignal()
    }

    fun setForexTimeframe(timeframe: String) {
        _uiState.update { it.copy(forexTimeframe = timeframe) }
        analyzeForexSignal()
    }

    fun analyzeForexSignal() {
        val state = _uiState.value
        val signal = MarketDataService.analyzeSignal(
            pair = state.forexPair,
            marketType = MarketType.FOREX,
            timeframe = state.forexTimeframe
        )
        _uiState.update { it.copy(currentForexSignal = signal) }
        triggerHaptic()
    }

    // ─────────────────────────────────────────────────────────────
    // OTC Operations
    // ─────────────────────────────────────────────────────────────

    fun setOtcScanTimeframe(timeframe: String) {
        _uiState.update { it.copy(otcScanTimeframe = timeframe) }
        scanOtcPairs()
    }

    fun scanOtcPairs() {
        viewModelScope.launch {
            _uiState.update { it.copy(isOtcScanning = true) }
            val timeframe = _uiState.value.otcScanTimeframe
            val items = MarketDataService.OTC_PAIRS.map { otcPair ->
                val signal = MarketDataService.analyzeSignal(otcPair, MarketType.OTC, timeframe)
                OtcScanItem(pair = otcPair, timeframe = timeframe, signal = signal)
            }.sortedByDescending { it.signal.confidenceScore }

            delay(400) // Brief scan animation
            _uiState.update { it.copy(otcScanResults = items, isOtcScanning = false) }
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Synthetic Operations
    // ─────────────────────────────────────────────────────────────

    fun setSyntheticInstrument(instrument: String) {
        _uiState.update { it.copy(syntheticInstrument = instrument) }
        analyzeSyntheticSignal()
    }

    fun setSyntheticTimeframe(timeframe: String) {
        _uiState.update { it.copy(syntheticTimeframe = timeframe) }
        analyzeSyntheticSignal()
    }

    fun analyzeSyntheticSignal() {
        val state = _uiState.value
        val signal = MarketDataService.analyzeSignal(
            pair = state.syntheticInstrument,
            marketType = MarketType.SYNTHETIC,
            timeframe = state.syntheticTimeframe
        )
        _uiState.update { it.copy(currentSyntheticSignal = signal) }
        triggerHaptic()
    }

    // ─────────────────────────────────────────────────────────────
    // Signal Recording & History
    // ─────────────────────────────────────────────────────────────

    fun saveSignalToHistory(signal: TradingSignal) {
        viewModelScope.launch {
            repository.saveSignal(signal)
            triggerHaptic()
        }
    }

    fun recordSignalOutcome(id: String, result: SignalResult, pnl: Double = 0.0) {
        viewModelScope.launch {
            repository.updateResult(id, result, pnl)
            triggerHaptic()
        }
    }

    fun deleteSignal(id: String) {
        viewModelScope.launch {
            repository.deleteSignal(id)
        }
    }

    fun clearSignalHistory() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }

    fun filterHistoryByMarket(marketType: MarketType?) {
        _uiState.update { it.copy(historyFilterMarket = marketType) }
    }

    fun filterHistoryByResult(result: SignalResult?) {
        _uiState.update { it.copy(historyFilterResult = result) }
    }

    // ─────────────────────────────────────────────────────────────
    // Voice AI Operations
    // ─────────────────────────────────────────────────────────────

    fun setVoiceLanguage(lang: String) {
        _uiState.update { it.copy(voiceLanguage = lang) }
    }

    fun processVoiceQuery(query: String) {
        val lang = _uiState.value.voiceLanguage
        val result = voiceService.processVoiceQuery(query, lang)
        _uiState.update { it.copy(voiceResult = result) }
        if (_uiState.value.soundEnabled) {
            voiceService.speak(result.spokenText, lang)
        }
    }

    fun speakVoiceResult() {
        val result = _uiState.value.voiceResult ?: return
        voiceService.speak(result.spokenText, _uiState.value.voiceLanguage)
    }

    fun stopSpeaking() {
        voiceService.stopSpeaking()
    }

    // ─────────────────────────────────────────────────────────────
    // Settings Operations
    // ─────────────────────────────────────────────────────────────

    fun setRiskPreference(pref: String) {
        _uiState.update { it.copy(riskPreference = pref) }
    }

    fun toggleSound(enabled: Boolean) {
        _uiState.update { it.copy(soundEnabled = enabled) }
    }

    fun updateTelegramConfig(token: String, chatId: String) {
        _uiState.update { it.copy(telegramBotToken = token, telegramChatId = chatId) }
    }

    fun updateApiKey(key: String) {
        _uiState.update { it.copy(apiKey = key) }
    }

    private fun triggerHaptic() {
        try {
            val context = getApplication<Application>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                vibrator?.vibrate(30)
            }
        } catch (_: Exception) {}
    }

    override fun onCleared() {
        super.onCleared()
        binaryCountdownJob?.cancel()
        voiceService.destroy()
    }
}
