package com.example

import com.example.data.market.MarketDataService
import com.example.data.market.TechnicalAnalysisEngine
import com.example.model.Candle
import com.example.model.MarketType
import com.example.model.SignalDirection
import com.example.model.SignalGrade
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testRsiCalculation() {
        val prices = listOf(
            1.0800, 1.0810, 1.0825, 1.0815, 1.0830,
            1.0845, 1.0840, 1.0855, 1.0860, 1.0850,
            1.0870, 1.0880, 1.0875, 1.0890, 1.0905, 1.0910
        )
        val rsi = TechnicalAnalysisEngine.calculateRSI(prices, 14)
        assertTrue("RSI should be between 0 and 100", rsi in 0.0..100.0)
        assertTrue("RSI should reflect upward series", rsi > 50.0)
    }

    @Test
    fun testEmaCalculation() {
        val prices = listOf(10.0, 11.0, 12.0, 13.0, 14.0, 15.0)
        val ema = TechnicalAnalysisEngine.calculateEMA(prices, 3)
        assertEquals(prices.size, ema.size)
        assertTrue("EMA should trend upwards", ema.last() > ema.first())
    }

    @Test
    fun testCandlePatternHammer() {
        // Hammer candle: long lower wick, small body at the top
        val candles = listOf(
            Candle(1000, 1.1000, 1.1020, 1.0980, 1.0990, 100.0),
            Candle(2000, 1.0990, 1.1000, 1.0950, 1.0960, 100.0),
            Candle(3000, 1.0960, 1.0970, 1.0850, 1.0965, 100.0) // Deep wick down to 1.0850, close 1.0965
        )
        val pattern = TechnicalAnalysisEngine.detectCandlestickPattern(candles)
        assertTrue(pattern.patternName == "Hammer" || pattern.patternName == "Pin Bar")
    }

    @Test
    fun testConfluenceEngineConfidenceBounds() {
        val candles = MarketDataService.getCandleHistory("EUR/USD", 40)
        val signal = TechnicalAnalysisEngine.evaluateConfluence(
            pair = "EUR/USD",
            marketType = MarketType.FOREX,
            timeframe = "15M",
            candles = candles
        )
        assertTrue("Confidence must be in 0..100 range", signal.confidenceScore in 0..100)
        assertNotNull(signal.confidenceGrade)
        assertNotNull(signal.indicatorChecks)
        assertEquals(8, signal.indicatorChecks.size)
    }

    @Test
    fun testTelegramFormatOutput() {
        val candles = MarketDataService.getCandleHistory("EUR/USD", 30)
        val signal = TechnicalAnalysisEngine.evaluateConfluence("EUR/USD", MarketType.FOREX, "15M", candles)
        val telegramMessage = signal.toTelegramFormat()
        assertTrue(telegramMessage.contains("ZAKS QX AI SIGNAL"))
        assertTrue(telegramMessage.contains("EUR/USD"))
        assertTrue(telegramMessage.contains("Confidence:"))
    }
}
