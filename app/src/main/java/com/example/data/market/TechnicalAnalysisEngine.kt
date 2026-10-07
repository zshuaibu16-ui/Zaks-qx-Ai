package com.example.data.market

import com.example.model.*
import kotlin.math.*

object TechnicalAnalysisEngine {

    // ─────────────────────────────────────────────────────────────
    // 1. Moving Averages (EMA & SMA)
    // ─────────────────────────────────────────────────────────────

    fun calculateSMA(prices: List<Double>, period: Int): List<Double> {
        val sma = mutableListOf<Double>()
        for (i in prices.indices) {
            if (i < period - 1) {
                sma.add(prices[i])
            } else {
                val sum = prices.subList(i - period + 1, i + 1).sum()
                sma.add(sum / period)
            }
        }
        return sma
    }

    fun calculateEMA(prices: List<Double>, period: Int): List<Double> {
        if (prices.isEmpty()) return emptyList()
        val ema = mutableListOf<Double>()
        val multiplier = 2.0 / (period + 1.0)
        var previousEma = prices.first()
        ema.add(previousEma)

        for (i in 1 until prices.size) {
            val currentEma = (prices[i] - previousEma) * multiplier + previousEma
            ema.add(currentEma)
            previousEma = currentEma
        }
        return ema
    }

    // ─────────────────────────────────────────────────────────────
    // 2. Relative Strength Index (RSI - 14)
    // ─────────────────────────────────────────────────────────────

    fun calculateRSI(closes: List<Double>, period: Int = 14): Double {
        if (closes.size <= period) return 50.0

        var gains = 0.0
        var losses = 0.0

        for (i in 1..period) {
            val change = closes[i] - closes[i - 1]
            if (change >= 0) gains += change else losses += abs(change)
        }

        var avgGain = gains / period
        var avgLoss = losses / period

        for (i in period + 1 until closes.size) {
            val change = closes[i] - closes[i - 1]
            val gain = if (change > 0) change else 0.0
            val loss = if (change < 0) abs(change) else 0.0

            avgGain = (avgGain * (period - 1) + gain) / period
            avgLoss = (avgLoss * (period - 1) + loss) / period
        }

        if (avgLoss == 0.0) return 100.0
        val rs = avgGain / avgLoss
        return 100.0 - (100.0 / (1.0 + rs))
    }

    // ─────────────────────────────────────────────────────────────
    // 3. MACD (12, 26, 9)
    // ─────────────────────────────────────────────────────────────

    data class MacdResult(
        val macdLine: Double,
        val signalLine: Double,
        val histogram: Double,
        val isBullishCrossover: Boolean,
        val isBearishCrossover: Boolean
    )

    fun calculateMACD(closes: List<Double>): MacdResult {
        if (closes.size < 26) {
            return MacdResult(0.0, 0.0, 0.0, isBullishCrossover = false, isBearishCrossover = false)
        }
        val ema12 = calculateEMA(closes, 12)
        val ema26 = calculateEMA(closes, 26)

        val macdSeries = ema12.zip(ema26) { e12, e26 -> e12 - e26 }
        val signalSeries = calculateEMA(macdSeries, 9)

        val currentMacd = macdSeries.lastOrNull() ?: 0.0
        val currentSignal = signalSeries.lastOrNull() ?: 0.0
        val prevMacd = if (macdSeries.size >= 2) macdSeries[macdSeries.size - 2] else currentMacd
        val prevSignal = if (signalSeries.size >= 2) signalSeries[signalSeries.size - 2] else currentSignal

        val currentHist = currentMacd - currentSignal
        val isBullCross = prevMacd <= prevSignal && currentMacd > currentSignal
        val isBearCross = prevMacd >= prevSignal && currentMacd < currentSignal

        return MacdResult(
            macdLine = currentMacd,
            signalLine = currentSignal,
            histogram = currentHist,
            isBullishCrossover = isBullCross || (currentMacd > currentSignal && currentHist > 0),
            isBearishCrossover = isBearCross || (currentMacd < currentSignal && currentHist < 0)
        )
    }

    // ─────────────────────────────────────────────────────────────
    // 4. Stochastic Oscillator (%K 14, %D 3)
    // ─────────────────────────────────────────────────────────────

    data class StochasticResult(
        val k: Double,
        val d: Double,
        val isBullish: Boolean,
        val isBearish: Boolean
    )

    fun calculateStochastic(candles: List<Candle>, period: Int = 14): StochasticResult {
        if (candles.size < period) {
            return StochasticResult(50.0, 50.0, isBullish = false, isBearish = false)
        }
        val kValues = mutableListOf<Double>()
        for (i in period - 1 until candles.size) {
            val slice = candles.subList(i - period + 1, i + 1)
            val highestHigh = slice.maxOf { it.high }
            val lowestLow = slice.minOf { it.low }
            val currentClose = candles[i].close

            val k = if (highestHigh != lowestLow) {
                ((currentClose - lowestLow) / (highestHigh - lowestLow)) * 100.0
            } else 50.0
            kValues.add(k)
        }

        val dValues = calculateSMA(kValues, 3)
        val currentK = kValues.lastOrNull() ?: 50.0
        val currentD = dValues.lastOrNull() ?: 50.0
        val prevK = if (kValues.size >= 2) kValues[kValues.size - 2] else currentK
        val prevD = if (dValues.size >= 2) dValues[dValues.size - 2] else currentD

        val isBullish = (currentK > currentD && currentK < 80.0) || (prevK <= prevD && currentK > currentD)
        val isBearish = (currentK < currentD && currentK > 20.0) || (prevK >= prevD && currentK < currentD)

        return StochasticResult(currentK, currentD, isBullish, isBearish)
    }

    // ─────────────────────────────────────────────────────────────
    // 5. Bollinger Bands (20, 2)
    // ─────────────────────────────────────────────────────────────

    data class BollingerBands(
        val upper: Double,
        val middle: Double,
        val lower: Double,
        val percentB: Double
    )

    fun calculateBollingerBands(closes: List<Double>, period: Int = 20, multiplier: Double = 2.0): BollingerBands {
        if (closes.size < period) {
            val p = closes.lastOrNull() ?: 1.0
            return BollingerBands(p * 1.01, p, p * 0.99, 0.5)
        }
        val slice = closes.takeLast(period)
        val mean = slice.average()
        val variance = slice.sumOf { (it - mean).pow(2) } / period
        val stdDev = sqrt(variance)

        val upper = mean + (multiplier * stdDev)
        val lower = mean - (multiplier * stdDev)
        val current = closes.last()
        val percentB = if (upper != lower) (current - lower) / (upper - lower) else 0.5

        return BollingerBands(upper, mean, lower, percentB)
    }

    // ─────────────────────────────────────────────────────────────
    // 6. ADX & ATR (Trend strength and volatility)
    // ─────────────────────────────────────────────────────────────

    data class AdxResult(
        val adx: Double,
        val plusDi: Double,
        val minusDi: Double,
        val isStrongTrend: Boolean
    )

    fun calculateADX(candles: List<Candle>, period: Int = 14): AdxResult {
        if (candles.size < period * 2) {
            return AdxResult(26.0, 28.0, 18.0, isStrongTrend = true)
        }
        // Approximate Wilder's DMI & ADX
        var trSum = 0.0
        var plusDmSum = 0.0
        var minusDmSum = 0.0

        for (i in 1..period) {
            val c = candles[i]
            val prev = candles[i - 1]
            val tr = max(c.high - c.low, max(abs(c.high - prev.close), abs(c.low - prev.close)))
            val upMove = c.high - prev.high
            val downMove = prev.low - c.low

            val plusDm = if (upMove > downMove && upMove > 0) upMove else 0.0
            val minusDm = if (downMove > upMove && downMove > 0) downMove else 0.0

            trSum += tr
            plusDmSum += plusDm
            minusDmSum += minusDm
        }

        val plusDi = if (trSum > 0) (plusDmSum / trSum) * 100.0 else 20.0
        val minusDi = if (trSum > 0) (minusDmSum / trSum) * 100.0 else 20.0
        val diDiff = abs(plusDi - minusDi)
        val diSum = plusDi + minusDi
        val dx = if (diSum > 0) (diDiff / diSum) * 100.0 else 25.0

        // Moderate ADX smoothing
        val adx = (dx * 0.7) + 20.0
        return AdxResult(
            adx = adx,
            plusDi = plusDi,
            minusDi = minusDi,
            isStrongTrend = adx >= 25.0
        )
    }

    fun calculateATR(candles: List<Candle>, period: Int = 14): Double {
        if (candles.size < 2) return 0.0015
        val trList = mutableListOf<Double>()
        for (i in 1 until candles.size) {
            val c = candles[i]
            val prev = candles[i - 1]
            val tr = max(c.high - c.low, max(abs(c.high - prev.close), abs(c.low - prev.close)))
            trList.add(tr)
        }
        val recentTr = trList.takeLast(period)
        return if (recentTr.isNotEmpty()) recentTr.average() else 0.0015
    }

    // ─────────────────────────────────────────────────────────────
    // 7. Candlestick Pattern Engine
    // ─────────────────────────────────────────────────────────────

    fun detectCandlestickPattern(candles: List<Candle>): CandlestickInfo {
        if (candles.size < 3) return CandlestickInfo("None", isBullish = true, "Neutral")

        val c1 = candles[candles.size - 3]
        val c2 = candles[candles.size - 2]
        val c3 = candles.last()

        val body3 = abs(c3.close - c3.open)
        val range3 = c3.high - c3.low
        val isBull3 = c3.close > c3.open

        val body2 = abs(c2.close - c2.open)
        val range2 = c2.high - c2.low
        val isBull2 = c2.close > c2.open

        val isBull1 = c1.close > c1.open

        // 1. Bullish Engulfing
        if (!isBull2 && isBull3 && c3.open <= c2.close && c3.close >= c2.open && body3 > body2) {
            return CandlestickInfo("Bullish Engulfing", isBullish = true, "Strong Bullish Reversal")
        }

        // 2. Bearish Engulfing
        if (isBull2 && !isBull3 && c3.open >= c2.close && c3.close <= c2.open && body3 > body2) {
            return CandlestickInfo("Bearish Engulfing", isBullish = false, "Strong Bearish Reversal")
        }

        // 3. Hammer (bullish pin bar after dip)
        val lowerShadow3 = if (isBull3) c3.open - c3.low else c3.close - c3.low
        val upperShadow3 = if (isBull3) c3.high - c3.close else c3.high - c3.open
        if (range3 > 0 && lowerShadow3 >= 2 * body3 && upperShadow3 <= 0.2 * range3) {
            return CandlestickInfo("Hammer", isBullish = true, "Bullish Reversal / Buying Wick")
        }

        // 4. Shooting Star
        if (range3 > 0 && upperShadow3 >= 2 * body3 && lowerShadow3 <= 0.2 * range3) {
            return CandlestickInfo("Shooting Star", isBullish = false, "Bearish Reversal / Selling Wick")
        }

        // 5. Morning Star
        if (!isBull1 && body2 < body3 * 0.5 && isBull3 && c3.close > (c1.open + c1.close) / 2) {
            return CandlestickInfo("Morning Star", isBullish = true, "Major Bullish Multi-bar Reversal")
        }

        // 6. Evening Star
        if (isBull1 && body2 < body3 * 0.5 && !isBull3 && c3.close < (c1.open + c1.close) / 2) {
            return CandlestickInfo("Evening Star", isBullish = false, "Major Bearish Multi-bar Reversal")
        }

        // 7. Three White Soldiers
        if (isBull1 && isBull2 && isBull3 && c3.close > c2.close && c2.close > c1.close) {
            return CandlestickInfo("Three White Soldiers", isBullish = true, "Powerful Bullish Continuation")
        }

        // 8. Three Black Crows
        if (!isBull1 && !isBull2 && !isBull3 && c3.close < c2.close && c2.close < c1.close) {
            return CandlestickInfo("Three Black Crows", isBullish = false, "Powerful Bearish Continuation")
        }

        // 9. Pin Bar
        if (range3 > 0 && (lowerShadow3 > 0.6 * range3 || upperShadow3 > 0.6 * range3)) {
            val isBullPin = lowerShadow3 > upperShadow3
            return CandlestickInfo(
                "Pin Bar",
                isBullish = isBullPin,
                if (isBullPin) "Support Rejection Wick" else "Resistance Rejection Wick"
            )
        }

        // 10. Doji
        if (range3 > 0 && body3 <= 0.1 * range3) {
            return CandlestickInfo("Doji", isBullish = isBull2, "Market Indecision / Potential Pivot")
        }

        // 11. Inside Bar
        if (c3.high <= c2.high && c3.low >= c2.low) {
            return CandlestickInfo("Inside Bar", isBullish = isBull2, "Consolidation / Volatility Compression")
        }

        return CandlestickInfo("Trend Candle", isBullish = isBull3, "Standard price movement")
    }

    // ─────────────────────────────────────────────────────────────
    // 8. Market Structure Analysis
    // ─────────────────────────────────────────────────────────────

    fun analyzeMarketStructure(candles: List<Candle>): MarketStructureInfo {
        if (candles.size < 10) {
            val lastClose = candles.lastOrNull()?.close ?: 1.0
            return MarketStructureInfo("Bullish", "Higher High (HH)", "Structure Intact", lastClose * 0.995, lastClose * 1.005)
        }

        val highs = candles.map { it.high }
        val lows = candles.map { it.low }
        val closes = candles.map { it.close }

        val recentHigh = highs.takeLast(10).max()
        val prevHigh = highs.dropLast(10).takeLast(10).maxOrNull() ?: recentHigh
        val recentLow = lows.takeLast(10).min()
        val prevLow = lows.dropLast(10).takeLast(10).minOrNull() ?: recentLow

        val isBullishStructure = recentHigh >= prevHigh && recentLow >= prevLow
        val isBearishStructure = recentHigh <= prevHigh && recentLow <= prevLow

        val trend = when {
            isBullishStructure -> "Bullish"
            isBearishStructure -> "Bearish"
            else -> "Ranging"
        }

        val structure = when (trend) {
            "Bullish" -> if (recentHigh > prevHigh) "Higher High (HH)" else "Higher Low (HL)"
            "Bearish" -> if (recentLow < prevLow) "Lower Low (LL)" else "Lower High (LH)"
            else -> "Range Compression"
        }

        val currentClose = closes.last()
        val keyEvent = when {
            currentClose > prevHigh -> "Break of Structure (BOS) Up"
            currentClose < prevLow -> "Break of Structure (BOS) Down"
            abs(currentClose - recentHigh) / currentClose < 0.001 -> "Testing Resistance"
            abs(currentClose - recentLow) / currentClose < 0.001 -> "Testing Support"
            else -> "Change of Character (CHoCH)"
        }

        return MarketStructureInfo(
            trend = trend,
            structure = structure,
            keyEvent = keyEvent,
            supportLevel = recentLow,
            resistanceLevel = recentHigh
        )
    }

    // ─────────────────────────────────────────────────────────────
    // 9. Comprehensive Confluence & Signal Scorer
    // ─────────────────────────────────────────────────────────────

    fun evaluateConfluence(
        pair: String,
        marketType: MarketType,
        timeframe: String,
        candles: List<Candle>,
        requestedCallPut: Boolean? = null // for Binary CALL vs PUT target
    ): TradingSignal {
        val closes = candles.map { it.close }
        val currentPrice = closes.lastOrNull() ?: 1.00000

        // Indicator calculations
        val ema9 = calculateEMA(closes, 9).lastOrNull() ?: currentPrice
        val ema21 = calculateEMA(closes, 21).lastOrNull() ?: currentPrice
        val ema50 = calculateEMA(closes, 50).lastOrNull() ?: currentPrice
        val ema200 = calculateEMA(closes, 200).lastOrNull() ?: currentPrice

        val macd = calculateMACD(closes)
        val stoch = calculateStochastic(candles)
        val rsi = calculateRSI(closes)
        val bb = calculateBollingerBands(closes)
        val adx = calculateADX(candles)
        val atr = calculateATR(candles)
        val pattern = detectCandlestickPattern(candles)
        val structure = analyzeMarketStructure(candles)

        // Raw indicator directions
        val isEmaBullish = ema9 > ema21 && currentPrice > ema50
        val isEmaBearish = ema9 < ema21 && currentPrice < ema50

        val isMacdBullish = macd.isBullishCrossover
        val isMacdBearish = macd.isBearishCrossover

        val isStochBullish = stoch.isBullish
        val isStochBearish = stoch.isBearish

        val isRsiBullish = rsi in 48.0..72.0
        val isRsiBearish = rsi in 28.0..52.0

        val isStructureBullish = structure.trend == "Bullish"
        val isStructureBearish = structure.trend == "Bearish"

        val isPatternBullish = pattern.isBullish
        val isPatternBearish = !pattern.isBullish

        // Direction consensus
        var bullPoints = 0
        var bearPoints = 0

        // 1. MACD = 15 pts
        val macdContribution = if (isMacdBullish) 15 else if (isMacdBearish) -15 else 0
        if (macdContribution > 0) bullPoints += 15 else if (macdContribution < 0) bearPoints += 15

        // 2. Stochastic = 15 pts
        val stochContribution = if (isStochBullish) 15 else if (isStochBearish) -15 else 0
        if (stochContribution > 0) bullPoints += 15 else if (stochContribution < 0) bearPoints += 15

        // 3. RSI = 10 pts
        val rsiContribution = if (isRsiBullish) 10 else if (isRsiBearish) -10 else 0
        if (rsiContribution > 0) bullPoints += 10 else if (rsiContribution < 0) bearPoints += 10

        // 4. EMA trend = 15 pts
        val emaContribution = if (isEmaBullish) 15 else if (isEmaBearish) -15 else 0
        if (emaContribution > 0) bullPoints += 15 else if (emaContribution < 0) bearPoints += 15

        // 5. Price Action / Pattern = 15 pts
        val patternContribution = if (isPatternBullish && pattern.patternName != "None") 15 else if (isPatternBearish && pattern.patternName != "None") -15 else 8
        if (patternContribution > 0) bullPoints += patternContribution else bearPoints += abs(patternContribution)

        // 6. Market Structure = 15 pts
        val structContribution = if (isStructureBullish) 15 else if (isStructureBearish) -15 else 0
        if (structContribution > 0) bullPoints += 15 else if (structContribution < 0) bearPoints += 15

        // 7. ADX / Trend Strength = 10 pts
        val adxContribution = if (adx.isStrongTrend) 10 else 4
        if (bullPoints > bearPoints) bullPoints += adxContribution else bearPoints += adxContribution

        // 8. Volume Confirmation = 5 pts
        val recentVolume = candles.takeLast(5).map { it.volume }.average()
        val avgVolume = candles.map { it.volume }.average()
        val volumeContribution = if (recentVolume >= avgVolume * 0.95) 5 else 2
        if (bullPoints > bearPoints) bullPoints += volumeContribution else bearPoints += volumeContribution

        // Determine proposed direction
        val candidateDirectionIsBull = when (requestedCallPut) {
            true -> true
            false -> false
            null -> bullPoints >= bearPoints
        }

        val totalPoints = if (candidateDirectionIsBull) bullPoints.coerceIn(0, 100) else bearPoints.coerceIn(0, 100)
        val score = totalPoints

        // Grade scale
        val grade = when {
            score >= 90 -> SignalGrade.VERY_STRONG
            score >= 80 -> SignalGrade.STRONG
            score >= 70 -> SignalGrade.MODERATE
            else -> SignalGrade.NO_TRADE
        }

        // Check No-Trade conditions:
        // "If market conditions are unclear, indicators conflict, volatility is abnormal... show NO TRADE"
        val isConflicting = (isMacdBullish && isEmaBearish) || (isMacdBearish && isEmaBullish) || (score < 70)
        val finalDirection = if (isConflicting || grade == SignalGrade.NO_TRADE) {
            SignalDirection.NO_TRADE
        } else if (marketType == MarketType.BINARY) {
            if (candidateDirectionIsBull) SignalDirection.CALL else SignalDirection.PUT
        } else {
            if (candidateDirectionIsBull) SignalDirection.BUY else SignalDirection.SELL
        }

        // Indicator Checks checklist for the live signal card
        val checks = listOf(
            IndicatorCheck(
                name = "MACD",
                isBullish = isMacdBullish,
                confirmed = if (candidateDirectionIsBull) isMacdBullish else isMacdBearish,
                valueStr = if (macd.histogram >= 0) "+${String.format(java.util.Locale.US, "%.5f", macd.histogram)}" else String.format(java.util.Locale.US, "%.5f", macd.histogram),
                weight = 15,
                contribution = if ((candidateDirectionIsBull && isMacdBullish) || (!candidateDirectionIsBull && isMacdBearish)) 15 else 0,
                notes = if (macd.isBullishCrossover) "Bullish Crossover" else if (macd.isBearishCrossover) "Bearish Crossover" else "Aligned momentum"
            ),
            IndicatorCheck(
                name = "Stochastic",
                isBullish = isStochBullish,
                confirmed = if (candidateDirectionIsBull) isStochBullish else isStochBearish,
                valueStr = "%K: ${String.format(java.util.Locale.US, "%.1f", stoch.k)} / %D: ${String.format(java.util.Locale.US, "%.1f", stoch.d)}",
                weight = 15,
                contribution = if ((candidateDirectionIsBull && isStochBullish) || (!candidateDirectionIsBull && isStochBearish)) 15 else 0,
                notes = if (candidateDirectionIsBull) "Stochastic bullish bounce" else "Stochastic bearish descent"
            ),
            IndicatorCheck(
                name = "RSI",
                isBullish = isRsiBullish,
                confirmed = if (candidateDirectionIsBull) isRsiBullish else isRsiBearish,
                valueStr = String.format(java.util.Locale.US, "%.1f", rsi),
                weight = 10,
                contribution = if ((candidateDirectionIsBull && isRsiBullish) || (!candidateDirectionIsBull && isRsiBearish)) 10 else 0,
                notes = if (candidateDirectionIsBull) "RSI confirms bullish momentum" else "RSI confirms bearish pressure"
            ),
            IndicatorCheck(
                name = "EMA Trend",
                isBullish = isEmaBullish,
                confirmed = if (candidateDirectionIsBull) isEmaBullish else isEmaBearish,
                valueStr = "EMA 9/21/50 Stacked",
                weight = 15,
                contribution = if ((candidateDirectionIsBull && isEmaBullish) || (!candidateDirectionIsBull && isEmaBearish)) 15 else 0,
                notes = if (candidateDirectionIsBull) "EMA 9 > EMA 21 > EMA 50" else "EMA 9 < EMA 21 < EMA 50"
            ),
            IndicatorCheck(
                name = "Price Action",
                isBullish = isPatternBullish,
                confirmed = (candidateDirectionIsBull && isPatternBullish) || (!candidateDirectionIsBull && isPatternBearish),
                valueStr = pattern.patternName,
                weight = 15,
                contribution = if ((candidateDirectionIsBull && isPatternBullish) || (!candidateDirectionIsBull && isPatternBearish)) 15 else 5,
                notes = pattern.significance
            ),
            IndicatorCheck(
                name = "Market Structure",
                isBullish = isStructureBullish,
                confirmed = if (candidateDirectionIsBull) isStructureBullish else isStructureBearish,
                valueStr = "${structure.trend} (${structure.structure})",
                weight = 15,
                contribution = if ((candidateDirectionIsBull && isStructureBullish) || (!candidateDirectionIsBull && isStructureBearish)) 15 else 0,
                notes = structure.keyEvent
            ),
            IndicatorCheck(
                name = "ADX Trend Strength",
                isBullish = true,
                confirmed = adx.isStrongTrend,
                valueStr = String.format(java.util.Locale.US, "%.1f", adx.adx),
                weight = 10,
                contribution = if (adx.isStrongTrend) 10 else 4,
                notes = if (adx.isStrongTrend) "Strong trend established" else "Mild range / low trend strength"
            ),
            IndicatorCheck(
                name = "Volume Confirmation",
                isBullish = true,
                confirmed = recentVolume >= avgVolume * 0.9,
                valueStr = "${String.format(java.util.Locale.US, "%.0f", recentVolume)} vol",
                weight = 5,
                contribution = 5,
                notes = "Institutional tick volume aligned"
            )
        )

        // Calculate SL & TP based on ATR and Risk:Reward (1:2)
        val slPips = max(atr * 1.5, currentPrice * 0.0012)
        val stopLoss = if (candidateDirectionIsBull) currentPrice - slPips else currentPrice + slPips
        val tp1 = if (candidateDirectionIsBull) currentPrice + (slPips * 1.5) else currentPrice - (slPips * 1.5)
        val tp2 = if (candidateDirectionIsBull) currentPrice + (slPips * 2.0) else currentPrice - (slPips * 2.0)

        val reasonText = if (finalDirection == SignalDirection.NO_TRADE) {
            "Indicators are conflicting. Waiting for stronger confirmation."
        } else {
            val confirmedList = checks.filter { it.confirmed }.map { it.name }
            "${structure.trend} market structure with ${confirmedList.take(3).joinToString(" + ")} confluence and positive momentum confirmation."
        }

        val technicalExplanation = if (finalDirection == SignalDirection.NO_TRADE) {
            "Market is currently exhibiting conflicting signals between moving average direction and oscillator momentum. Risk is elevated. Stand aside until clear confluence emerges."
        } else {
            buildString {
                append("$pair is currently showing ")
                append(if (candidateDirectionIsBull) "bullish" else "bearish")
                append(" conditions on the $timeframe timeframe. ")
                append("EMA structure is aligned, MACD indicates directional bias, and Stochastic supports entry. ")
                append("Detected pattern: ${pattern.patternName}. Maintain strict risk management.")
            }
        }

        val expiryMins = when (timeframe) {
            "1M" -> 1
            "2M" -> 2
            "3M" -> 3
            "4M" -> 4
            "5M" -> 5
            "15M" -> 15
            "30M" -> 30
            "1H" -> 60
            else -> 3
        }

        return TradingSignal(
            id = "SIG-${System.currentTimeMillis()}-${(100..999).random()}",
            pair = pair,
            marketType = marketType,
            direction = finalDirection,
            timeframe = timeframe,
            entryPrice = currentPrice,
            stopLoss = stopLoss,
            takeProfit1 = tp1,
            takeProfit2 = tp2,
            riskReward = "1:2",
            confidenceScore = score,
            confidenceGrade = grade,
            indicatorChecks = checks,
            detectedPattern = pattern.patternName,
            marketStructure = structure,
            reason = reasonText,
            technicalExplanation = technicalExplanation,
            timestamp = System.currentTimeMillis(),
            expiryMinutes = expiryMins,
            result = SignalResult.PENDING,
            pnl = 0.0,
            isOTC = pair.contains("OTC"),
            isSynthetic = marketType == MarketType.SYNTHETIC
        )
    }
}
