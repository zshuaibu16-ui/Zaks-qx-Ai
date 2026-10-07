package com.example.data.repository

import com.example.data.database.SignalDao
import com.example.data.database.SignalEntity
import com.example.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar

class SignalRepository(private val signalDao: SignalDao) {

    val allSignals: Flow<List<TradingSignal>> = signalDao.getAllSignals().map { entities ->
        entities.map { it.toDomain() }
    }

    fun getSignalsByMarket(marketType: MarketType): Flow<List<TradingSignal>> =
        signalDao.getSignalsByMarket(marketType.name).map { entities ->
            entities.map { it.toDomain() }
        }

    fun getSignalsByResult(result: SignalResult): Flow<List<TradingSignal>> =
        signalDao.getSignalsByResult(result.name).map { entities ->
            entities.map { it.toDomain() }
        }

    suspend fun saveSignal(signal: TradingSignal) {
        signalDao.insertSignal(SignalEntity.fromDomain(signal))
    }

    suspend fun updateResult(id: String, result: SignalResult, pnl: Double) {
        signalDao.updateResult(id, result.name, pnl)
    }

    suspend fun deleteSignal(id: String) {
        signalDao.deleteSignal(id)
    }

    suspend fun clearAll() {
        signalDao.clearAllSignals()
    }

    /**
     * Accurately calculate performance metrics exclusively from real recorded signals in database.
     */
    fun computeStats(signals: List<TradingSignal>): PerformanceStats {
        val completedSignals = signals.filter { it.result == SignalResult.WIN || it.result == SignalResult.LOSS }
        val wins = completedSignals.count { it.result == SignalResult.WIN }
        val losses = completedSignals.count { it.result == SignalResult.LOSS }
        val totalCompleted = wins + losses
        val winRate = if (totalCompleted > 0) (wins.toDouble() / totalCompleted * 100.0) else 0.0
        val totalPnl = completedSignals.sumOf { it.pnl }

        // Today's metrics
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val todayStart = cal.timeInMillis

        val todaySignals = completedSignals.filter { it.timestamp >= todayStart }
        val todayWins = todaySignals.count { it.result == SignalResult.WIN }
        val todayLosses = todaySignals.count { it.result == SignalResult.LOSS }
        val todayTotal = todayWins + todayLosses
        val todayWinRate = if (todayTotal > 0) (todayWins.toDouble() / todayTotal * 100.0) else 0.0
        val todayPnl = todaySignals.sumOf { it.pnl }

        // Weekly (past 7 days)
        val weekStart = todayStart - (7L * 24 * 60 * 60 * 1000)
        val weekSignals = completedSignals.filter { it.timestamp >= weekStart }
        val weekWins = weekSignals.count { it.result == SignalResult.WIN }
        val weekTotal = weekWins + weekSignals.count { it.result == SignalResult.LOSS }
        val weeklyWinRate = if (weekTotal > 0) (weekWins.toDouble() / weekTotal * 100.0) else 0.0

        // Monthly (past 30 days)
        val monthStart = todayStart - (30L * 24 * 60 * 60 * 1000)
        val monthSignals = completedSignals.filter { it.timestamp >= monthStart }
        val monthWins = monthSignals.count { it.result == SignalResult.WIN }
        val monthTotal = monthWins + monthSignals.count { it.result == SignalResult.LOSS }
        val monthlyWinRate = if (monthTotal > 0) (monthWins.toDouble() / monthTotal * 100.0) else 0.0

        val pendingCount = signals.count { it.result == SignalResult.PENDING }

        return PerformanceStats(
            totalSignals = signals.size,
            wins = wins,
            losses = losses,
            winRate = winRate,
            todayWins = todayWins,
            todayLosses = todayLosses,
            todayWinRate = todayWinRate,
            todayPnl = todayPnl,
            totalPnl = totalPnl,
            weeklyWinRate = weeklyWinRate,
            monthlyWinRate = monthlyWinRate,
            pendingCount = pendingCount
        )
    }
}
