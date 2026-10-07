package com.example.data.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import com.example.data.market.MarketDataService
import com.example.model.*
import java.util.Locale

class VoiceAiService(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    init {
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.US
            isTtsReady = true
        }
    }

    fun speak(text: String, languageCode: String = "en") {
        if (!isTtsReady || tts == null) return
        val targetLocale = when (languageCode) {
            "ha" -> Locale("ha")
            "ar" -> Locale("ar")
            "fr" -> Locale.FRENCH
            else -> Locale.US
        }
        val result = tts?.setLanguage(targetLocale)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            tts?.language = Locale.US
        }
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "ZAKS_TTS_${System.currentTimeMillis()}")
    }

    fun stopSpeaking() {
        tts?.stop()
    }

    fun destroy() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }

    /**
     * Parse query text in English, Hausa, Arabic, or French and analyze the target pair.
     */
    fun processVoiceQuery(query: String, languageCode: String = "en"): VoiceAnalysisResult {
        val clean = query.uppercase()

        // Extract pair
        val detectedPair = MarketDataService.FOREX_PAIRS.firstOrNull { clean.contains(it.replace("/", "")) || clean.contains(it) }
            ?: MarketDataService.OTC_PAIRS.firstOrNull { clean.contains(it.replace("/", "").replace(" OTC", "")) }
            ?: "EUR/USD"

        // Extract timeframe
        val detectedTimeframe = when {
            clean.contains("1M") || clean.contains("1 MINUTE") || clean.contains("MINTI 1") || clean.contains("دقيقة") || clean.contains("1 MINUTE") -> "1M"
            clean.contains("5M") || clean.contains("5 MINUTES") || clean.contains("MINUTES") || clean.contains("MINTI 5") || clean.contains("5 دقائق") -> "5M"
            clean.contains("15M") || clean.contains("15 MINUTES") || clean.contains("MINTI 15") || clean.contains("15 دقيقة") -> "15M"
            clean.contains("30M") || clean.contains("30 MINUTES") || clean.contains("MINTI 30") || clean.contains("30 دقيقة") -> "30M"
            clean.contains("1H") || clean.contains("1 HOUR") || clean.contains("AWA 1") || clean.contains("ساعة") || clean.contains("1 HEURE") -> "1H"
            else -> "15M"
        }

        val marketType = if (detectedPair.contains("OTC")) MarketType.OTC else MarketType.FOREX
        val signal = MarketDataService.analyzeSignal(detectedPair, marketType, detectedTimeframe)

        val spokenExplanation = generateSpokenResponse(signal, languageCode)

        return VoiceAnalysisResult(
            query = query,
            pair = detectedPair,
            timeframe = detectedTimeframe,
            signal = signal,
            spokenText = spokenExplanation
        )
    }

    private fun generateSpokenResponse(signal: TradingSignal, lang: String): String {
        val directionStr = if (signal.direction == SignalDirection.NO_TRADE) "Wait and No Trade"
        else if (signal.direction.isBullish) "Bullish Buy" else "Bearish Sell"

        return when (lang) {
            "ha" -> buildString {
                append("Binciken ZAKS QX AI na ${signal.pair} a lokacin ${signal.timeframe}. ")
                append("Hanya tana nuna: ${if (signal.direction.isBullish) "Hauwa (Kira)" else "Sauka (Sayarwa)"}. ")
                append("Tabbaci shine kashi ${signal.confidenceScore} cikin dari. ")
                append("Tsarin kasuwa: ${signal.marketStructure.trend}. ")
                append("Dalili: ${signal.reason}. ")
                append("Kada ka manta, babu tabbacin riba a kasuwa, kiyaye hadarin kudi.")
            }
            "ar" -> buildString {
                append("تقرير ZAKS QX AI لزوج ${signal.pair} على إطار ${signal.timeframe}. ")
                append("الاتجاه الفني: ${if (signal.direction.isBullish) "شراء صاعد" else "بيع هابط"}. ")
                append("نسبة الثقة ${signal.confidenceScore} بالمئة. ")
                append("هيكل السوق: ${signal.marketStructure.trend}. ")
                append("السبب: ${signal.reason}. ")
                append("تذكر دائما: لا توجد أرباح مضمونة، التزم بإدارة المخاطر الصارمة.")
            }
            "fr" -> buildString {
                append("Analyse ZAKS QX AI pour ${signal.pair} sur l'unité de temps ${signal.timeframe}. ")
                append("Orientation: $directionStr. ")
                append("Niveau de confiance: ${signal.confidenceScore} pourcent. ")
                append("Structure du marché: ${signal.marketStructure.trend}. ")
                append("Raison: ${signal.reason}. ")
                append("Avertissement: Les performances passées ne garantissent pas les gains futurs.")
            }
            else -> buildString {
                append("ZAKS QX AI analysis for ${signal.pair} on ${signal.timeframe} timeframe. ")
                append("Trend bias is $directionStr with ${signal.confidenceScore} percent confluence confidence. ")
                append("Market structure is ${signal.marketStructure.trend} with ${signal.marketStructure.keyEvent}. ")
                append("Primary indicators: ${signal.reason}. ")
                append("Risk warning: No signal is guaranteed. Always manage your capital responsibly.")
            }
        }
    }
}

data class VoiceAnalysisResult(
    val query: String,
    val pair: String,
    val timeframe: String,
    val signal: TradingSignal,
    val spokenText: String
)
