package com.example.domain.model

enum class TransactionType(val title: String) {
    EXPENSE("Expense"),
    INCOME("Income");

    companion object {
        fun fromString(value: String): TransactionType {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) || it.title.equals(value, ignoreCase = true) }
                ?: EXPENSE
        }
    }
}

enum class PaymentMethod(val displayName: String, val iconName: String) {
    CASH("Cash", "payments"),
    UPI("UPI", "qr_code_scanner"),
    DEBIT_CARD("Debit Card", "credit_card"),
    CREDIT_CARD("Credit Card", "credit_card"),
    BANK_TRANSFER("Bank Transfer", "account_balance"),
    OTHER("Other", "more_horiz");

    companion object {
        fun fromString(value: String): PaymentMethod {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) || it.displayName.equals(value, ignoreCase = true) }
                ?: CASH
        }
    }
}

enum class DateFilterType(val label: String) {
    TODAY("Today"),
    YESTERDAY("Yesterday"),
    LAST_7_DAYS("Last 7 Days"),
    THIS_MONTH("This Month"),
    PREVIOUS_MONTH("Previous Month"),
    CUSTOM_RANGE("Custom Range"),
    ALL_TIME("All Time")
}

enum class SortOrder(val label: String) {
    NEWEST_FIRST("Newest First"),
    OLDEST_FIRST("Oldest First"),
    HIGHEST_AMOUNT("Highest Amount"),
    LOWEST_AMOUNT("Lowest Amount")
}

data class Category(
    val id: String,
    val name: String,
    val iconName: String,
    val colorHex: String,
    val monthlyBudget: Double = 0.0,
    val isCustom: Boolean = false,
    val orderIndex: Int = 0,
    val type: TransactionType = TransactionType.EXPENSE
)

data class Expense(
    val id: Long = 0,
    val amount: Double,
    val categoryId: String,
    val categoryName: String,
    val title: String,
    val dateIso: String, // YYYY-MM-DD
    val paymentMethod: PaymentMethod,
    val notes: String = "",
    val receiptUri: String? = null,
    val type: TransactionType = TransactionType.EXPENSE,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val isIncome: Boolean get() = type == TransactionType.INCOME
    val isExpense: Boolean get() = type == TransactionType.EXPENSE
}

typealias Transaction = Expense

data class CategorySummary(
    val category: Category,
    val totalAmount: Double,
    val transactionCount: Int,
    val percentageOfTotal: Double
)

data class MonthComparisonData(
    val month1Key: String, // "YYYY-MM"
    val month1Label: String,
    val month1Total: Double,
    val month2Key: String,
    val month2Label: String,
    val month2Total: Double,
    val differenceAmount: Double, // month1 - month2
    val percentageChange: Double // ((month1 - month2) / month2) * 100
)

data class DailySpendPoint(
    val dayNumber: Int,
    val dateIso: String,
    val dayName: String,
    val amount: Double
)
