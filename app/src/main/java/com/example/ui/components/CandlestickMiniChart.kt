package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.market.MarketDataService
import com.example.data.market.TechnicalAnalysisEngine
import com.example.model.Candle
import com.example.ui.theme.*

@Composable
fun CandlestickMiniChart(
    pair: String,
    entryPrice: Double,
    stopLoss: Double = 0.0,
    takeProfit1: Double = 0.0,
    modifier: Modifier = Modifier
) {
    val candles = remember(pair) {
        MarketDataService.getCandleHistory(pair, 28)
    }

    val closes = remember(candles) { candles.map { it.close } }
    val ema9 = remember(closes) { TechnicalAnalysisEngine.calculateEMA(closes, 9) }
    val ema21 = remember(closes) { TechnicalAnalysisEngine.calculateEMA(closes, 21) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurfaceVariant)
            .border(1.dp, DarkCardBorder, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "REAL-TIME PRICE ACTION",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "$pair (M1)",
                    color = GoldPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(modifier = Modifier.size(8.dp, 2.dp).background(GoldAccent))
                    Text("EMA 9", color = TextSecondary, fontSize = 9.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(modifier = Modifier.size(8.dp, 2.dp).background(CyanAccent))
                    Text("EMA 21", color = TextSecondary, fontSize = 9.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                if (candles.isEmpty()) return@Canvas

                val minPrice = minOf(candles.minOf { it.low }, if (stopLoss > 0) stopLoss else Double.MAX_VALUE) * 0.9995
                val maxPrice = maxOf(candles.maxOf { it.high }, if (takeProfit1 > 0) takeProfit1 else Double.MIN_VALUE) * 1.0005
                val priceRange = if (maxPrice > minPrice) maxPrice - minPrice else 1.0

                fun getY(price: Double): Float {
                    return ((1.0 - ((price - minPrice) / priceRange)) * size.height).toFloat()
                }

                // Grid lines
                val gridLines = 4
                for (g in 0..gridLines) {
                    val y = (size.height / gridLines) * g
                    drawLine(
                        color = Color(0x1AFFFFFF),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1f
                    )
                }

                val candleWidth = (size.width / candles.size).coerceAtLeast(3f)
                val bodyWidth = (candleWidth * 0.7f).coerceAtLeast(2f)

                // Draw Candles
                candles.forEachIndexed { i, candle ->
                    val x = i * candleWidth + (candleWidth / 2f)
                    val isBull = candle.close >= candle.open
                    val candleColor = if (isBull) BullishGreen else BearishRed

                    val highY = getY(candle.high)
                    val lowY = getY(candle.low)
                    val openY = getY(candle.open)
                    val closeY = getY(candle.close)

                    // Wick
                    drawLine(
                        color = candleColor,
                        start = Offset(x, highY),
                        end = Offset(x, lowY),
                        strokeWidth = 1.5f
                    )

                    // Body
                    val topY = minOf(openY, closeY)
                    val height = maxOf(kotlin.math.abs(closeY - openY), 2f)
                    drawRect(
                        color = candleColor,
                        topLeft = Offset(x - bodyWidth / 2f, topY),
                        size = Size(bodyWidth, height)
                    )
                }

                // Draw EMA 9
                if (ema9.size == candles.size) {
                    val ema9Path = Path()
                    ema9.forEachIndexed { i, valEma ->
                        val x = i * candleWidth + (candleWidth / 2f)
                        val y = getY(valEma)
                        if (i == 0) ema9Path.moveTo(x, y) else ema9Path.lineTo(x, y)
                    }
                    drawPath(
                        path = ema9Path,
                        color = GoldAccent,
                        style = Stroke(width = 2f)
                    )
                }

                // Draw EMA 21
                if (ema21.size == candles.size) {
                    val ema21Path = Path()
                    ema21.forEachIndexed { i, valEma ->
                        val x = i * candleWidth + (candleWidth / 2f)
                        val y = getY(valEma)
                        if (i == 0) ema21Path.moveTo(x, y) else ema21Path.lineTo(x, y)
                    }
                    drawPath(
                        path = ema21Path,
                        color = CyanAccent,
                        style = Stroke(width = 2f)
                    )
                }

                val dashEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)

                // Entry Price line
                val entryY = getY(entryPrice)
                if (entryY in 0f..size.height) {
                    drawLine(
                        color = GoldPrimary,
                        start = Offset(0f, entryY),
                        end = Offset(size.width, entryY),
                        strokeWidth = 1.5f,
                        pathEffect = dashEffect
                    )
                }

                // Take Profit line
                if (takeProfit1 > 0) {
                    val tpY = getY(takeProfit1)
                    if (tpY in 0f..size.height) {
                        drawLine(
                            color = BullishGreen,
                            start = Offset(0f, tpY),
                            end = Offset(size.width, tpY),
                            strokeWidth = 1.5f,
                            pathEffect = dashEffect
                        )
                    }
                }

                // Stop Loss line
                if (stopLoss > 0) {
                    val slY = getY(stopLoss)
                    if (slY in 0f..size.height) {
                        drawLine(
                            color = BearishRed,
                            start = Offset(0f, slY),
                            end = Offset(size.width, slY),
                            strokeWidth = 1.5f,
                            pathEffect = dashEffect
                        )
                    }
                }
            }
        }
    }
}
