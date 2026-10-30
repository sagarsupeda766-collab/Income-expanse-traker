package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ExpenseRepository
import com.example.domain.model.Category
import com.example.domain.model.CategorySummary
import com.example.domain.model.DailySpendPoint
import com.example.domain.model.DateFilterType
import com.example.domain.model.Expense
import com.example.domain.model.MonthComparisonData
import com.example.domain.model.PaymentMethod
import com.example.domain.model.SortOrder
import com.example.domain.model.TransactionType
import com.example.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID

data class DashboardState(
    val todayTotal: Double = 0.0, // Today's Expense
    val todayIncome: Double = 0.0,
    val weekTotal: Double = 0.0,
    val monthTotal: Double = 0.0, // Month's Expense
    val monthExpense: Double = 0.0,
    val monthIncome: Double = 0.0,
    val netBalance: Double = 0.0, // monthIncome - monthExpense
    val selectedPeriodTotal: Double = 0.0,
    val totalTransactionsCount: Int = 0,
    val remainingBudget: Double = 0.0,
    val highestCategory: CategorySummary? = null,
    val categoryBreakdown: List<CategorySummary> = emptyList(), // Expense breakdown
    val incomeBreakdown: List<CategorySummary> = emptyList(), // Income breakdown
    val dailySpending: List<DailySpendPoint> = emptyList(),
    val recentTransactions: List<Expense> = emptyList(),
    val currentBudget: Double = 35000.0
)

data class MonthlyReportState(
    val selectedMonth: YearMonth = DateUtils.currentYearMonth(),
    val totalSpent: Double = 0.0,
    val totalIncome: Double = 0.0,
    val netSavings: Double = 0.0,
    val averageDailySpend: Double = 0.0,
    val highestSpendingDay: DailySpendPoint? = null,
    val lowestSpendingDay: DailySpendPoint? = null,
    val totalTransactions: Int = 0,
    val mostExpensiveCategory: CategorySummary? = null,
    val categorySummaries: List<CategorySummary> = emptyList(),
    val incomeSummaries: List<CategorySummary> = emptyList(),
    val dailyTrend: List<DailySpendPoint> = emptyList(),
    val comparison: MonthComparisonData? = null
)

class ExpenseViewModel(
    private val repository: ExpenseRepository
) : ViewModel() {

    val allExpenses: StateFlow<List<Expense>> = repository.allExpensesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCategories: StateFlow<List<Category>> = repository.allCategoriesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val monthlyBudget: StateFlow<Double> = repository.preferencesManager.monthlyBudget
    val currencySymbol: StateFlow<String> = repository.preferencesManager.currencySymbol
    val themeMode: StateFlow<String> = repository.preferencesManager.themeMode

    // Navigation and Period Selection
    private val _selectedYearMonth = MutableStateFlow(DateUtils.currentYearMonth())
    val selectedYearMonth: StateFlow<YearMonth> = _selectedYearMonth.asStateFlow()

    private val _comparisonYearMonth = MutableStateFlow(DateUtils.currentYearMonth().minusMonths(1))
    val comparisonYearMonth: StateFlow<YearMonth> = _comparisonYearMonth.asStateFlow()

    // Transaction List Filters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedDateFilter = MutableStateFlow(DateFilterType.THIS_MONTH)
    val selectedDateFilter: StateFlow<DateFilterType> = _selectedDateFilter.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow<String?>(null)
    val selectedCategoryFilter: StateFlow<String?> = _selectedCategoryFilter.asStateFlow()

    private val _selectedPaymentFilter = MutableStateFlow<PaymentMethod?>(null)
    val selectedPaymentFilter: StateFlow<PaymentMethod?> = _selectedPaymentFilter.asStateFlow()

    private val _selectedTransactionTypeFilter = MutableStateFlow<TransactionType?>(null)
    val selectedTransactionTypeFilter: StateFlow<TransactionType?> = _selectedTransactionTypeFilter.asStateFlow()

    private val _selectedSortOrder = MutableStateFlow(SortOrder.NEWEST_FIRST)
    val selectedSortOrder: StateFlow<SortOrder> = _selectedSortOrder.asStateFlow()

    private val _customDateRange = MutableStateFlow<Pair<String, String>?>(null)
    val customDateRange: StateFlow<Pair<String, String>?> = _customDateRange.asStateFlow()

    init {
        viewModelScope.launch {
            repository.checkAndSeedDefaults()
            if (!repository.preferencesManager.hasSeededSampleData()) {
                repository.seedSampleTransactions()
            }
        }
    }

    // Filtered Transactions Flow
    val filteredExpenses: StateFlow<List<Expense>> = combine(
        allExpenses,
        _searchQuery,
        _selectedDateFilter,
        _selectedCategoryFilter,
        _selectedPaymentFilter,
        _selectedTransactionTypeFilter,
        _selectedSortOrder,
        _customDateRange
    ) { args ->
        val expenses = args[0] as List<Expense>
        val query = (args[1] as String).trim().lowercase()
        val dateFilter = args[2] as DateFilterType
        val categoryId = args[3] as String?
        val paymentMethod = args[4] as PaymentMethod?
        val typeFilter = args[5] as TransactionType?
        val sortOrder = args[6] as SortOrder
        val customRange = args[7] as Pair<String, String>?

        val today = DateUtils.today()
        val currentMonthPrefix = DateUtils.formatToYearMonthKey(today)
        val prevMonthPrefix = DateUtils.formatToYearMonthKey(today.minusMonths(1))

        var list = expenses.filter { exp ->
            // Date filter check
            val dateMatch = when (dateFilter) {
                DateFilterType.TODAY -> exp.dateIso == today.toString()
                DateFilterType.YESTERDAY -> exp.dateIso == today.minusDays(1).toString()
                DateFilterType.LAST_7_DAYS -> {
                    val expDate = DateUtils.parseIso(exp.dateIso)
                    !expDate.isBefore(today.minusDays(6)) && !expDate.isAfter(today)
                }
                DateFilterType.THIS_MONTH -> exp.dateIso.startsWith(currentMonthPrefix)
                DateFilterType.PREVIOUS_MONTH -> exp.dateIso.startsWith(prevMonthPrefix)
                DateFilterType.CUSTOM_RANGE -> {
                    if (customRange != null) {
                        exp.dateIso >= customRange.first && exp.dateIso <= customRange.second
                    } else true
                }
                DateFilterType.ALL_TIME -> true
            }

            // Category filter
            val catMatch = categoryId == null || exp.categoryId == categoryId

            // Payment method filter
            val payMatch = paymentMethod == null || exp.paymentMethod == paymentMethod

            // Transaction Type filter (Income vs Expense)
            val typeMatch = typeFilter == null || exp.type == typeFilter

            // Query match (title, category, notes, amount)
            val textMatch = if (query.isBlank()) true else {
                exp.title.lowercase().contains(query) ||
                        exp.categoryName.lowercase().contains(query) ||
                        exp.notes.lowercase().contains(query) ||
                        exp.amount.toString().contains(query) ||
                        exp.dateIso.contains(query)
            }

            dateMatch && catMatch && payMatch && typeMatch && textMatch
        }

        // Apply sorting
        list = when (sortOrder) {
            SortOrder.NEWEST_FIRST -> list.sortedWith(compareByDescending<Expense> { it.dateIso }.thenByDescending { it.createdAt })
            SortOrder.OLDEST_FIRST -> list.sortedWith(compareBy<Expense> { it.dateIso }.thenBy { it.createdAt })
            SortOrder.HIGHEST_AMOUNT -> list.sortedByDescending { it.amount }
            SortOrder.LOWEST_AMOUNT -> list.sortedBy { it.amount }
        }

        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dashboard State (computes both Income & Expense)
    val dashboardState: StateFlow<DashboardState> = combine(
        allExpenses,
        allCategories,
        _selectedYearMonth,
        monthlyBudget
    ) { expenses, categories, selectedYm, budget ->
        val today = DateUtils.today()
        val todayIso = today.toString()
        val (weekStart, weekEnd) = DateUtils.getStartAndEndOfWeek(today)
        val selectedMonthPrefix = DateUtils.formatYearMonthKey(selectedYm)

        var todayExpenseSum = 0.0
        var todayIncomeSum = 0.0
        var weekExpenseSum = 0.0
        var monthExpenseSum = 0.0
        var monthIncomeSum = 0.0

        val monthExpenses = mutableListOf<Expense>()
        val monthIncomes = mutableListOf<Expense>()

        for (e in expenses) {
            val isExpense = e.type == TransactionType.EXPENSE
            val isIncome = e.type == TransactionType.INCOME

            if (e.dateIso == todayIso) {
                if (isExpense) todayExpenseSum += e.amount
                if (isIncome) todayIncomeSum += e.amount
            }

            val eDate = DateUtils.parseIso(e.dateIso)
            if (!eDate.isBefore(weekStart) && !eDate.isAfter(weekEnd) && isExpense) {
                weekExpenseSum += e.amount
            }

            if (e.dateIso.startsWith(selectedMonthPrefix)) {
                if (isExpense) {
                    monthExpenseSum += e.amount
                    monthExpenses.add(e)
                } else if (isIncome) {
                    monthIncomeSum += e.amount
                    monthIncomes.add(e)
                }
            }
        }

        // Expense category breakdown
        val expenseCatMap = monthExpenses.groupBy { it.categoryId }
        val expenseSummaries = expenseCatMap.map { (catId, catList) ->
            val catTotal = catList.sumOf { it.amount }
            val catObj = categories.find { it.id == catId } ?: Category(
                id = catId,
                name = catList.firstOrNull()?.categoryName ?: "Other",
                iconName = "category",
                colorHex = "#DC2626",
                type = TransactionType.EXPENSE
            )
            CategorySummary(
                category = catObj,
                totalAmount = catTotal,
                transactionCount = catList.size,
                percentageOfTotal = if (monthExpenseSum > 0) (catTotal / monthExpenseSum) * 100.0 else 0.0
            )
        }.sortedByDescending { it.totalAmount }

        // Income category breakdown
        val incomeCatMap = monthIncomes.groupBy { it.categoryId }
        val incomeSummaries = incomeCatMap.map { (catId, catList) ->
            val catTotal = catList.sumOf { it.amount }
            val catObj = categories.find { it.id == catId } ?: Category(
                id = catId,
                name = catList.firstOrNull()?.categoryName ?: "Other Income",
                iconName = "payments",
                colorHex = "#10B981",
                type = TransactionType.INCOME
            )
            CategorySummary(
                category = catObj,
                totalAmount = catTotal,
                transactionCount = catList.size,
                percentageOfTotal = if (monthIncomeSum > 0) (catTotal / monthIncomeSum) * 100.0 else 0.0
            )
        }.sortedByDescending { it.totalAmount }

        val highestCat = expenseSummaries.firstOrNull()

        // Daily spending points for selected month
        val daysInMonth = selectedYm.lengthOfMonth()
        val dailyMap = monthExpenses.groupBy { DateUtils.parseIso(it.dateIso).dayOfMonth }
        val dailyPoints = (1..daysInMonth).map { day ->
            val dayExpenses = dailyMap[day] ?: emptyList()
            val daySum = dayExpenses.sumOf { it.amount }
            val dayDate = selectedYm.atDay(day)
            DailySpendPoint(
                dayNumber = day,
                dateIso = dayDate.toString(),
                dayName = "${dayDate.dayOfWeek.name.take(3)} $day",
                amount = daySum
            )
        }

        DashboardState(
            todayTotal = todayExpenseSum,
            todayIncome = todayIncomeSum,
            weekTotal = weekExpenseSum,
            monthTotal = monthExpenseSum,
            monthExpense = monthExpenseSum,
            monthIncome = monthIncomeSum,
            netBalance = monthIncomeSum - monthExpenseSum,
            selectedPeriodTotal = monthExpenseSum,
            totalTransactionsCount = monthExpenses.size + monthIncomes.size,
            remainingBudget = budget - monthExpenseSum,
            highestCategory = highestCat,
            categoryBreakdown = expenseSummaries,
            incomeBreakdown = incomeSummaries,
            dailySpending = dailyPoints,
            recentTransactions = expenses.take(6),
            currentBudget = budget
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardState())

    // Monthly Reports & Comparison State
    val monthlyReportState: StateFlow<MonthlyReportState> = combine(
        allExpenses,
        allCategories,
        _selectedYearMonth,
        _comparisonYearMonth
    ) { expenses, categories, selectedYm, compYm ->
        val selectedPrefix = DateUtils.formatYearMonthKey(selectedYm)
        val compPrefix = DateUtils.formatYearMonthKey(compYm)

        val month1All = expenses.filter { it.dateIso.startsWith(selectedPrefix) }
        val month2All = expenses.filter { it.dateIso.startsWith(compPrefix) }

        val month1Expenses = month1All.filter { it.type == TransactionType.EXPENSE }
        val month1Incomes = month1All.filter { it.type == TransactionType.INCOME }

        val totalSpent1 = month1Expenses.sumOf { it.amount }
        val totalIncome1 = month1Incomes.sumOf { it.amount }
        val totalSpent2 = month2All.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

        val daysInMonth = selectedYm.lengthOfMonth()
        val dailyMap = month1Expenses.groupBy { DateUtils.parseIso(it.dateIso).dayOfMonth }
        val dailyPoints = (1..daysInMonth).map { day ->
            val dayDate = selectedYm.atDay(day)
            val sum = dailyMap[day]?.sumOf { it.amount } ?: 0.0
            DailySpendPoint(
                dayNumber = day,
                dateIso = dayDate.toString(),
                dayName = "${dayDate.dayOfWeek.name.take(3)} $day",
                amount = sum
            )
        }

        val activeDays = dailyPoints.filter { it.amount > 0 }
        val highestDay = activeDays.maxByOrNull { it.amount }
        val lowestDay = activeDays.minByOrNull { it.amount }
        val avgDailySpend = if (activeDays.isNotEmpty()) totalSpent1 / activeDays.size else 0.0

        val catMap = month1Expenses.groupBy { it.categoryId }
        val catSummaries = catMap.map { (catId, catList) ->
            val sum = catList.sumOf { it.amount }
            val catObj = categories.find { it.id == catId } ?: Category(
                id = catId,
                name = catList.firstOrNull()?.categoryName ?: "Other",
                iconName = "category",
                colorHex = "#DC2626",
                type = TransactionType.EXPENSE
            )
            CategorySummary(
                category = catObj,
                totalAmount = sum,
                transactionCount = catList.size,
                percentageOfTotal = if (totalSpent1 > 0) (sum / totalSpent1) * 100.0 else 0.0
            )
        }.sortedByDescending { it.totalAmount }

        val incomeMap = month1Incomes.groupBy { it.categoryId }
        val incomeSummaries = incomeMap.map { (catId, catList) ->
            val sum = catList.sumOf { it.amount }
            val catObj = categories.find { it.id == catId } ?: Category(
                id = catId,
                name = catList.firstOrNull()?.categoryName ?: "Other Income",
                iconName = "payments",
                colorHex = "#10B981",
                type = TransactionType.INCOME
            )
            CategorySummary(
                category = catObj,
                totalAmount = sum,
                transactionCount = catList.size,
                percentageOfTotal = if (totalIncome1 > 0) (sum / totalIncome1) * 100.0 else 0.0
            )
        }.sortedByDescending { it.totalAmount }

        val diffAmount = totalSpent1 - totalSpent2
        val pctChange = if (totalSpent2 > 0) ((totalSpent1 - totalSpent2) / totalSpent2) * 100.0 else 0.0

        val compData = MonthComparisonData(
            month1Key = selectedPrefix,
            month1Label = DateUtils.formatMonthYear(selectedYm),
            month1Total = totalSpent1,
            month2Key = compPrefix,
            month2Label = DateUtils.formatMonthYear(compYm),
            month2Total = totalSpent2,
            differenceAmount = diffAmount,
            percentageChange = pctChange
        )

        MonthlyReportState(
            selectedMonth = selectedYm,
            totalSpent = totalSpent1,
            totalIncome = totalIncome1,
            netSavings = totalIncome1 - totalSpent1,
            averageDailySpend = avgDailySpend,
            highestSpendingDay = highestDay,
            lowestSpendingDay = lowestDay,
            totalTransactions = month1All.size,
            mostExpensiveCategory = catSummaries.firstOrNull(),
            categorySummaries = catSummaries,
            incomeSummaries = incomeSummaries,
            dailyTrend = dailyPoints,
            comparison = compData
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MonthlyReportState())

    // Filter controls
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setDateFilter(filter: DateFilterType) {
        _selectedDateFilter.value = filter
    }

    fun setCategoryFilter(categoryId: String?) {
        _selectedCategoryFilter.value = categoryId
    }

    fun setPaymentFilter(method: PaymentMethod?) {
        _selectedPaymentFilter.value = method
    }

    fun setTransactionTypeFilter(type: TransactionType?) {
        _selectedTransactionTypeFilter.value = type
    }

    fun setSortOrder(order: SortOrder) {
        _selectedSortOrder.value = order
    }

    fun setCustomDateRange(startIso: String, endIso: String) {
        _customDateRange.value = Pair(startIso, endIso)
        _selectedDateFilter.value = DateFilterType.CUSTOM_RANGE
    }

    fun setSelectedYearMonth(yearMonth: YearMonth) {
        _selectedYearMonth.value = yearMonth
    }

    fun setComparisonYearMonth(yearMonth: YearMonth) {
        _comparisonYearMonth.value = yearMonth
    }

    // CRUD Operations for Transactions (Income & Expense)
    fun addExpense(
        amount: Double,
        category: Category,
        title: String,
        dateIso: String,
        paymentMethod: PaymentMethod,
        notes: String = "",
        type: TransactionType = category.type,
        receiptUri: String? = null,
        onSuccess: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            val expense = Expense(
                amount = amount,
                categoryId = category.id,
                categoryName = category.name,
                title = title.ifBlank { category.name },
                dateIso = dateIso,
                paymentMethod = paymentMethod,
                type = type,
                notes = notes,
                receiptUri = receiptUri
            )
            repository.insertExpense(expense)
            onSuccess?.invoke()
        }
    }

    fun updateExpense(
        expense: Expense,
        amount: Double,
        category: Category,
        title: String,
        dateIso: String,
        paymentMethod: PaymentMethod,
        notes: String = "",
        type: TransactionType = expense.type,
        onSuccess: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            val updated = expense.copy(
                amount = amount,
                categoryId = category.id,
                categoryName = category.name,
                title = title.ifBlank { category.name },
                dateIso = dateIso,
                paymentMethod = paymentMethod,
                type = type,
                notes = notes,
                updatedAt = System.currentTimeMillis()
            )
            repository.updateExpense(updated)
            onSuccess?.invoke()
        }
    }

    fun deleteExpense(expense: Expense, onSuccess: (() -> Unit)? = null) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
            onSuccess?.invoke()
        }
    }

    fun duplicateExpense(expense: Expense, onSuccess: (() -> Unit)? = null) {
        viewModelScope.launch {
            val duplicated = expense.copy(
                id = 0,
                title = "${expense.title} (Copy)",
                dateIso = DateUtils.todayIso(),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            repository.insertExpense(duplicated)
            onSuccess?.invoke()
        }
    }

    fun addCustomCategory(
        name: String,
        iconName: String,
        colorHex: String,
        budget: Double = 0.0,
        type: TransactionType = TransactionType.EXPENSE
    ) {
        viewModelScope.launch {
            val newCategory = Category(
                id = "custom_${UUID.randomUUID().toString().take(8)}",
                name = name,
                iconName = iconName,
                colorHex = colorHex,
                monthlyBudget = budget,
                isCustom = true,
                orderIndex = 100,
                type = type
            )
            repository.insertCategory(newCategory)
        }
    }

    fun updateCategoryBudget(categoryId: String, budget: Double) {
        viewModelScope.launch {
            repository.updateCategoryBudget(categoryId, budget)
        }
    }

    fun setMonthlyBudget(amount: Double) {
        repository.preferencesManager.setMonthlyBudget(amount)
    }

    fun setCurrencySymbol(symbol: String) {
        repository.preferencesManager.setCurrencySymbol(symbol)
    }

    fun setThemeMode(mode: String) {
        repository.preferencesManager.setThemeMode(mode)
    }

    fun clearAllData(onSuccess: (() -> Unit)? = null) {
        viewModelScope.launch {
            repository.clearAllData()
            onSuccess?.invoke()
        }
    }

    fun seedSampleData(onSuccess: (() -> Unit)? = null) {
        viewModelScope.launch {
            repository.seedSampleTransactions()
            onSuccess?.invoke()
        }
    }

    suspend fun exportCsv(): String {
        return repository.exportToCsv()
    }

    suspend fun exportJson(): String {
        return repository.exportToJson()
    }

    suspend fun restoreJson(json: String): Boolean {
        return repository.restoreFromJson(json)
    }
}

class ExpenseViewModelFactory(
    private val repository: ExpenseRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExpenseViewModel::class.java)) {
            return ExpenseViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
