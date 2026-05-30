package com.example.weatherapp.model

import androidx.compose.ui.graphics.Color

data class Expense(
    val name: String,
    val category: String,
    val amount: Double,
    val date: String = "",
    val type: String = "Expense"
)

object ExpenseConstants {
    
    // Pre-populated common category list
    val COMMON_CATEGORIES = listOf(
        "Food", 
        "Transport", 
        "Rent", 
        "Shopping", 
        "Utilities", 
        "Entertainment", 
        "Health", 
        "Insurance",
        "Transfer",
        "Bank Fees",
        "Other"
    )

    // Pre-populated common income categories list
    val COMMON_INCOME_CATEGORIES = listOf(
        "Salary",
        "Freelance / Work",
        "Investments",
        "Gifts",
        "Other Income"
    )

    // Map standard categories to icons/emojis
    fun getCategoryIcon(category: String): String {
        return when (category.trim().lowercase()) {
            "food", "dining", "groceries", "restaurant", "dining / food" -> "🍔"
            "transport", "taxi", "uber", "gas", "car", "transportation", "transit" -> "🚗"
            "rent", "housing", "mortgage", "stay", "housing / mortgage" -> "🏠"
            "shopping", "clothing", "clothes", "gifts", "retail / services" -> "🛍️"
            "utilities", "electricity", "water", "internet", "bills", "utilities / telecom" -> "💡"
            "entertainment", "movie", "games", "netflix", "fun", "dining / entertainment" -> "🎭"
            "health", "medical", "doctor", "pharmacy", "insurance" -> "🩺"
            "salary" -> "💰"
            "freelance / work", "freelance", "work" -> "💻"
            "investments", "investment" -> "📈"
            "gifts", "gift" -> "🎁"
            "other income", "income" -> "💵"
            else -> "💵"
        }
    }

    // Map standard categories to colors for visualization
    fun getCategoryColor(category: String): Color {
        return when (category.trim().lowercase()) {
            "food", "dining", "groceries", "restaurant", "dining / food" -> Color(0xFFFF7043) // Coral
            "transport", "taxi", "uber", "gas", "car", "transportation", "transit" -> Color(0xFF29B6F6) // Sky Blue
            "rent", "housing", "mortgage", "stay", "housing / mortgage" -> Color(0xFF66BB6A) // Green
            "shopping", "clothing", "clothes", "gifts", "retail / services" -> Color(0xFFAB47BC) // Purple
            "utilities", "electricity", "water", "internet", "bills", "utilities / telecom" -> Color(0xFFFFCA28) // Amber
            "entertainment", "movie", "games", "netflix", "fun", "dining / entertainment" -> Color(0xFFEC407A) // Pink
            "health", "medical", "doctor", "pharmacy", "insurance" -> Color(0xFF26A69A) // Teal
            "salary" -> Color(0xFF34D399) // Emerald Green
            "freelance / work", "freelance", "work" -> Color(0xFF60A5FA) // Blue
            "investments", "investment" -> Color(0xFF818CF8) // Indigo
            "gifts", "gift" -> Color(0xFFF472B6) // Pink
            "other income", "income" -> Color(0xFF059669) // Forest Green
            else -> Color(0xFF78909C) // Grey-Blue
        }
    }

    // List of premium sample data for visual pre-population
    val SAMPLE_EXPENSES = listOf(
        Expense("Monthly Rent Payment", "Rent", 1200.0, "2026-05-01", "Expense"),
        Expense("Alforno Bakery Lunch", "Food", 45.50, "2026-05-02", "Expense"),
        Expense("Uber Ride to Downtown", "Transport", 20.0, "2026-05-03", "Expense"),
        Expense("New Summer Clothing", "Shopping", 215.80, "2026-05-04", "Expense"),
        Expense("Telus Internet Bill", "Utilities", 95.0, "2026-05-05", "Expense"),
        Expense("Cinema Movie Ticket", "Entertainment", 15.0, "2026-05-06", "Expense"),
        Expense("Pharmacy Prescription", "Health", 35.0, "2026-05-07", "Expense")
    )
}
