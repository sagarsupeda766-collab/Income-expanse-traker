package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.CategoryDao
import com.example.data.local.CategoryEntity
import com.example.data.local.DefaultCategories
import com.example.data.local.ExpenseDao
import com.example.data.local.ExpenseEntity
import com.example.data.local.PreferencesManager
import com.example.domain.model.Category
import com.example.domain.model.Expense
import com.example.domain.model.PaymentMethod
import com.example.domain.model.TransactionType
import com.example.util.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

class ExpenseRepository(
    private val database: AppDatabase,
    val preferencesManager: PreferencesManager
) {
    private val expenseDao: ExpenseDao = database.expenseDao()
    private val categoryDao: CategoryDao = database.categoryDao()

    val allExpensesFlow: Flow<List<Expense>> = expenseDao.getAllExpensesFlow().map { entities ->
        entities.map { it.toDomain() }
    }

    val allCategoriesFlow: Flow<List<Category>> = categoryDao.getAllCategoriesFlow().map { entities ->
        entities.map { it.toDomain() }
    }

    suspend fun checkAndSeedDefaults() = withContext(Dispatchers.IO) {
        val count = categoryDao.countCategories()
        if (count == 0) {
            val entities = DefaultCategories.LIST.map { CategoryEntity.fromDomain(it) }
            categoryDao.insertAll(entities)
        }
    }

    suspend fun insertExpense(expense: Expense): Long = withContext(Dispatchers.IO) {
        val entity = ExpenseEntity.fromDomain(expense)
        expenseDao.insert(entity)
    }

    suspend fun updateExpense(expense: Expense) = withContext(Dispatchers.IO) {
        val entity = ExpenseEntity.fromDomain(expense)
        expenseDao.update(entity)
    }

    suspend fun deleteExpense(expense: Expense) = withContext(Dispatchers.IO) {
        val entity = ExpenseEntity.fromDomain(expense)
        expenseDao.delete(entity)
    }

    suspend fun deleteExpenseById(id: Long) = withContext(Dispatchers.IO) {
        expenseDao.deleteById(id)
    }

    suspend fun clearAllData() = withContext(Dispatchers.IO) {
        expenseDao.clearAll()
    }

    suspend fun insertCategory(category: Category) = withContext(Dispatchers.IO) {
        val entity = CategoryEntity.fromDomain(category)
        categoryDao.insert(entity)
    }

    suspend fun updateCategory(category: Category) = withContext(Dispatchers.IO) {
        val entity = CategoryEntity.fromDomain(category)
        categoryDao.update(entity)
    }

    suspend fun deleteCategory(category: Category) = withContext(Dispatchers.IO) {
        val entity = CategoryEntity.fromDomain(category)
        categoryDao.delete(entity)
    }

    suspend fun updateCategoryBudget(categoryId: String, budget: Double) = withContext(Dispatchers.IO) {
        categoryDao.updateCategoryBudget(categoryId, budget)
    }

    suspend fun getExpensesForCategory(categoryId: String): List<Expense> = withContext(Dispatchers.IO) {
        expenseDao.getAllExpensesList()
            .filter { it.categoryId == categoryId }
            .map { it.toDomain() }
    }

    suspend fun seedSampleTransactions() = withContext(Dispatchers.IO) {
        checkAndSeedDefaults()
        val today = DateUtils.today()

        val sampleTransactions = listOf(
            // Income for this month
            Expense(
                amount = 65000.0,
                categoryId = "income_salary",
                categoryName = "Salary / Wages",
                title = "Monthly Salary Deposit",
                dateIso = today.withDayOfMonth(1).toString(),
                paymentMethod = PaymentMethod.BANK_TRANSFER,
                type = TransactionType.INCOME,
                notes = "Primary employment direct credit"
            ),
            Expense(
                amount = 12500.0,
                categoryId = "income_freelance",
                categoryName = "Freelance & Projects",
                title = "Mobile UI Consultation",
                dateIso = today.minusDays(6.coerceAtMost(today.dayOfMonth - 1).toLong()).toString(),
                paymentMethod = PaymentMethod.UPI,
                type = TransactionType.INCOME,
                notes = "Client milestone payout"
            ),
            // Today's expenses
            Expense(
                amount = 60.0,
                categoryId = "milk_dairy",
                categoryName = "Milk & Dairy",
                title = "Cow Milk 1L Packets",
                dateIso = today.toString(),
                paymentMethod = PaymentMethod.UPI,
                type = TransactionType.EXPENSE,
                notes = "Daily morning delivery"
            ),
            Expense(
                amount = 450.0,
                categoryId = "grocery",
                categoryName = "Grocery & Food",
                title = "Fresh Veggies & Fruits",
                dateIso = today.toString(),
                paymentMethod = PaymentMethod.CASH,
                type = TransactionType.EXPENSE,
                notes = "Tomatoes, Onions, Potatoes & Spinach"
            ),
            Expense(
                amount = 300.0,
                categoryId = "petrol_fuel",
                categoryName = "Petrol & Fuel",
                title = "Bike Fuel refill",
                dateIso = today.toString(),
                paymentMethod = PaymentMethod.UPI,
                type = TransactionType.EXPENSE,
                notes = "HP Petrol Pump"
            ),
            // Yesterday's expenses
            Expense(
                amount = 150.0,
                categoryId = "food_restaurants",
                categoryName = "Dining & Snacks",
                title = "Breakfast & Masala Tea",
                dateIso = today.minusDays(1).toString(),
                paymentMethod = PaymentMethod.UPI,
                type = TransactionType.EXPENSE,
                notes = "South Indian breakfast stall"
            ),
            Expense(
                amount = 200.0,
                categoryId = "medical_healthcare",
                categoryName = "Medical & Health",
                title = "Medicine & Vitamins",
                dateIso = today.minusDays(1).toString(),
                paymentMethod = PaymentMethod.DEBIT_CARD,
                type = TransactionType.EXPENSE,
                notes = "Apollo Pharmacy"
            ),
            // Earlier this week
            Expense(
                amount = 1200.0,
                categoryId = "grocery",
                categoryName = "Grocery & Food",
                title = "Weekly Supermarket Staples",
                dateIso = today.minusDays(3).toString(),
                paymentMethod = PaymentMethod.CREDIT_CARD,
                type = TransactionType.EXPENSE,
                notes = "Rice, Dal, Atta & Cooking Oil"
            ),
            Expense(
                amount = 900.0,
                categoryId = "electricity_bill",
                categoryName = "Electricity & Utility",
                title = "Monthly Power Utility",
                dateIso = today.minusDays(5).toString(),
                paymentMethod = PaymentMethod.BANK_TRANSFER,
                type = TransactionType.EXPENSE,
                notes = "Discom online portal"
            ),
            Expense(
                amount = 650.0,
                categoryId = "mobile_internet",
                categoryName = "Mobile & Internet",
                title = "3 Months Unlimited Data Plan",
                dateIso = today.minusDays(6).toString(),
                paymentMethod = PaymentMethod.UPI,
                type = TransactionType.EXPENSE,
                notes = "Jio 5G prepaid pack"
            ),
            // Earlier this month
            Expense(
                amount = 15000.0,
                categoryId = "house_rent",
                categoryName = "House Rent",
                title = "Apartment Rent",
                dateIso = today.withDayOfMonth(3).toString(),
                paymentMethod = PaymentMethod.BANK_TRANSFER,
                type = TransactionType.EXPENSE,
                notes = "Monthly flat rental"
            ),
            // Previous month data for comparison
            Expense(
                amount = 65000.0,
                categoryId = "income_salary",
                categoryName = "Salary / Wages",
                title = "Monthly Salary Deposit",
                dateIso = today.minusMonths(1).withDayOfMonth(1).toString(),
                paymentMethod = PaymentMethod.BANK_TRANSFER,
                type = TransactionType.INCOME,
                notes = "Previous month salary"
            ),
            Expense(
                amount = 3200.0,
                categoryId = "grocery",
                categoryName = "Grocery & Food",
                title = "Previous Month Bulk Grocery",
                dateIso = today.minusMonths(1).withDayOfMonth(5).toString(),
                paymentMethod = PaymentMethod.CREDIT_CARD,
                type = TransactionType.EXPENSE,
                notes = "Bulk grocery stock"
            ),
            Expense(
                amount = 1800.0,
                categoryId = "petrol_fuel",
                categoryName = "Petrol & Fuel",
                title = "Monthly Car Fuel",
                dateIso = today.minusMonths(1).withDayOfMonth(12).toString(),
                paymentMethod = PaymentMethod.DEBIT_CARD,
                type = TransactionType.EXPENSE,
                notes = "Indian Oil"
            )
        )

        expenseDao.insertAll(sampleTransactions.map { ExpenseEntity.fromDomain(it) })
        preferencesManager.setSampleDataSeeded(true)
    }

    suspend fun exportToJson(): String = withContext(Dispatchers.IO) {
        val expenses = expenseDao.getAllExpensesList()
        val categories = categoryDao.getAllCategoriesList()

        val root = JSONObject()
        root.put("app", "HisabPro")
        root.put("version", 2)
        root.put("exportedAt", System.currentTimeMillis())
        root.put("monthlyBudget", preferencesManager.monthlyBudget.value)
        root.put("currencySymbol", preferencesManager.currencySymbol.value)

        val categoriesArray = JSONArray()
        for (cat in categories) {
            val cObj = JSONObject()
            cObj.put("id", cat.id)
            cObj.put("name", cat.name)
            cObj.put("iconName", cat.iconName)
            cObj.put("colorHex", cat.colorHex)
            cObj.put("monthlyBudget", cat.monthlyBudget)
            cObj.put("isCustom", cat.isCustom)
            cObj.put("orderIndex", cat.orderIndex)
            cObj.put("type", cat.type)
            categoriesArray.put(cObj)
        }
        root.put("categories", categoriesArray)

        val expensesArray = JSONArray()
        for (exp in expenses) {
            val eObj = JSONObject()
            eObj.put("id", exp.id)
            eObj.put("amount", exp.amount)
            eObj.put("categoryId", exp.categoryId)
            eObj.put("categoryName", exp.categoryName)
            eObj.put("title", exp.title)
            eObj.put("dateIso", exp.dateIso)
            eObj.put("paymentMethod", exp.paymentMethod)
            eObj.put("type", exp.type)
            eObj.put("notes", exp.notes)
            eObj.put("createdAt", exp.createdAt)
            eObj.put("updatedAt", exp.updatedAt)
            expensesArray.put(eObj)
        }
        root.put("expenses", expensesArray)

        root.toString(2)
    }

    suspend fun restoreFromJson(jsonString: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString)
            if (!root.has("expenses")) return@withContext false

            if (root.has("monthlyBudget")) {
                preferencesManager.setMonthlyBudget(root.getDouble("monthlyBudget"))
            }
            if (root.has("currencySymbol")) {
                preferencesManager.setCurrencySymbol(root.getString("currencySymbol"))
            }

            if (root.has("categories")) {
                val catArray = root.getJSONArray("categories")
                val categoryEntities = mutableListOf<CategoryEntity>()
                for (i in 0 until catArray.length()) {
                    val cObj = catArray.getJSONObject(i)
                    categoryEntities.add(
                        CategoryEntity(
                            id = cObj.getString("id"),
                            name = cObj.getString("name"),
                            iconName = cObj.optString("iconName", "category"),
                            colorHex = cObj.optString("colorHex", "#10B981"),
                            monthlyBudget = cObj.optDouble("monthlyBudget", 0.0),
                            isCustom = cObj.optBoolean("isCustom", false),
                            orderIndex = cObj.optInt("orderIndex", i),
                            type = cObj.optString("type", "EXPENSE")
                        )
                    )
                }
                categoryDao.insertAll(categoryEntities)
            }

            val expArray = root.getJSONArray("expenses")
            val expenseEntities = mutableListOf<ExpenseEntity>()
            for (i in 0 until expArray.length()) {
                val eObj = expArray.getJSONObject(i)
                expenseEntities.add(
                    ExpenseEntity(
                        id = eObj.optLong("id", 0),
                        amount = eObj.getDouble("amount"),
                        categoryId = eObj.getString("categoryId"),
                        categoryName = eObj.getString("categoryName"),
                        title = eObj.getString("title"),
                        dateIso = eObj.getString("dateIso"),
                        paymentMethod = eObj.getString("paymentMethod"),
                        type = eObj.optString("type", "EXPENSE"),
                        notes = eObj.optString("notes", ""),
                        createdAt = eObj.optLong("createdAt", System.currentTimeMillis()),
                        updatedAt = eObj.optLong("updatedAt", System.currentTimeMillis())
                    )
                )
            }
            expenseDao.insertAll(expenseEntities)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun exportToCsv(): String = withContext(Dispatchers.IO) {
        val expenses = expenseDao.getAllExpensesList()
        val sb = StringBuilder()
        sb.append("ID,Type,Date,Item / Title,Category,Amount,Payment Method,Notes\n")
        for (e in expenses) {
            val escapedTitle = "\"${e.title.replace("\"", "\"\"")}\""
            val escapedCategory = "\"${e.categoryName.replace("\"", "\"\"")}\""
            val escapedNotes = "\"${e.notes.replace("\"", "\"\"")}\""
            sb.append("${e.id},${e.type},${e.dateIso},$escapedTitle,$escapedCategory,${e.amount},${e.paymentMethod},$escapedNotes\n")
        }
        sb.toString()
    }
}
