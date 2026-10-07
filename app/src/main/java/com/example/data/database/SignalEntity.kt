package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.*

@Entity(tableName = "signals")
data class SignalEntity(
    @PrimaryKey
    val id: String,
    val pair: String,
    val marketType: String,
    val direction: String,
    val timeframe: String,
    val entryPrice: Double,
    val stopLoss: Double,
    val takeProfit1: Double,
    val takeProfit2: Double,
    val riskReward: String,
    val confidenceScore: Int,
    val confidenceGrade: String,
    val detectedPattern: String,
    val marketStructureTrend: String,
    val marketStructureEvent: String,
    val reason: String,
    val technicalExplanation: String,
    val indicatorsSummary: String,
    val timestamp: Long,
    val expiryMinutes: Int,
    val result: String,
    val pnl: Double,
    val isOTC: Boolean,
    val isSynthetic: Boolean
) {
    fun toDomain(): TradingSignal {
        val mType = try { MarketType.valueOf(marketType) } catch (e: Exception) { MarketType.BINARY }
        val sDirection = try { SignalDirection.valueOf(direction) } catch (e: Exception) { SignalDirection.BUY }
        val sGrade = try { SignalGrade.valueOf(confidenceGrade) } catch (e: Exception) { SignalGrade.STRONG }
        val sResult = try { SignalResult.valueOf(result) } catch (e: Exception) { SignalResult.PENDING }

        // Parse indicators summary back into checks
        val checks = indicatorsSummary.split(";").filter { it.isNotBlank() }.map { part ->
            val tokens = part.split("|")
            val name = tokens.getOrNull(0) ?: "Indicator"
            val confirmed = tokens.getOrNull(1)?.toBooleanStrictOrNull() ?: true
            val valStr = tokens.getOrNull(2) ?: "Aligned"
            val weight = tokens.getOrNull(3)?.toIntOrNull() ?: 15
            val isBull = tokens.getOrNull(4)?.toBooleanStrictOrNull() ?: true
            IndicatorCheck(
                name = name,
                isBullish = isBull,
                confirmed = confirmed,
                valueStr = valStr,
                weight = weight,
                contribution = if (confirmed) weight else 0,
                notes = if (confirmed) "Aligned with trend" else "Caution advised"
            )
        }

        return TradingSignal(
            id = id,
            pair = pair,
            marketType = mType,
            direction = sDirection,
            timeframe = timeframe,
            entryPrice = entryPrice,
            stopLoss = stopLoss,
            takeProfit1 = takeProfit1,
            takeProfit2 = takeProfit2,
            riskReward = riskReward,
            confidenceScore = confidenceScore,
            confidenceGrade = sGrade,
            indicatorChecks = checks,
            detectedPattern = detectedPattern,
            marketStructure = MarketStructureInfo(
                trend = marketStructureTrend,
                structure = if (marketStructureTrend == "Bullish") "Higher High (HH)" else "Lower Low (LL)",
                keyEvent = marketStructureEvent,
                supportLevel = entryPrice * 0.995,
                resistanceLevel = entryPrice * 1.005
            ),
            reason = reason,
            technicalExplanation = technicalExplanation,
            timestamp = timestamp,
            expiryMinutes = expiryMinutes,
            result = sResult,
            pnl = pnl,
            isOTC = isOTC,
            isSynthetic = isSynthetic
        )
    }

    companion object {
        fun fromDomain(signal: TradingSignal): SignalEntity {
            val indSummary = signal.indicatorChecks.joinToString(";") {
                "${it.name}|${it.confirmed}|${it.valueStr}|${it.weight}|${it.isBullish}"
            }
            return SignalEntity(
                id = signal.id,
                pair = signal.pair,
                marketType = signal.marketType.name,
                direction = signal.direction.name,
                timeframe = signal.timeframe,
                entryPrice = signal.entryPrice,
                stopLoss = signal.stopLoss,
                takeProfit1 = signal.takeProfit1,
                takeProfit2 = signal.takeProfit2,
                riskReward = signal.riskReward,
                confidenceScore = signal.confidenceScore,
                confidenceGrade = signal.confidenceGrade.name,
                detectedPattern = signal.detectedPattern,
                marketStructureTrend = signal.marketStructure.trend,
                marketStructureEvent = signal.marketStructure.keyEvent,
                reason = signal.reason,
                technicalExplanation = signal.technicalExplanation,
                indicatorsSummary = indSummary,
                timestamp = signal.timestamp,
                expiryMinutes = signal.expiryMinutes,
                result = signal.result.name,
                pnl = signal.pnl,
                isOTC = signal.isOTC,
                isSynthetic = signal.isSynthetic
            )
        }
    }
}
