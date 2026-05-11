# HealthTracker 🔴

A minimalist, high-performance health and nutrition tracker inspired by the **Nothing OS** aesthetic. Designed to be "snappy," tactile, and distraction-free.

![Nothing Design](https://img.shields.io/badge/Design-Nothing%20OS-black?style=for-the-badge)
![Kotlin](https://img.shields.io/badge/Kotlin-2.1.0-blue?style=for-the-badge&logo=kotlin)
![Compose](https://img.shields.io/badge/Jetpack-Compose-green?style=for-the-badge&logo=android)

## 📱 Features

- **Dot-Matrix Dashboard**: Monitor your daily calorie and protein intake with custom-built Nothing-style tachometer gauges.
- **Dynamic Profile Ecosystem**: Real-time synchronization of BMR, TDEE, and Protein targets based on your latest weight entries.
- **Smart Barcode Scanner**:
    - **Nothing Viewfinder**: Custom square viewfinder with a dashed/dotted border.
    - **Tactile Feedback**: Haptic "ticks" provide physical confirmation on successful scans.
    - **Open Food Facts Integration**: Instantly log food by scanning barcodes, powered by the world's largest open food database.
- **Offline-First Performance**: Recently scanned items are cached locally in Room for instant access, even without an internet connection.
- **Live Macro Scaling**: Adjust portion sizes in real-time and watch your macros scale instantly before adding them to your log.
- **Cloud Sync & Backup**:
    - **Direct Google Drive Backup**: Export and import your database manually via the Storage Access Framework.
    - **Automated Cloud Backup**: Seamless Android Auto-Backup integration for effortless data recovery.
- **Minimalist Weight Tracking**: Log your weight with a clean, simple interface and track your progress over time.

## 🛠️ Technical Stack

- **UI**: Jetpack Compose with custom components (Dot Matrix headers, Pulsing loaders, Tachometer gauges).
- **Architecture**: MVVM (Model-View-ViewModel) with a Repository pattern.
- **Networking**: Retrofit 3.0 + OkHttp 5 + Kotlinx Serialization.
- **Local Storage**: Room Database with Write-Ahead-Logging (WAL) and DataStore for user preferences.
- **ML & Camera**: Google ML Kit Barcode Scanning + CameraX.
- **Navigation**: Compose Navigation with a custom "Floating Pill" navigation bar.
- **Splash Screen**: Android Core Splashscreen API with a custom Nothing-themed animated icon.

## 🎨 Design Philosophy

HealthTracker adheres to the **Nothing OS** design language:
- **Pure Black Theme**: Absolute `#000000` background for maximum OLED contrast and battery efficiency.
- **Authentic Typography**: Heavy use of **NType82** for headers and data points.
- **Nothing Red**: Used sparingly as an accent color (`#D71921`) for primary actions and status indicators.
- **Zero Latency**: Optimised transitions and instant feedback to ensure the app feels "snappy" and tactile.

## 🚀 Getting Started

1. **Clone the repository**:
   ```bash
   git clone https://github.com/tenebris/healthtracker.git
   ```
2. **Open in Android Studio**:
   Requires **Android Studio Ladybug (2024.2.1)** or newer.
3. **Build & Run**:
   Connect a device (Nothing Phone recommended for full haptic effect!) and hit **Run**.

## 🤝 Contributing

This project uses the **Open Food Facts API**. If you find a product missing or with incorrect data, please consider contributing directly to the [Open Food Facts](https://world.openfoodfacts.org/) database.

---

*Designed with 🔴 in Berlin.*
