package com.example.weatherapp.ui.expense

import android.app.Application
import android.content.Context
import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.model.Expense
import com.example.weatherapp.model.ExpenseConstants
import com.example.weatherapp.service.ExpenseParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayInputStream

sealed interface ExpenseUiState {
    object Idle : ExpenseUiState
    object Loading : ExpenseUiState
    data class Success(val message: String) : ExpenseUiState
    data class Error(val message: String) : ExpenseUiState
}

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

    private val _expenses = MutableStateFlow<List<Expense>>(emptyList())
    val expenses: StateFlow<List<Expense>> = _expenses.asStateFlow()

    private val _uiState = MutableStateFlow<ExpenseUiState>(ExpenseUiState.Idle)
    val uiState: StateFlow<ExpenseUiState> = _uiState.asStateFlow()

    init {
        // Load saved expenses on startup
        val saved = loadExpenses()
        _expenses.value = sortListByDate(saved)
    }

    // Sort helper: maps MM/DD/YYYY and YYYY-MM-DD into a sortable YYYYMMDD string
    private fun getSortableDateString(date: String): String {
        val trimmed = date.trim()
        if (trimmed.isEmpty()) return "00000000"

        // Format 1: MM/DD/YYYY
        if (trimmed.contains("/")) {
            val parts = trimmed.split("/")
            if (parts.size >= 3) {
                val mm = parts[0].padStart(2, '0')
                val dd = parts[1].padStart(2, '0')
                val yyyy = parts[2]
                return "$yyyy$mm$dd"
            }
        }

        // Format 2: YYYY-MM-DD
        if (trimmed.contains("-")) {
            val parts = trimmed.split("-")
            if (parts.size >= 3) {
                val yyyy = parts[0]
                val mm = parts[1].padStart(2, '0')
                val dd = parts[2].padStart(2, '0')
                return "$yyyy$mm$dd"
            }
        }

        return trimmed.filter { it.isDigit() }.padEnd(8, '0')
    }

    private fun sortListByDate(list: List<Expense>): List<Expense> {
        return list.sortedByDescending { getSortableDateString(it.date) }
    }

    private fun updateAndPersistList(newList: List<Expense>) {
        val sorted = sortListByDate(newList)
        _expenses.value = sorted
        saveExpenses(sorted)
    }



    fun addExpense(name: String, category: String, amount: Double, date: String = "", type: String = "Expense") {
        if (name.isBlank() || category.isBlank() || amount <= 0.0) return
        val newList = _expenses.value.toMutableList()
        newList.add(Expense(name.trim(), category.trim(), amount, date, type))
        updateAndPersistList(newList)
        _uiState.value = ExpenseUiState.Success("Added $type: ${name.trim()}")
    }

    fun editExpense(oldExpense: Expense, newExpense: Expense) {
        val newList = _expenses.value.toMutableList()
        val index = newList.indexOf(oldExpense)
        if (index != -1) {
            newList[index] = newExpense
            updateAndPersistList(newList)
            val typeLabel = if (newExpense.type == "Income") "income" else "expense"
            _uiState.value = ExpenseUiState.Success("Updated $typeLabel: ${newExpense.name}")
        }
    }

    fun deleteExpense(expense: Expense) {
        val newList = _expenses.value.toMutableList()
        if (newList.remove(expense)) {
            updateAndPersistList(newList)
            val typeLabel = if (expense.type == "Income") "income" else "expense"
            _uiState.value = ExpenseUiState.Success("Removed $typeLabel: ${expense.name}")
        }
    }

    fun clearExpenses() {
        updateAndPersistList(emptyList())
        _uiState.value = ExpenseUiState.Idle
    }

    fun pushSampleData() {
        val samples = ExpenseConstants.SAMPLE_EXPENSES.toMutableList()
        samples.add(Expense("Bi-Weekly Salary Pay", "Salary", 2450.00, "2026-05-01", "Income"))
        samples.add(Expense("Freelance Logo Design", "Freelance / Work", 350.00, "2026-05-05", "Income"))
        val currentList = _expenses.value.toMutableList()
        currentList.addAll(samples)
        updateAndPersistList(currentList)
        _uiState.value = ExpenseUiState.Success("Successfully populated ${samples.size} premium sample transactions.")
    }

    fun importRawCsvText(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            _uiState.value = ExpenseUiState.Loading
            try {
                val parsedList = withContext(Dispatchers.IO) {
                    ExpenseParser.parseRawCsvText(text)
                }
                if (parsedList.isNotEmpty()) {
                    val currentList = _expenses.value.toMutableList()
                    currentList.addAll(parsedList)
                    updateAndPersistList(currentList)
                    _uiState.value = ExpenseUiState.Success("Successfully appended ${parsedList.size} copy-pasted transactions.")
                } else {
                    _uiState.value = ExpenseUiState.Error("No valid rows found. Format: Date,Expense Description,Category,Amount.")
                }
            } catch (e: Exception) {
                _uiState.value = ExpenseUiState.Error(e.message ?: "Failed to parse CSV text.")
            }
        }
    }

    fun restoreBackupCsvText(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            _uiState.value = ExpenseUiState.Loading
            try {
                val parsedList = withContext(Dispatchers.IO) {
                    ExpenseParser.parseRawCsvText(text)
                }
                if (parsedList.isNotEmpty()) {
                    updateAndPersistList(parsedList)
                    _uiState.value = ExpenseUiState.Success("Successfully restored and replaced database with ${parsedList.size} transactions.")
                } else {
                    _uiState.value = ExpenseUiState.Error("No valid backup rows found. Format: Date,Description,Category,Amount,Type.")
                }
            } catch (e: Exception) {
                _uiState.value = ExpenseUiState.Error(e.message ?: "Failed to restore backup.")
            }
        }
    }

    fun importFile(contentResolver: ContentResolver, uri: Uri, fileName: String) {
        viewModelScope.launch {
            _uiState.value = ExpenseUiState.Loading
            try {
                val parsedList = withContext(Dispatchers.IO) {
                    contentResolver.openInputStream(uri)?.use { inputStream ->
                        if (fileName.lowercase().endsWith(".xlsx")) {
                            ExpenseParser.parseXlsx(inputStream)
                        } else if (fileName.lowercase().endsWith(".csv")) {
                            ExpenseParser.parseCsv(inputStream)
                        } else {
                            // Fallback try both
                            val bytes = inputStream.readBytes()
                            val xlsxResult = ExpenseParser.parseXlsx(ByteArrayInputStream(bytes))
                            if (xlsxResult.isNotEmpty()) {
                                xlsxResult
                            } else {
                                ExpenseParser.parseCsv(ByteArrayInputStream(bytes))
                            }
                        }
                    } ?: emptyList()
                }

                if (parsedList.isNotEmpty()) {
                    val currentList = _expenses.value.toMutableList()
                    currentList.addAll(parsedList)
                    updateAndPersistList(currentList)
                    _uiState.value = ExpenseUiState.Success("Successfully imported and appended ${parsedList.size} transactions.")
                } else {
                    _uiState.value = ExpenseUiState.Error("No valid rows found. Ensure format matches Col A (Category), Col B (Amount).")
                }
            } catch (e: Exception) {
                _uiState.value = ExpenseUiState.Error(e.message ?: "Failed to import spreadsheet file.")
            }
        }
    }

    // Persist list to SharedPreferences
    private fun saveExpenses(list: List<Expense>) {
        try {
            val context = getApplication<Application>().applicationContext
            val sharedPrefs = context.getSharedPreferences("expense_prefs", Context.MODE_PRIVATE)
            val jsonArray = JSONArray()
            for (exp in list) {
                val jsonObj = JSONObject()
                jsonObj.put("name", exp.name)
                jsonObj.put("category", exp.category)
                jsonObj.put("amount", exp.amount)
                jsonObj.put("date", exp.date)
                jsonObj.put("type", exp.type)
                jsonArray.put(jsonObj)
            }
            sharedPrefs.edit().putString("expenses_list", jsonArray.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Load list from SharedPreferences
    private fun loadExpenses(): List<Expense> {
        val context = getApplication<Application>().applicationContext
        val sharedPrefs = context.getSharedPreferences("expense_prefs", Context.MODE_PRIVATE)
        val jsonStr = sharedPrefs.getString("expenses_list", null) ?: return emptyList()
        val result = mutableListOf<Expense>()
        try {
            val jsonArray = JSONArray(jsonStr)
            for (i in 0 until jsonArray.length()) {
                val jsonObj = jsonArray.getJSONObject(i)
                result.add(
                    Expense(
                        jsonObj.getString("name"),
                        jsonObj.getString("category"),
                        jsonObj.getDouble("amount"),
                        jsonObj.optString("date", ""),
                        jsonObj.optString("type", "Expense")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }
}
