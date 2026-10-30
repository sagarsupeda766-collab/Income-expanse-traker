package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.Category
import com.example.ui.components.CategoryIconBadge
import com.example.ui.components.CategoryIconHelper
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.WarningAmber
import com.example.ui.viewmodel.ExpenseViewModel
import com.example.util.CurrencyFormatter
import com.example.util.DateUtils

@Composable
fun BudgetScreen(
    viewModel: ExpenseViewModel,
    onCategoryClick: (String) -> Unit
) {
    val monthlyBudget by viewModel.monthlyBudget.collectAsStateWithLifecycle()
    val allExpenses by viewModel.allExpenses.collectAsStateWithLifecycle()
    val allCategories by viewModel.allCategories.collectAsStateWithLifecycle()
    val currencySymbol by viewModel.currencySymbol.collectAsStateWithLifecycle()
    val selectedYm by viewModel.selectedYearMonth.collectAsStateWithLifecycle()

    var showEditOverallBudgetDialog by remember { mutableStateOf(false) }
    var editingCategoryBudget by remember { mutableStateOf<Category?>(null) }

    val monthPrefix = remember(selectedYm) { DateUtils.formatYearMonthKey(selectedYm) }
    val monthExpenses = remember(allExpenses, monthPrefix) {
        allExpenses.filter { it.dateIso.startsWith(monthPrefix) }
    }
    val totalSpent = remember(monthExpenses) { monthExpenses.sumOf { it.amount } }
    val remainingBudget = monthlyBudget - totalSpent
    val overallProgress = if (monthlyBudget > 0) (totalSpent / monthlyBudget).toFloat().coerceIn(0f, 1f) else 0f
    val overallPercent = if (monthlyBudget > 0) (totalSpent / monthlyBudget) * 100.0 else 0.0

    // Edit Overall Budget Dialog
    if (showEditOverallBudgetDialog) {
        var budgetInput by remember { mutableStateOf(monthlyBudget.toString().removeSuffix(".0")) }
        Dialog(onDismissRequest = { showEditOverallBudgetDialog = false }) {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Set Monthly Budget",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = budgetInput,
                        onValueChange = { budgetInput = it },
                        label = { Text("Total Monthly Budget ($currencySymbol)") },
                        placeholder = { Text("e.g., 35000") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("overall_budget_input")
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showEditOverallBudgetDialog = false }) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                val value = budgetInput.toDoubleOrNull() ?: 35000.0
                                viewModel.setMonthlyBudget(value)
                                showEditOverallBudgetDialog = false
                            },
                            modifier = Modifier.testTag("save_overall_budget_button")
                        ) {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }

    // Edit Category Budget Dialog
    if (editingCategoryBudget != null) {
        val cat = editingCategoryBudget!!
        var catBudgetInput by remember { mutableStateOf(cat.monthlyBudget.toString().removeSuffix(".0")) }
        Dialog(onDismissRequest = { editingCategoryBudget = null }) {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Set Budget for ${cat.name}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = catBudgetInput,
                        onValueChange = { catBudgetInput = it },
                        label = { Text("Category Budget ($currencySymbol)") },
                        placeholder = { Text("e.g. 5000") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { editingCategoryBudget = null }) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                val amount = catBudgetInput.toDoubleOrNull() ?: 0.0
                                viewModel.updateCategoryBudget(cat.id, amount)
                                editingCategoryBudget = null
                            }
                        ) {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Budget Management",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Target spending for ${DateUtils.formatMonthYear(selectedYm)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = { showEditOverallBudgetDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("edit_budget_btn")
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit Budget")
                }
            }
        }

        // Overall Budget Card
        item {
            val statusColor = when {
                totalSpent > monthlyBudget -> ExpenseRed
                overallPercent >= 80.0 -> WarningAmber
                else -> IncomeGreen
            }

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Monthly Total Budget",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = CurrencyFormatter.format(monthlyBudget, currencySymbol),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    LinearProgressIndicator(
                        progress = { overallProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = statusColor,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        strokeCap = StrokeCap.Round
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Spent",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = CurrencyFormatter.format(totalSpent, currencySymbol),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Utilization",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${String.format("%.1f", overallPercent)}%",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = if (remainingBudget >= 0) "Remaining" else "Deficit",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = CurrencyFormatter.format(remainingBudget, currencySymbol),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            )
                        }
                    }

                    // Alerts
                    if (totalSpent > monthlyBudget) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(ExpenseRed.copy(alpha = 0.12f))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = ExpenseRed, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "CRITICAL: You have exceeded your monthly budget by ${CurrencyFormatter.format(totalSpent - monthlyBudget, currencySymbol)}!",
                                style = MaterialTheme.typography.bodySmall,
                                color = ExpenseRed,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else if (overallPercent >= 80.0) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(WarningAmber.copy(alpha = 0.12f))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.WarningAmber, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "WARNING: You have reached 80% of your total budget limit.",
                                style = MaterialTheme.typography.bodySmall,
                                color = WarningAmber,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Category Budgets Header
        item {
            Text(
                text = "Individual Category Budgets",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        // Category Budget Items
        items(allCategories, key = { it.id }) { cat ->
            val catExpenses = monthExpenses.filter { it.categoryId == cat.id }
            val catSpent = catExpenses.sumOf { it.amount }
            val hasBudget = cat.monthlyBudget > 0
            val catProgress = if (hasBudget) (catSpent / cat.monthlyBudget).toFloat().coerceIn(0f, 1f) else 0f
            val catPercent = if (hasBudget) (catSpent / cat.monthlyBudget) * 100.0 else 0.0

            val catColor = CategoryIconHelper.parseColor(cat.colorHex)
            val isExceeded = hasBudget && catSpent > cat.monthlyBudget
            val isNearLimit = hasBudget && catPercent >= 80.0 && !isExceeded

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { editingCategoryBudget = cat },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CategoryIconBadge(
                            iconName = cat.iconName,
                            colorHex = cat.colorHex,
                            size = 38.dp,
                            iconSize = 20.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = cat.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (hasBudget) {
                                    "Spent: ${CurrencyFormatter.format(catSpent, currencySymbol)} of ${CurrencyFormatter.format(cat.monthlyBudget, currencySymbol)}"
                                } else {
                                    "Spent: ${CurrencyFormatter.format(catSpent, currencySymbol)} (No budget set)"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }

                        IconButton(
                            onClick = { editingCategoryBudget = cat },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Category Budget", modifier = Modifier.size(16.dp))
                        }
                    }

                    if (hasBudget) {
                        Spacer(modifier = Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = { catProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = if (isExceeded) ExpenseRed else if (isNearLimit) WarningAmber else catColor,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            strokeCap = StrokeCap.Round
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${String.format("%.0f", catPercent)}% spent",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isExceeded) ExpenseRed else if (isNearLimit) WarningAmber else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = if (isExceeded || isNearLimit) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 10.sp
                            )
                            Text(
                                text = if (catSpent <= cat.monthlyBudget)
                                    "Remaining: ${CurrencyFormatter.format(cat.monthlyBudget - catSpent, currencySymbol)}"
                                else
                                    "Exceeded by ${CurrencyFormatter.format(catSpent - cat.monthlyBudget, currencySymbol)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isExceeded) ExpenseRed else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
