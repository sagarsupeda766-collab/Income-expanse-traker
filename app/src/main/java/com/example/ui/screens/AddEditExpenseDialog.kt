package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.domain.model.Category
import com.example.domain.model.Expense
import com.example.domain.model.PaymentMethod
import com.example.domain.model.TransactionType
import com.example.ui.components.CategoryIconBadge
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditExpenseDialog(
    initialExpense: Expense? = null,
    categories: List<Category>,
    currencySymbol: String,
    initialType: TransactionType = initialExpense?.type ?: TransactionType.EXPENSE,
    onDismiss: () -> Unit,
    onSave: (
        amount: Double,
        category: Category,
        title: String,
        dateIso: String,
        paymentMethod: PaymentMethod,
        notes: String,
        type: TransactionType
    ) -> Unit
) {
    var selectedType by remember {
        mutableStateOf(initialExpense?.type ?: initialType)
    }

    val filteredCategories = remember(categories, selectedType) {
        val matching = categories.filter { it.type == selectedType }
        if (matching.isNotEmpty()) matching else categories
    }

    var amountText by remember {
        mutableStateOf(if (initialExpense != null) initialExpense.amount.toString().removeSuffix(".0") else "")
    }
    var title by remember { mutableStateOf(initialExpense?.title ?: "") }
    var selectedCategory by remember {
        mutableStateOf(
            if (initialExpense != null) {
                categories.find { it.id == initialExpense.categoryId } ?: filteredCategories.firstOrNull()
            } else {
                filteredCategories.firstOrNull()
            }
        )
    }
    var dateIso by remember {
        mutableStateOf(initialExpense?.dateIso ?: DateUtils.todayIso())
    }
    var selectedPaymentMethod by remember {
        mutableStateOf(initialExpense?.paymentMethod ?: PaymentMethod.UPI)
    }
    var notes by remember { mutableStateOf(initialExpense?.notes ?: "") }
    var showMoreOptions by remember { mutableStateOf(notes.isNotBlank()) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = DateUtils.isoToMillis(dateIso)
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            dateIso = DateUtils.millisToIso(millis)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .heightIn(max = 700.dp)
                .testTag("add_expense_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initialExpense != null) {
                            "Edit ${if (selectedType == TransactionType.INCOME) "Income" else "Expense"}"
                        } else {
                            "Add ${if (selectedType == TransactionType.INCOME) "Income" else "Expense"}"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Transaction Type Selector (Segmented Tabs: Expense / Income)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Expense Tab
                        val isExpenseSelected = selectedType == TransactionType.EXPENSE
                        Surface(
                            onClick = {
                                selectedType = TransactionType.EXPENSE
                                val matching = categories.filter { it.type == TransactionType.EXPENSE }
                                if (selectedCategory?.type != TransactionType.EXPENSE) {
                                    selectedCategory = matching.firstOrNull()
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isExpenseSelected) ExpenseRed else androidx.compose.ui.graphics.Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("type_tab_expense")
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "↑ Expense",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (isExpenseSelected) androidx.compose.ui.graphics.Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Income Tab
                        val isIncomeSelected = selectedType == TransactionType.INCOME
                        Surface(
                            onClick = {
                                selectedType = TransactionType.INCOME
                                val matching = categories.filter { it.type == TransactionType.INCOME }
                                if (selectedCategory?.type != TransactionType.INCOME) {
                                    selectedCategory = matching.firstOrNull()
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isIncomeSelected) IncomeGreen else androidx.compose.ui.graphics.Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("type_tab_income")
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "↓ Income",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (isIncomeSelected) androidx.compose.ui.graphics.Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Amount (Big, clean numeric entry)
                val activeAccentColor = if (selectedType == TransactionType.INCOME) IncomeGreen else MaterialTheme.colorScheme.primary

                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        if (it.isEmpty() || it.matches(Regex("""^\d*\.?\d{0,2}$"""))) {
                            amountText = it
                            errorMessage = null
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("expense_amount_input"),
                    placeholder = { Text("0.00", fontSize = 24.sp, fontWeight = FontWeight.Bold) },
                    leadingIcon = {
                        Text(
                            text = currencySymbol,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = activeAccentColor,
                            modifier = Modifier.padding(start = 12.dp, end = 4.dp)
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Title / Description
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("expense_title_input"),
                    placeholder = {
                        Text(
                            if (selectedType == TransactionType.INCOME)
                                "Income source / description (e.g. Salary, Client Payout)"
                            else
                                "What did you spend on? (e.g. Lunch, Petrol)"
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Category Selection: Direct 1-Tap Chips
                Text(
                    text = "${if (selectedType == TransactionType.INCOME) "Income" else "Expense"} Category",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    filteredCategories.forEach { cat ->
                        val isSelected = selectedCategory?.id == cat.id
                        Surface(
                            onClick = { selectedCategory = cat },
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) activeAccentColor.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, activeAccentColor) else null,
                            modifier = Modifier.testTag("cat_chip_${cat.name.lowercase()}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CategoryIconBadge(
                                    iconName = cat.iconName,
                                    colorHex = cat.colorHex,
                                    size = 24.dp,
                                    iconSize = 14.dp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = cat.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) activeAccentColor else MaterialTheme.colorScheme.onSurface,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Date Chips (Today, Yesterday, Pick Date)
                Text(
                    text = "Date",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val todayIso = DateUtils.todayIso()
                    val yesterdayIso = DateUtils.today().minusDays(1).toString()

                    FilterChip(
                        selected = dateIso == todayIso,
                        onClick = { dateIso = todayIso },
                        label = { Text("Today") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = activeAccentColor.copy(alpha = 0.2f),
                            selectedLabelColor = activeAccentColor
                        )
                    )

                    FilterChip(
                        selected = dateIso == yesterdayIso,
                        onClick = { dateIso = yesterdayIso },
                        label = { Text("Yesterday") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = activeAccentColor.copy(alpha = 0.2f),
                            selectedLabelColor = activeAccentColor
                        )
                    )

                    FilterChip(
                        selected = dateIso != todayIso && dateIso != yesterdayIso,
                        onClick = { showDatePicker = true },
                        leadingIcon = {
                            Icon(
                                Icons.Default.CalendarMonth,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        label = {
                            Text(
                                if (dateIso != todayIso && dateIso != yesterdayIso)
                                    DateUtils.formatIsoToDisplay(dateIso)
                                else "Pick Date"
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = activeAccentColor.copy(alpha = 0.2f),
                            selectedLabelColor = activeAccentColor
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Payment / Deposit Method
                Text(
                    text = if (selectedType == TransactionType.INCOME) "Deposit / Account Method" else "Payment Method",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PaymentMethod.entries.forEach { method ->
                        val isSelected = selectedPaymentMethod == method
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedPaymentMethod = method },
                            label = { Text(method.displayName) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        )
                    }
                }

                if (!showMoreOptions) {
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = { showMoreOptions = true },
                        modifier = Modifier.align(Alignment.Start)
                    ) {
                        Text("+ Add Notes (Optional)", fontSize = 13.sp)
                    }
                } else {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("expense_notes_input"),
                        placeholder = { Text("Add notes, payer/payee name...") },
                        maxLines = 2,
                        shape = RoundedCornerShape(14.dp)
                    )
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Save Action Button
                Button(
                    onClick = {
                        val amount = amountText.toDoubleOrNull()
                        if (amount == null || amount <= 0.0) {
                            errorMessage = "Please enter a valid amount."
                            return@Button
                        }
                        val cat = selectedCategory ?: filteredCategories.firstOrNull()
                        if (cat == null) {
                            errorMessage = "Please select a category."
                            return@Button
                        }

                        onSave(
                            amount,
                            cat,
                            title.trim(),
                            dateIso,
                            selectedPaymentMethod,
                            notes.trim(),
                            selectedType
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("save_expense_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedType == TransactionType.INCOME) IncomeGreen else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = if (initialExpense != null) {
                            "Update ${if (selectedType == TransactionType.INCOME) "Income" else "Expense"}"
                        } else {
                            "Save ${if (selectedType == TransactionType.INCOME) "Income" else "Expense"}"
                        },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
