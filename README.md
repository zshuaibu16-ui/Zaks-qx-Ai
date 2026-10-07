# ZAKS QX AI

> **AI Market Analysis & Signal System**  
> Educational and research dashboard for Binary Options, Forex, OTC pairs, and Synthetic Indices.

[![Platform](https://img.shields.io/badge/Platform-Android%20%7C%20Jetpack%20Compose-green.svg)](https://developer.android.com/)
[![Language](https://img.shields.io/badge/Language-Kotlin-purple.svg)](https://kotlinlang.org/)
[![Database](https://img.shields.io/badge/Persistence-Room-blue.svg)](https://developer.android.com/training/data-storage/room)
[![Repository](https://img.shields.io/badge/GitHub-zshuaibu16--ui%2FZaks--qx--Ai-gold.svg)](https://github.com/zshuaibu16-ui/Zaks-qx-Ai)

---

## 🌟 Key Features

### 1. Dedicated Signal Engines
- **Binary Options**: 1M, 2M, 3M, 4M, 5M expiry timeframes with CALL/PUT directional bias and an active countdown timer.
- **Forex Engine**: 5M, 15M, 30M, 1H timeframes with Entry, Stop Loss, Take Profit 1, Take Profit 2, and 1:2 Risk-to-Reward ratio.
- **OTC Scanner**: Automated multi-pair scanner for 7 OTC assets (`EUR/USD OTC`, `GBP/USD OTC`, `USD/JPY OTC`, `AUD/CAD OTC`, etc.) clearly labeled as synthetic quotations.
- **Synthetic Indices**: Algo-driven analysis for `Volatility 75 (1s)`, `Volatility 100`, `Boom 500`, `Crash 500`, `Step Index`, and `Jump 25`.

### 2. Multi-Indicator Confluence (Max 100 Pts)
The system requires multi-indicator agreement before issuing signals:
- **MACD (12, 26, 9)**: Crossovers & histogram momentum (15 pts)
- **Stochastic Oscillator (14, 3)**: %K / %D momentum turns (15 pts)
- **RSI (14)**: Relative momentum strength (10 pts)
- **EMA Trend (9, 21, 50, 200)**: Trend stacking alignment (15 pts)
- **Price Action & Candlestick Pattern**: (15 pts)
- **Market Structure**: (15 pts)
- **ADX Trend Strength**: Wilder's DMI validation (10 pts)
- **Volume Confirmation**: Tick volume alignment (5 pts)

**Transparent Grading Scale:**
- `90–100`: VERY STRONG
- `80–89`: STRONG
- `70–79`: MODERATE
- `< 70`: **WAIT / NO TRADE** (Strict filter when indicators conflict)

### 3. Candlestick Pattern & Market Structure Engine
- **Patterns**: Hammer, Inverted Hammer, Doji, Bullish/Bearish Engulfing, Morning/Evening Star, Shooting Star, Pin Bar, Inside Bar, Three White Soldiers, Three Black Crows.
- **Market Structure**: Swing detection for Higher High (HH), Higher Low (HL), Lower High (LH), Lower Low (LL), Break of Structure (BOS), and Change of Character (CHoCH).

### 4. Signal Journal & Performance Dashboard
- **Room SQLite Persistence**: Saves every generated signal with timestamp, asset, direction, timeframe, entry, SL, TP, confidence, outcome result (`WIN`, `LOSS`, `PENDING`, `EXPIRED`), and P&L.
- **Audited Performance Metrics**: Evaluates Today's, 7-Day Weekly, 30-Day Monthly, and All-Time win rates and P&L exclusively from recorded journal trades.
- **Canvas Equity Growth Curve**: Visualizes cumulative profitability.

### 5. Multilingual Voice AI Analysis
- Supports voice commands and AI responses in **English**, **Hausa** (*Bincika EUR/USD a minti 15*), **Arabic** (*تحليل EUR/USD على 15 دقيقة*), and **French** (*Analyser EUR/USD sur 15 minutes*).
- Built-in Android Speech Recognition and Text-to-Speech (TTS) readout.

### 6. Telegram Bot Integration
- Standardized signal export ready to copy or dispatch directly to Telegram channels.

---

## 🏗️ Architecture & Tech Stack

- **UI Framework**: Jetpack Compose (Material Design 3)
- **Language**: Kotlin 100%
- **Architecture**: MVVM (Model-View-ViewModel) + Repository Pattern
- **Persistence**: Room Database with Coroutines Flow
- **Design Identity**: Luxury Obsidian Dark (`#0B0E14`) + Gold Metallic Accents (`#FFB800`)

---

## 🚀 Building the Project

### Prerequisites
- Android Studio Ladybug / Meerkat or Android SDK 36
- JDK 17 / 21

### Assemble Debug APK
```bash
gradle :app:assembleDebug
```
The generated APK will be located at:
```
app/build/outputs/apk/debug/app-debug.apk
```

### Run Unit & Robolectric Tests
```bash
gradle :app:testDebugUnitTest
```

---

## ⚖️ Disclaimer
*ZAKS QX AI is developed for educational and research market analysis purposes. Trading foreign exchange, binary options, and synthetic indices carries substantial risk. The application does not guarantee profits and strictly reports performance from user-recorded journals.*
