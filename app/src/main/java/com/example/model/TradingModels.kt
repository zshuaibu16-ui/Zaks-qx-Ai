package com.example.model

enum class MarketType(val displayName: String) {
    BINARY("Binary Options"),
    FOREX("Forex"),
    OTC("OTC Market"),
    SYNTHETIC("Synthetic Indices")
}

enum class SignalDirection(val displayName: String, val isBullish: Boolean) {
    BUY("BUY 🟢", true),
    SELL("SELL 🔴", false),
    CALL("CALL 🟢", true),
    PUT("PUT 🔴", false),
    NO_TRADE("NO TRADE ⚠️", false)
}

enum class SignalGrade(val label: String, val minScore: Int) {
    VERY_STRONG("VERY STRONG", 90),
    STRONG("STRONG", 80),
    MODERATE("MODERATE", 70),
    NO_TRADE("WAIT / NO TRADE", 0)
}

enum class SignalResult {
    PENDING,
    WIN,
    LOSS,
    EXPIRED
}

data class IndicatorCheck(
    val name: String,
    val isBullish: Boolean,
    val confirmed: Boolean,
    val valueStr: String,
    val weight: Int,
    val contribution: Int,
    val notes: String
)

data class CandlestickInfo(
    val patternName: String,
    val isBullish: Boolean,
    val significance: String
)

data class MarketStructureInfo(
    val trend: String, // Bullish, Bearish, Ranging
    val structure: String, // Higher High (HH), Higher Low (HL), Lower High (LH), Lower Low (LL)
    val keyEvent: String, // Breakout, Break of Structure (BOS), Change of Character (CHoCH)
    val supportLevel: Double,
    val resistanceLevel: Double
)

data class Candle(
    val timestamp: Long,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Double
)

data class TradingSignal(
    val id: String,
    val pair: String,
    val marketType: MarketType,
    val direction: SignalDirection,
    val timeframe: String,
    val entryPrice: Double,
    val stopLoss: Double = 0.0,
    val takeProfit1: Double = 0.0,
    val takeProfit2: Double = 0.0,
    val riskReward: String = "1:2",
    val confidenceScore: Int,
    val confidenceGrade: SignalGrade,
    val indicatorChecks: List<IndicatorCheck>,
    val detectedPattern: String,
    val marketStructure: MarketStructureInfo,
    val reason: String,
    val technicalExplanation: String,
    val timestamp: Long = System.currentTimeMillis(),
    val expiryMinutes: Int = 3,
    val result: SignalResult = SignalResult.PENDING,
    val pnl: Double = 0.0,
    val isOTC: Boolean = false,
    val isSynthetic: Boolean = false
) {
    val formattedEntry: String
        get() = String.format(java.util.Locale.US, if (pair.contains("JPY") || isSynthetic) "%.3f" else "%.5f", entryPrice)

    val formattedSL: String
        get() = if (stopLoss > 0) String.format(java.util.Locale.US, if (pair.contains("JPY") || isSynthetic) "%.3f" else "%.5f", stopLoss) else "N/A"

    val formattedTP1: String
        get() = if (takeProfit1 > 0) String.format(java.util.Locale.US, if (pair.contains("JPY") || isSynthetic) "%.3f" else "%.5f", takeProfit1) else "N/A"

    val formattedTP2: String
        get() = if (takeProfit2 > 0) String.format(java.util.Locale.US, if (pair.contains("JPY") || isSynthetic) "%.3f" else "%.5f", takeProfit2) else "N/A"

    fun toTelegramFormat(): String {
        return buildString {
            appendLine("━━━━━━━━━━━━━━━━━━━")
            appendLine("⚡ ZAKS QX AI SIGNAL")
            appendLine("━━━━━━━━━━━━━━━━━━━")
            appendLine("📊 Asset: $pair" + if (isOTC) " (OTC)" else "")
            appendLine("🎯 Direction: ${direction.displayName}")
            appendLine("⏱️ Timeframe: $timeframe" + if (marketType == MarketType.BINARY) " (Expiry: ${expiryMinutes}M)" else "")
            appendLine("💵 Entry: $formattedEntry")
            if (marketType == MarketType.FOREX || marketType == MarketType.SYNTHETIC) {
                appendLine("🛑 SL: $formattedSL")
                appendLine("🎯 TP1: $formattedTP1")
                appendLine("🎯 TP2: $formattedTP2")
                appendLine("⚖️ Risk/Reward: $riskReward")
            }
            appendLine("🔥 Confidence: $confidenceScore% [${confidenceGrade.label}]")
            appendLine()
            appendLine("📈 Confluence Check:")
            indicatorChecks.forEach { check ->
                val mark = if (check.confirmed) "✅" else "⚠️"
                appendLine("$mark ${check.name}: ${check.valueStr}")
            }
            if (detectedPattern.isNotBlank() && detectedPattern != "None") {
                appendLine("🕯️ Pattern: $detectedPattern ✅")
            }
            appendLine("🏛️ Market Structure: ${marketStructure.trend} (${marketStructure.keyEvent})")
            appendLine()
            appendLine("💡 Reason:")
            appendLine(reason)
            appendLine("━━━━━━━━━━━━━━━━━━━")
            appendLine("⚠️ Educational & research purpose only. Never risk money you cannot afford to lose.")
        }
    }
}

data class MarketSessionStatus(
    val sessionName: String,
    val isOpen: Boolean,
    val openTimeUtc: String,
    val closeTimeUtc: String
)

data class PerformanceStats(
    val totalSignals: Int = 0,
    val wins: Int = 0,
    val losses: Int = 0,
    val winRate: Double = 0.0,
    val todayWins: Int = 0,
    val todayLosses: Int = 0,
    val todayWinRate: Double = 0.0,
    val todayPnl: Double = 0.0,
    val totalPnl: Double = 0.0,
    val weeklyWinRate: Double = 0.0,
    val monthlyWinRate: Double = 0.0,
    val pendingCount: Int = 0
)
