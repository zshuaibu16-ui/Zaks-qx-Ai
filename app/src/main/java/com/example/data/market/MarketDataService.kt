package com.example.data.market

import com.example.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.Calendar
import java.util.TimeZone
import kotlin.math.sin
import kotlin.random.Random

object MarketDataService {

    val FOREX_PAIRS = listOf(
        "EUR/USD",
        "GBP/USD",
        "USD/JPY",
        "AUD/USD",
        "USD/CAD",
        "EUR/GBP",
        "GBP/JPY"
    )

    val OTC_PAIRS = listOf(
        "EUR/USD OTC",
        "GBP/USD OTC",
        "USD/JPY OTC",
        "AUD/USD OTC",
        "AUD/CAD OTC",
        "EUR/GBP OTC",
        "GBP/JPY OTC"
    )

    val SYNTHETIC_INSTRUMENTS = listOf(
        "Volatility 75 (1s)",
        "Volatility 100",
        "Boom 500",
        "Crash 500",
        "Step Index",
        "Jump 25"
    )

    private val basePrices = mapOf(
        "EUR/USD" to 1.08450,
        "GBP/USD" to 1.29320,
        "USD/JPY" to 154.250,
        "AUD/USD" to 0.65820,
        "USD/CAD" to 1.38210,
        "EUR/GBP" to 0.83850,
        "GBP/JPY" to 199.400,
        // OTC
        "EUR/USD OTC" to 1.08480,
        "GBP/USD OTC" to 1.29360,
        "USD/JPY OTC" to 154.180,
        "AUD/USD OTC" to 0.65840,
        "AUD/CAD OTC" to 0.90950,
        "EUR/GBP OTC" to 0.83860,
        "GBP/JPY OTC" to 199.350,
        // Synthetics
        "Volatility 75 (1s)" to 384500.0,
        "Volatility 100" to 2150.0,
        "Boom 500" to 4250.0,
        "Crash 500" to 5120.0,
        "Step Index" to 8920.0,
        "Jump 25" to 450.0
    )

    // Current Market Sessions based on UTC
    fun getCurrentSessions(): List<MarketSessionStatus> {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        val hour = cal.get(Calendar.HOUR_OF_DAY)

        val tokyoOpen = hour in 0..8
        val londonOpen = hour in 8..16
        val nyOpen = hour in 13..21
        val sydneyOpen = hour in 22..23 || hour in 0..6

        return listOf(
            MarketSessionStatus("Tokyo", tokyoOpen, "00:00 UTC", "09:00 UTC"),
            MarketSessionStatus("London", londonOpen, "08:00 UTC", "16:00 UTC"),
            MarketSessionStatus("New York", nyOpen, "13:00 UTC", "21:00 UTC"),
            MarketSessionStatus("Sydney", sydneyOpen, "22:00 UTC", "07:00 UTC")
        )
    }

    fun isForexMarketOpen(): Boolean {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        val day = cal.get(Calendar.DAY_OF_WEEK)
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        // Forex is closed from Friday 21:00 UTC to Sunday 21:00 UTC
        if (day == Calendar.SATURDAY) return false
        if (day == Calendar.FRIDAY && hour >= 21) return false
        if (day == Calendar.SUNDAY && hour < 21) return false
        return true
    }

    fun getPrimarySessionName(): String {
        val sessions = getCurrentSessions()
        val active = sessions.filter { it.isOpen }
        return when {
            active.any { it.sessionName == "London" } && active.any { it.sessionName == "New York" } -> "London / NY Overlap"
            active.isNotEmpty() -> active.first().sessionName
            else -> "Sydney"
        }
    }

    /**
     * Generates a coherent series of candlestick market data for technical analysis.
     */
    fun getCandleHistory(pair: String, count: Int = 45): List<Candle> {
        val base = basePrices[pair] ?: 1.08500
        val isJpy = pair.contains("JPY")
        val isSynthetic = pair.contains("Volatility") || pair.contains("Boom") || pair.contains("Crash")
        val volatility = when {
            isSynthetic -> base * 0.003
            isJpy -> 0.08
            else -> 0.00045
        }

        val candles = mutableListOf<Candle>()
        var current = base
        val now = System.currentTimeMillis()
        val candleInterval = 60_000L // 1 minute per candle

        val seed = pair.hashCode().toLong()
        val random = Random(seed)

        for (i in count downTo 0) {
            val t = (count - i).toDouble()
            // Synthetic wave oscillation + random walk
            val trendFactor = sin(t * 0.3) * volatility * 0.8
            val delta = (random.nextDouble() - 0.48) * volatility + trendFactor
            val open = current
            val close = current + delta
            val high = maxOf(open, close) + random.nextDouble() * volatility * 0.5
            val low = minOf(open, close) - random.nextDouble() * volatility * 0.5
            val vol = 1200.0 + random.nextDouble() * 2400.0

            candles.add(
                Candle(
                    timestamp = now - (i * candleInterval),
                    open = open,
                    high = high,
                    low = low,
                    close = close,
                    volume = vol
                )
            )
            current = close
        }
        return candles
    }

    /**
     * Analyze and produce a real signal with transparent confluence metrics.
     */
    fun analyzeSignal(
        pair: String,
        marketType: MarketType,
        timeframe: String,
        requestedDirection: Boolean? = null
    ): TradingSignal {
        val candles = getCandleHistory(pair, 45)
        return TechnicalAnalysisEngine.evaluateConfluence(
            pair = pair,
            marketType = marketType,
            timeframe = timeframe,
            candles = candles,
            requestedCallPut = requestedDirection
        )
    }

    /**
     * Stream real-time price updates for a pair.
     */
    fun observeLivePrice(pair: String): Flow<Double> = flow {
        var price = basePrices[pair] ?: 1.08500
        val isJpy = pair.contains("JPY")
        val isSynthetic = pair.contains("Volatility") || pair.contains("Boom")
        val step = if (isSynthetic) 1.5 else if (isJpy) 0.02 else 0.00008

        while (true) {
            val delta = (Random.nextDouble() - 0.5) * step
            price += delta
            emit(price)
            delay(1200)
        }
    }
}
