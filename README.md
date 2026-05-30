# 🏠 Home Expense Dashboard — Jetpack Compose Android App

A premium, modern, and highly interactive Android expense-tracking application built using **Kotlin**, **Jetpack Compose**, **Material 3**, and **Clean Architecture (MVVM)**. This app is designed to provide users with a sleek, glassmorphic dark-mode dashboard to track financial inflows, outflows, and net balances dynamically.

---

## 🚀 Key Features

*   **✨ Pre-Populated Mock Data**: Instantly generate a balanced set of premium transactions (expenses and income) to explore dashboard capabilities immediately.
*   **📊 Dynamic Financial Analytics**: Real-time calculation of Total Inflow, Total Outflow, and Net Balance with category-wise visual filtering.
*   **🎨 Premium Glassmorphic UI/UX**: Designed with a sleek, Harmonious Dark Mode (Indigo & Emerald accents), rounded layouts, custom micro-interactions, and premium typography.
*   **🔍 Full-Text Search Timeline**: Search and filter transactions instantly by name, description, or category.
*   **📤 Data Backup & Share**: Export your entire transaction ledger to a secure, shareable CSV string, and share it with other system apps (Email, WhatsApp, Drive).
*   **📥 CSV Restore & Replace**: Paste CSV backup data directly into the Hub settings to restore your financial database cleanly.
*   **📂 Persistent Storage**: Saves data locally using secure SharedPreferences with JSON serialization, preserving state across device reboots.

---

## 🛠️ Tech Stack & Architecture

*   **Language**: Kotlin
*   **UI Framework**: Jetpack Compose (Declarative UI)
*   **UI Components**: Material 3 Design System
*   **Architecture Pattern**: MVVM (Model-View-ViewModel)
*   **Reactive Streams**: Kotlin Coroutines, StateFlow (`collectAsStateWithLifecycle`)
*   **Build System**: Gradle Kotlin DSL (`.kts`)

---

## 📂 Project Structure

```
app/src/main/java/com/example/weatherapp/
│
├── model/
│   ├── ExpenseModel.kt         # Data structures and constants
│   
├── service/
│   ├── ExpenseParser.kt        # CSV / Excel parsing engines
│   
└── ui/
    └── expense/
        ├── ExpenseScreen.kt    # Declarative Compose UI screens
        ├── ExpenseViewModel.kt # MVVM ViewModel managing business logic and persistence
```

---

## 🏗️ Getting Started

### Prerequisites
*   Android Studio Ladybug (or higher)
*   JDK 17+

### Installation
1. Clone this repository:
   ```bash
   git clone https://github.com/YOUR_USERNAME/HomeExpenseApp.git
   ```
2. Open the project in **Android Studio**.
3. Let Gradle sync and download dependencies.
4. Run on an Android Emulator or a physical device.

---

## 📝 Resume Showcase Highlights
If you are adding this project to your resume, here are high-impact bullet points you can use:
*   *Designed and built a native Android financial ledger application using Jetpack Compose and Material 3, achieving a premium, responsive glassmorphic UI.*
*   *Implemented the MVVM architecture pattern with Kotlin Coroutines and StateFlow for robust, reactive state management and modern lifecycle-aware data streams.*
*   *Developed a custom CSV import/export utility supporting SharedPreferences JSON serialization, providing reliable offline data persistence.*
