package com.example.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SignalDao {
    @Query("SELECT * FROM signals ORDER BY timestamp DESC")
    fun getAllSignals(): Flow<List<SignalEntity>>

    @Query("SELECT * FROM signals WHERE marketType = :marketType ORDER BY timestamp DESC")
    fun getSignalsByMarket(marketType: String): Flow<List<SignalEntity>>

    @Query("SELECT * FROM signals WHERE result = :result ORDER BY timestamp DESC")
    fun getSignalsByResult(result: String): Flow<List<SignalEntity>>

    @Query("SELECT * FROM signals WHERE id = :id LIMIT 1")
    suspend fun getSignalById(id: String): SignalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSignal(signal: SignalEntity)

    @Update
    suspend fun updateSignal(signal: SignalEntity)

    @Query("UPDATE signals SET result = :result, pnl = :pnl WHERE id = :id")
    suspend fun updateResult(id: String, result: String, pnl: Double)

    @Query("DELETE FROM signals WHERE id = :id")
    suspend fun deleteSignal(id: String)

    @Query("DELETE FROM signals")
    suspend fun clearAllSignals()

    @Query("SELECT COUNT(*) FROM signals")
    suspend fun countAll(): Int

    @Query("SELECT COUNT(*) FROM signals WHERE result = 'WIN'")
    suspend fun countWins(): Int

    @Query("SELECT COUNT(*) FROM signals WHERE result = 'LOSS'")
    suspend fun countLosses(): Int
}
