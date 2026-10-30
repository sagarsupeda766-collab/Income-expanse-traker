package com.example

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.Expense
import com.example.domain.model.TransactionType
import com.example.ui.screens.AddEditExpenseDialog
import com.example.ui.screens.BudgetScreen
import com.example.ui.screens.CategoriesScreen
import com.example.ui.screens.CategoryDetailScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TransactionsScreen
import com.example.ui.theme.HisabProTheme
import com.example.ui.viewmodel.ExpenseViewModel
import com.example.util.CurrencyFormatter
import kotlinx.coroutines.launch

sealed class AppScreen {
    object Dashboard : AppScreen()
    object Transactions : AppScreen()
    object Categories : AppScreen()
    object Reports : AppScreen()
    object Budget : AppScreen()
    object Settings : AppScreen()
    data class CategoryDetail(val categoryId: String) : AppScreen()
}

enum class NavigationItem(val screen: AppScreen, val title: String, val icon: ImageVector) {
    DASHBOARD(AppScreen.Dashboard, "Dashboard", Icons.Default.Dashboard),
    TRANSACTIONS(AppScreen.Transactions, "History", Icons.Default.ReceiptLong),
    CATEGORIES(AppScreen.Categories, "Categories", Icons.Default.Category),
    REPORTS(AppScreen.Reports, "Reports", Icons.Default.BarChart),
    BUDGET(AppScreen.Budget, "Budget", Icons.Default.BarChart)
}

@Composable
fun HisabProApp(viewModel: ExpenseViewModel) {
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val isDark = when (themeMode) {
        "LIGHT" -> false
        "DARK" -> true
        else -> isSystemInDarkTheme()
    }

    HisabProTheme(darkTheme = isDark) {
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()
        val snackbarHostState = remember { SnackbarHostState() }

        var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Dashboard) }

        // Dialog states
        var expenseToEdit by remember { mutableStateOf<Expense?>(null) }
        var isAddExpenseOpen by remember { mutableStateOf(false) }
        var addDialogInitialType by remember { mutableStateOf(TransactionType.EXPENSE) }
        var expenseToDelete by remember { mutableStateOf<Expense?>(null) }

        val allCategories by viewModel.allCategories.collectAsStateWithLifecycle()
        val currencySymbol by viewModel.currencySymbol.collectAsStateWithLifecycle()

        // Handle System Back button
        BackHandler(enabled = currentScreen != AppScreen.Dashboard) {
            when (currentScreen) {
                is AppScreen.CategoryDetail -> currentScreen = AppScreen.Categories
                AppScreen.Settings -> currentScreen = AppScreen.Dashboard
                AppScreen.Budget -> currentScreen = AppScreen.Dashboard
                AppScreen.Reports -> currentScreen = AppScreen.Dashboard
                AppScreen.Categories -> currentScreen = AppScreen.Dashboard
                AppScreen.Transactions -> currentScreen = AppScreen.Dashboard
                else -> {}
            }
        }

        // Add / Edit Expense Dialog
        if (isAddExpenseOpen || expenseToEdit != null) {
            AddEditExpenseDialog(
                initialExpense = expenseToEdit,
                categories = allCategories,
                currencySymbol = currencySymbol,
                initialType = expenseToEdit?.type ?: addDialogInitialType,
                onDismiss = {
                    isAddExpenseOpen = false
                    expenseToEdit = null
                },
                onSave = { amount, category, title, dateIso, paymentMethod, notes, type ->
                    if (expenseToEdit != null) {
                        viewModel.updateExpense(
                            expense = expenseToEdit!!,
                            amount = amount,
                            category = category,
                            title = title,
                            dateIso = dateIso,
                            paymentMethod = paymentMethod,
                            notes = notes,
                            type = type,
                            onSuccess = {
                                val label = if (type == TransactionType.INCOME) "Income" else "Expense"
                                Toast.makeText(context, "$label updated successfully", Toast.LENGTH_SHORT).show()
                            }
                        )
                    } else {
                        viewModel.addExpense(
                            amount = amount,
                            category = category,
                            title = title,
                            dateIso = dateIso,
                            paymentMethod = paymentMethod,
                            notes = notes,
                            type = type,
                            onSuccess = {
                                val label = if (type == TransactionType.INCOME) "Income" else "Expense"
                                Toast.makeText(context, "$label recorded successfully", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                    isAddExpenseOpen = false
                    expenseToEdit = null
                }
            )
        }

        // Delete Confirmation Dialog
        if (expenseToDelete != null) {
            val exp = expenseToDelete!!
            AlertDialog(
                onDismissRequest = { expenseToDelete = null },
                title = { Text("Delete Expense?") },
                text = {
                    Text(
                        "Are you sure you want to delete \"${exp.title}\" (${CurrencyFormatter.format(exp.amount, currencySymbol)})?"
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteExpense(exp) {
                                Toast.makeText(context, "Expense deleted", Toast.LENGTH_SHORT).show()
                            }
                            expenseToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { expenseToDelete = null }) {
                        Text("Cancel")
                    }
                }
            )
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                // Only show BottomBar on top-level screens
                val showBottomNav = currentScreen !is AppScreen.CategoryDetail && currentScreen != AppScreen.Settings
                if (showBottomNav) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 6.dp
                    ) {
                        val navItems = listOf(
                            Triple("Home", Icons.Default.Dashboard, AppScreen.Dashboard),
                            Triple("History", Icons.Default.ReceiptLong, AppScreen.Transactions),
                            Triple("Analytics", Icons.Default.BarChart, AppScreen.Reports),
                            Triple("Categories", Icons.Default.Category, AppScreen.Categories)
                        )

                        navItems.forEach { (label, icon, screen) ->
                            val isSelected = currentScreen == screen
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = {
                                    currentScreen = screen
                                },
                                icon = {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = label,
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = label,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 11.sp
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                modifier = Modifier.testTag("nav_item_${label.lowercase()}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "screen_transition"
                ) { screen ->
                    when (screen) {
                        is AppScreen.Dashboard -> DashboardScreen(
                            viewModel = viewModel,
                            onNavigateToTransactions = { currentScreen = AppScreen.Transactions },
                            onNavigateToCategories = { currentScreen = AppScreen.Categories },
                            onNavigateToCategoryDetail = { catId -> currentScreen = AppScreen.CategoryDetail(catId) },
                            onNavigateToBudget = { currentScreen = AppScreen.Budget },
                            onNavigateToSettings = { currentScreen = AppScreen.Settings },
                            onAddExpenseClick = {
                                addDialogInitialType = TransactionType.EXPENSE
                                isAddExpenseOpen = true
                            },
                            onAddIncomeClick = {
                                addDialogInitialType = TransactionType.INCOME
                                isAddExpenseOpen = true
                            },
                            onEditExpense = { exp ->
                                addDialogInitialType = exp.type
                                expenseToEdit = exp
                            },
                            onDuplicateExpense = { exp ->
                                viewModel.duplicateExpense(exp) {
                                    Toast.makeText(context, "Duplicated to today!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onDeleteExpense = { exp -> expenseToDelete = exp }
                        )

                        is AppScreen.Transactions -> TransactionsScreen(
                            viewModel = viewModel,
                            onAddExpenseClick = { isAddExpenseOpen = true },
                            onEditExpense = { exp -> expenseToEdit = exp },
                            onDuplicateExpense = { exp ->
                                viewModel.duplicateExpense(exp) {
                                    Toast.makeText(context, "Duplicated to today!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onDeleteExpense = { exp -> expenseToDelete = exp }
                        )

                        is AppScreen.Categories -> CategoriesScreen(
                            viewModel = viewModel,
                            onCategoryClick = { catId -> currentScreen = AppScreen.CategoryDetail(catId) }
                        )

                        is AppScreen.Reports -> ReportsScreen(
                            viewModel = viewModel,
                            onCategoryClick = { catId -> currentScreen = AppScreen.CategoryDetail(catId) }
                        )

                        is AppScreen.Budget -> BudgetScreen(
                            viewModel = viewModel,
                            onCategoryClick = { catId -> currentScreen = AppScreen.CategoryDetail(catId) }
                        )

                        is AppScreen.Settings -> SettingsScreen(
                            viewModel = viewModel,
                            onBack = { currentScreen = AppScreen.Dashboard }
                        )

                        is AppScreen.CategoryDetail -> CategoryDetailScreen(
                            categoryId = screen.categoryId,
                            viewModel = viewModel,
                            onBack = { currentScreen = AppScreen.Categories },
                            onAddExpense = { isAddExpenseOpen = true },
                            onEditExpense = { exp -> expenseToEdit = exp },
                            onDuplicateExpense = { exp ->
                                viewModel.duplicateExpense(exp) {
                                    Toast.makeText(context, "Duplicated to today!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onDeleteExpense = { exp -> expenseToDelete = exp }
                        )
                    }
                }
            }
        }
    }
}
