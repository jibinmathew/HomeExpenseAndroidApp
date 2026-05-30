package com.example.weatherapp.ui.expense

import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weatherapp.model.Expense
import com.example.weatherapp.model.ExpenseConstants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseScreen(
    modifier: Modifier = Modifier,
    viewModel: ExpenseViewModel = viewModel()
) {
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var currentScreen by remember { mutableStateOf("dashboard") } // "dashboard" or "data_hub"

    // State for manual Add/Edit Dialog Dialog
    var showFormDialog by remember { mutableStateOf(false) }
    var editingExpense by remember { mutableStateOf<Expense?>(null) } // Non-null if editing an item

    // Form inputs state
    var formName by remember { mutableStateOf("") }
    var formType by remember { mutableStateOf("Expense") } // "Expense" or "Income"
    var formCategory by remember { mutableStateOf(ExpenseConstants.COMMON_CATEGORIES.first()) }
    var formAmount by remember { mutableStateOf("") }
    var formDate by remember { mutableStateOf("") }
    var dropdownExpanded by remember { mutableStateOf(false) }



    // Helper to open form for ADDing Expense
    val openAddExpenseForm = {
        editingExpense = null
        formName = ""
        formType = "Expense"
        formCategory = ExpenseConstants.COMMON_CATEGORIES.first()
        formAmount = ""
        formDate = "05/27/2026" // Default current date
        showFormDialog = true
    }

    // Helper to open form for ADDing Income
    val openAddIncomeForm = {
        editingExpense = null
        formName = ""
        formType = "Income"
        formCategory = ExpenseConstants.COMMON_INCOME_CATEGORIES.first()
        formAmount = ""
        formDate = "05/27/2026" // Default current date
        showFormDialog = true
    }

    // Helper to open form for EDITing
    val openEditForm = { exp: Expense ->
        editingExpense = exp
        formName = exp.name
        formType = exp.type
        formCategory = exp.category
        formAmount = exp.amount.toString()
        formDate = exp.date
        showFormDialog = true
    }

    // Launcher for file picker
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            var displayName = "file.xlsx"
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1 && cursor.moveToFirst()) {
                    displayName = cursor.getString(nameIndex)
                }
            }
            viewModel.importFile(context.contentResolver, uri, displayName)
        }
    }

    // Dynamic background gradient
    val backgroundBrush = Brush.verticalGradient(
        listOf(Color(0xFF1E1B4B), Color(0xFF0F172A))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // App title & Nav Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "🏠",
                        fontSize = 15.sp
                    )
                    Text(
                        text = "HOME EXPENSE",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.7f),
                        letterSpacing = 1.5.sp
                    )
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Search Link Capsule
                    Text(
                        text = "🔍 Search",
                        color = if (currentScreen == "search") Color.White else Color(0xFF818CF8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (currentScreen == "search") Color(0xFF6366F1) else Color.White.copy(alpha = 0.05f))
                            .clickable { currentScreen = "search" }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    )

                    // Data Hub Link Capsule
                    Text(
                        text = "⚙️ Hub",
                        color = if (currentScreen == "data_hub") Color.White else Color(0xFF818CF8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (currentScreen == "data_hub") Color(0xFF6366F1) else Color.White.copy(alpha = 0.05f))
                            .clickable { currentScreen = "data_hub" }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    )

                    if (currentScreen != "dashboard") {
                        Text(
                            text = "🏠 Home",
                            color = Color(0xFF34D399),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.05f))
                                .clickable { currentScreen = "dashboard" }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            if (currentScreen == "dashboard") {
                Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons Row (Add Expense, Add Income)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Add Expense Button
                Button(
                    onClick = openAddExpenseForm,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)), // Indigo Accent
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("➕ Expense", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                // Add Income Button
                Button(
                    onClick = openAddIncomeForm,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)), // Emerald Green
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("➕ Income", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Parser State/Message Bar
            AnimatedVisibility(
                visible = uiState != ExpenseUiState.Idle,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                when (val state = uiState) {
                    is ExpenseUiState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color(0xFF818CF8), modifier = Modifier.size(32.dp))
                        }
                    }
                    is ExpenseUiState.Success -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF065F46).copy(alpha = 0.2f))
                                .border(1.dp, Color(0xFF059669).copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = state.message, color = Color(0xFF34D399), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                    is ExpenseUiState.Error -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF991B1B).copy(alpha = 0.2f))
                                .border(1.dp, Color(0xFFDC2626).copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = state.message, color = Color(0xFFFCA5A5), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                    else -> {}
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (expenses.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(280.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No financial data imported yet.\nTap Add Expense or Add Income to build dashboard breakdowns.",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                }
            } else {
                // Loaded Dashboard View
                ExpenseDashboard(expenses = expenses)

                Spacer(modifier = Modifier.height(14.dp))

                // Financial ledger list
                Text(
                    text = "FINANCIAL LEDGER TIMELINE",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.6f),
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Dynamic Category Filter Row
                val uniqueCategories = remember(expenses) {
                    listOf("All") + expenses.map { it.category.trim() }.distinct().sorted()
                }
                var selectedCategoryFilter by remember { mutableStateOf("All") }

                if (uniqueCategories.size > 2) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        uniqueCategories.forEach { category ->
                            val isSelected = selectedCategoryFilter == category
                            val emoji = if (category == "All") "🔍" else ExpenseConstants.getCategoryIcon(category)
                            
                            // Dynamic sum calculation for this specific category
                            val categoryTotal = remember(expenses, category) {
                                if (category == "All") {
                                    expenses.sumOf { if (it.type == "Income") it.amount else -it.amount }
                                } else {
                                    val catList = expenses.filter { it.category.trim() == category }
                                    catList.sumOf { if (it.type == "Income") it.amount else -it.amount }
                                }
                            }
                            
                            val formattedTotal = remember(categoryTotal) {
                                val sign = if (categoryTotal >= 0.0) "+" else "-"
                                val absAmt = java.lang.Math.abs(categoryTotal)
                                "$sign$${String.format("%.2f", absAmt)}"
                            }
                            
                            val displayLabel = if (category == "All") {
                                "All (${formattedTotal})"
                            } else {
                                "$category (${formattedTotal})"
                            }
                            
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isSelected) Color(0xFF6366F1) else Color.White.copy(alpha = 0.05f))
                                    .border(
                                        0.5.dp, 
                                        if (isSelected) Color(0xFF818CF8) else Color.White.copy(alpha = 0.12f), 
                                        RoundedCornerShape(20.dp)
                                    )
                                    .clickable { selectedCategoryFilter = category }
                                    .padding(horizontal = 14.dp, vertical = 7.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(text = emoji, fontSize = 12.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = displayLabel,
                                        color = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                val filteredExpenses = remember(expenses, selectedCategoryFilter) {
                    if (selectedCategoryFilter == "All") {
                        expenses
                    } else {
                        expenses.filter { it.category.trim() == selectedCategoryFilter }
                    }
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (filteredExpenses.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No transactions found under this category filter.",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.4f)
                            )
                        }
                    } else {
                        filteredExpenses.forEach { exp ->
                            ExpenseItemRow(
                                expense = exp,
                                onEdit = { openEditForm(exp) },
                                onDelete = { viewModel.deleteExpense(exp) }
                            )
                        }
                    }
                }
            }
        } else if (currentScreen == "search") {
            Spacer(modifier = Modifier.height(20.dp))
            SearchScreen(
                expenses = expenses,
                viewModel = viewModel,
                onBack = { currentScreen = "dashboard" },
                onEditExpense = { openEditForm(it) }
            )
        } else {
            Spacer(modifier = Modifier.height(20.dp))
            DataHubScreen(
                expenses = expenses, 
                viewModel = viewModel, 
                context = context,
                onBack = { currentScreen = "dashboard" }
            )
        }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    // Premium Add/Edit Form Dialog modal
    if (showFormDialog) {
        AlertDialog(
            onDismissRequest = { showFormDialog = false },
            containerColor = Color(0xFF1E1B4B), // Custom Deep Indigo Dark Dialog
            title = {
                Text(
                    text = if (editingExpense != null) "Edit Transaction" else "Add Transaction",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Transaction Type Selector (Expense vs Income)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("Expense", "Income").forEach { type ->
                            val isSelected = formType == type
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) {
                                        if (type == "Income") Color(0xFF10B981) else Color(0xFF6366F1)
                                    } else Color.Transparent)
                                    .clickable { 
                                        formType = type
                                        // Auto-swap default category to prevent mis-mapping
                                        formCategory = if (type == "Income") {
                                            ExpenseConstants.COMMON_INCOME_CATEGORIES.first()
                                        } else {
                                            ExpenseConstants.COMMON_CATEGORIES.first()
                                        }
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = type,
                                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    // Outlined Text field for Name
                    OutlinedTextField(
                        value = formName,
                        onValueChange = { formName = it },
                        label = { Text("Name / Description", color = Color.White.copy(alpha = 0.6f)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF818CF8),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Editable Category field
                    OutlinedTextField(
                        value = formCategory,
                        onValueChange = { formCategory = it },
                        label = { Text("Category", color = Color.White.copy(alpha = 0.6f)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF818CF8),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Quick Suggestions label
                    Text(
                        text = "Quick suggestions:",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Medium
                    )

                    // Quick-fill Category Suggestion Row
                    val suggestions = if (formType == "Income") {
                        listOf("Salary", "Other", "Freelance", "Investments")
                    } else {
                        listOf("Food", "Transport", "Rent", "Shopping", "Utilities", "Other")
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        suggestions.forEach { sug ->
                            val isSelected = formCategory.trim().equals(sug, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isSelected) {
                                        if (formType == "Income") Color(0xFF10B981) else Color(0xFF6366F1)
                                    } else Color.White.copy(alpha = 0.08f))
                                    .clickable { formCategory = sug }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = sug,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Outlined Text field for Amount
                    OutlinedTextField(
                        value = formAmount,
                        onValueChange = { formAmount = it },
                        label = { Text("Amount ($)", color = Color.White.copy(alpha = 0.6f)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF818CF8),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Outlined Text field for Date (Optional)
                    OutlinedTextField(
                        value = formDate,
                        onValueChange = { formDate = it },
                        label = { Text("Date (MM/DD/YYYY)", color = Color.White.copy(alpha = 0.6f)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF818CF8),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = formAmount.toDoubleOrNull() ?: 0.0
                        if (formName.isNotBlank() && amt > 0.0) {
                            val old = editingExpense
                            if (old != null) {
                                viewModel.editExpense(old, Expense(formName.trim(), formCategory, amt, formDate.trim(), formType))
                            } else {
                                viewModel.addExpense(formName.trim(), formCategory, amt, formDate.trim(), formType)
                            }
                            showFormDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (formType == "Income") Color(0xFF10B981) else Color(0xFF6366F1)
                    )
                ) {
                    Text(if (editingExpense != null) "Update" else "Add", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showFormDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.12f))
                ) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }
}

@Composable
fun ExpenseDashboard(expenses: List<Expense>) {
    // 1. Separate transactions by type
    val incomeList = expenses.filter { it.type == "Income" }
    val expenseList = expenses.filter { it.type == "Expense" }

    // 2. Sum transactions
    val totalIncome = incomeList.sumOf { it.amount }
    val totalExpense = expenseList.sumOf { it.amount }
    val netBalance = totalIncome - totalExpense

    // 3. Keep track of visual toggle state for the Donut Pie Chart Breakdown (Expense vs Income)
    var chartTypeToggle by remember { mutableStateOf("Expense") } // "Expense" or "Income"
    var isChartExpanded by remember { mutableStateOf(false) }

    // 4. Calculate variables for the active chart tab
    val activeList = if (chartTypeToggle == "Income") incomeList else expenseList
    val activeTotal = activeList.sumOf { it.amount }
    val activeGroups = activeList.groupBy { it.category.trim() }
    val activeAggregated = activeGroups.mapValues { entry -> entry.value.sumOf { it.amount } }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Core Statistics Cards Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Card 1: Total Inflow
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.07f))
                    .border(0.5.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text("Total Inflow", fontSize = 11.sp, color = Color.White.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$${String.format("%.2f", totalIncome)}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF34D399)
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    val count = incomeList.size
                    Text(
                        text = "$count ${if (count == 1) "Inflow" else "Inflows"}",
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.4f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Card 2: Total Outflow
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.07f))
                    .border(0.5.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text("Total Outflow", fontSize = 11.sp, color = Color.White.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$${String.format("%.2f", totalExpense)}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    val count = expenseList.size
                    Text(
                        text = "$count ${if (count == 1) "Outflow" else "Outflows"}",
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.4f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Card 3: Net Balance
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.07f))
                    .border(0.5.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text("Net Balance", fontSize = 11.sp, color = Color.White.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(4.dp))
                    val sign = if (netBalance >= 0.0) "+" else ""
                    Text(
                        text = "$sign$${String.format("%.2f", netBalance)}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (netBalance >= 0.0) Color(0xFF34D399) else Color(0xFFF87171)
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    val count = expenses.size
                    Text(
                        text = "$count ${if (count == 1) "Transaction" else "Transactions"}",
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.4f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Allocation Section Header with Minimize Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "📊 ALLOCATION BREAKDOWN",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.6f),
                letterSpacing = 1.5.sp
            )
            Text(
                text = if (isChartExpanded) "🔽 Hide Chart" else "🔼 Show Chart",
                color = Color(0xFF818CF8),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .clickable { isChartExpanded = !isChartExpanded }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        AnimatedVisibility(visible = isChartExpanded) {
            Column {
                // Donut Chart Segmented Selector Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.06f))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("Expense", "Income").forEach { type ->
                        val isSelected = chartTypeToggle == type
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) Color.White.copy(alpha = 0.15f) else Color.Transparent)
                                .clickable { chartTypeToggle = type }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (type == "Expense") "Visual Expenses" else "Visual Income",
                                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.5f),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // High-end Custom Canvas Donut Chart Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White.copy(alpha = 0.1f))
                        .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                        .padding(24.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "${chartTypeToggle.uppercase()} ALLOCATION",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.6f),
                            letterSpacing = 1.5.sp
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        if (activeTotal <= 0.0) {
                            // Premium Empty Breakdown state
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No ${chartTypeToggle.lowercase()} data recorded to visualize yet.",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.4f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            // Chart and Legend Layout
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier.size(140.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        var currentStartAngle = -90f
                                        activeAggregated.forEach { (cat, amount) ->
                                            val sweep = ((amount / activeTotal) * 360f).toFloat()
                                            drawArc(
                                                color = ExpenseConstants.getCategoryColor(cat),
                                                startAngle = currentStartAngle,
                                                sweepAngle = sweep,
                                                useCenter = false,
                                                style = Stroke(width = 24f)
                                            )
                                            currentStartAngle += sweep
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(text = "TOTAL", fontSize = 9.sp, color = Color.White.copy(alpha = 0.5f), fontWeight = FontWeight.Bold)
                                        Text(text = "$${String.format("%.0f", activeTotal)}", fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Black)
                                    }
                                }

                                Spacer(modifier = Modifier.width(20.dp))

                                // Legend
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    activeAggregated.forEach { (cat, amount) ->
                                        val percent = (amount / activeTotal) * 100
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Start
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .clip(RoundedCornerShape(3.dp))
                                                    .background(ExpenseConstants.getCategoryColor(cat))
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "$cat (${String.format("%.0f", percent)}%)",
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExpenseItemRow(
    expense: Expense,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .border(0.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Category Icon Block
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(ExpenseConstants.getCategoryColor(expense.category).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = ExpenseConstants.getCategoryIcon(expense.category), fontSize = 22.sp)
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Details (Name and Date/Category Outflow description)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = expense.name,
                        fontSize = 14.sp,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (expense.date.isNotEmpty()) "${expense.category} • ${expense.date}" else expense.category,
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.4f)
                    )
                }

                // Amount
                val isIncome = expense.type == "Income"
                Text(
                    text = if (isIncome) "+$${String.format("%.2f", expense.amount)}" else "-$${String.format("%.2f", expense.amount)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isIncome) Color(0xFF34D399) else Color(0xFFFCA5A5)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Inline Action Panel (Edit & Delete options)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "✏️ EDIT",
                    color = Color(0xFF818CF8), // Sky Blue Action Link
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable { onEdit() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "🗑️ DELETE",
                    color = Color(0xFFF87171), // Soft Red Action Link
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable { onDelete() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
fun DataHubScreen(
    expenses: List<Expense>,
    viewModel: ExpenseViewModel,
    context: android.content.Context,
    onBack: () -> Unit
) {
    var backupTextToRestore by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Card 1: Export Data Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White.copy(alpha = 0.08f))
                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(24.dp))
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "📤 BACKUP & SHARE DATA",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF818CF8),
                    letterSpacing = 1.2.sp
                )
                
                Text(
                    text = "Share your entire financial ledger with other apps (Notes, Email, WhatsApp, Drive). This generates a secure CSV backup file that can be fully restored in this app anytime.",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    lineHeight = 18.sp
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Button(
                    onClick = {
                        // 1. Generate CSV String
                        val csvBuilder = java.lang.StringBuilder()
                        csvBuilder.append("Date,Description,Category,Amount,Type\n")
                        expenses.forEach { exp ->
                            val cleanName = exp.name.replace("\"", "\"\"")
                            val cleanCat = exp.category.replace("\"", "\"\"")
                            csvBuilder.append("\"${exp.date}\",\"$cleanName\",\"$cleanCat\",${exp.amount},\"${exp.type}\"\n")
                        }
                        val csvText = csvBuilder.toString()
                        
                        // 2. Fire Intent
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "Antigravity Finances Backup")
                            putExtra(Intent.EXTRA_TEXT, csvText)
                        }
                        context.startActivity(Intent.createChooser(intent, "Share Financial Backup"))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("📤 Share / Export Backup CSV", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        // Card 2: Sample Data Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White.copy(alpha = 0.08f))
                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(24.dp))
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "✨ PRE-POPULATE SAMPLE DATA",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF34D399),
                    letterSpacing = 1.2.sp
                )
                
                Text(
                    text = "Instantly load mock transactions (both income and expenses) to explore the premium interactive dashboard breakdown, category filtering, and timeline ledger.",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    lineHeight = 18.sp
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Button(
                    onClick = {
                        viewModel.pushSampleData()
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("✨ Load Premium Mock Data", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        // Card 3: Restore Data Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White.copy(alpha = 0.08f))
                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(24.dp))
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "📥 RESTORE BACKUP DATA",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF34D399),
                    letterSpacing = 1.2.sp
                )
                
                Text(
                    text = "Paste the CSV backup text exported from this app in the text field below to restore your transaction records.",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    lineHeight = 18.sp
                )
                
                OutlinedTextField(
                    value = backupTextToRestore,
                    onValueChange = { backupTextToRestore = it },
                    placeholder = { 
                        Text(
                            "Paste CSV backup text here...\ne.g. \"05/26/2026\",\"Uber\",\"Transportation\",15.13,\"Expense\"", 
                            color = Color.White.copy(alpha = 0.3f),
                            fontSize = 11.sp
                        ) 
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    maxLines = 5,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF34D399),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                        focusedContainerColor = Color.White.copy(alpha = 0.03f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.02f)
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp)
                )
                
                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Option A: Append
                    Button(
                        onClick = {
                            if (backupTextToRestore.isNotBlank()) {
                                viewModel.importRawCsvText(backupTextToRestore)
                                backupTextToRestore = ""
                                onBack()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f),
                        enabled = backupTextToRestore.isNotBlank()
                    ) {
                        Text("Append", fontWeight = FontWeight.Bold)
                    }

                    // Option B: Restore and Replace
                    Button(
                        onClick = {
                            if (backupTextToRestore.isNotBlank()) {
                                viewModel.restoreBackupCsvText(backupTextToRestore)
                                backupTextToRestore = ""
                                onBack()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1.2f),
                        enabled = backupTextToRestore.isNotBlank()
                    ) {
                        Text("Restore & Replace", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        // Card 3: Danger Zone / Clear Data Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White.copy(alpha = 0.04f))
                .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "🚨 DANGER ZONE",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFEF4444),
                    letterSpacing = 1.2.sp
                )
                
                Text(
                    text = "Permanently erase all transaction records from this device. This operation is irreversible unless you have a CSV backup.",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.5f),
                    lineHeight = 18.sp
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                OutlinedButton(
                    onClick = { 
                        viewModel.clearExpenses()
                        onBack()
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFFCA5A5),
                        containerColor = Color.Transparent
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f))
                ) {
                    Text("🗑️ Clear All Dashboard Data", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun SearchScreen(
    expenses: List<Expense>,
    viewModel: ExpenseViewModel,
    onBack: () -> Unit,
    onEditExpense: (Expense) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filtered = remember(expenses, searchQuery) {
        if (searchQuery.isBlank()) {
            emptyList()
        } else {
            expenses.filter { exp ->
                exp.name.contains(searchQuery, ignoreCase = true) ||
                exp.category.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Search Input Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White.copy(alpha = 0.08f))
                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(24.dp))
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "🔍 SEARCH TRANSACTIONS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF818CF8),
                    letterSpacing = 1.2.sp
                )

                Text(
                    text = "Search for any transactions containing name keywords, description details, or category terms.",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    lineHeight = 18.sp
                )

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            "Type name or category to search...",
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 13.sp
                        )
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF818CF8),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                        focusedContainerColor = Color.White.copy(alpha = 0.03f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.02f)
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Results Timeline Header
        Text(
            text = if (searchQuery.isBlank()) "TIMELINE SEARCH" else "SEARCH RESULTS (${filtered.size})",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.6f),
            letterSpacing = 1.5.sp,
            modifier = Modifier.align(Alignment.Start)
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (searchQuery.isBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Enter a search term above to filter and display matching transactions.",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.4f),
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }
            } else if (filtered.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No transactions found matching \"${searchQuery}\".\nTry searching for different keywords.",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.4f),
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }
            } else {
                filtered.forEach { exp ->
                    ExpenseItemRow(
                        expense = exp,
                        onEdit = { onEditExpense(exp) },
                        onDelete = { viewModel.deleteExpense(exp) }
                    )
                }
            }
        }
    }
}
