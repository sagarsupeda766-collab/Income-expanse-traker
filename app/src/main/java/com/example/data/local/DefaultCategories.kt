package com.example.data.local

import com.example.domain.model.Category
import com.example.domain.model.TransactionType

object DefaultCategories {
    val EXPENSE_LIST: List<Category> = listOf(
        Category(
            id = "grocery",
            name = "Grocery & Food",
            iconName = "shopping_cart",
            colorHex = "#16A34A",
            monthlyBudget = 8000.0,
            isCustom = false,
            orderIndex = 1,
            type = TransactionType.EXPENSE
        ),
        Category(
            id = "milk_dairy",
            name = "Milk & Dairy",
            iconName = "water_drop",
            colorHex = "#0284C7",
            monthlyBudget = 2500.0,
            isCustom = false,
            orderIndex = 2,
            type = TransactionType.EXPENSE
        ),
        Category(
            id = "petrol_fuel",
            name = "Petrol & Fuel",
            iconName = "local_gas_station",
            colorHex = "#EA580C",
            monthlyBudget = 4000.0,
            isCustom = false,
            orderIndex = 3,
            type = TransactionType.EXPENSE
        ),
        Category(
            id = "transportation",
            name = "Transportation",
            iconName = "directions_bus",
            colorHex = "#2563EB",
            monthlyBudget = 2000.0,
            isCustom = false,
            orderIndex = 4,
            type = TransactionType.EXPENSE
        ),
        Category(
            id = "food_restaurants",
            name = "Dining & Snacks",
            iconName = "restaurant",
            colorHex = "#DC2626",
            monthlyBudget = 4500.0,
            isCustom = false,
            orderIndex = 5,
            type = TransactionType.EXPENSE
        ),
        Category(
            id = "electricity_bill",
            name = "Electricity & Utility",
            iconName = "bolt",
            colorHex = "#CA8A04",
            monthlyBudget = 2000.0,
            isCustom = false,
            orderIndex = 6,
            type = TransactionType.EXPENSE
        ),
        Category(
            id = "house_rent",
            name = "House Rent",
            iconName = "home",
            colorHex = "#7C3AED",
            monthlyBudget = 15000.0,
            isCustom = false,
            orderIndex = 7,
            type = TransactionType.EXPENSE
        ),
        Category(
            id = "mobile_internet",
            name = "Mobile & Internet",
            iconName = "phone_android",
            colorHex = "#0D9488",
            monthlyBudget = 1500.0,
            isCustom = false,
            orderIndex = 8,
            type = TransactionType.EXPENSE
        ),
        Category(
            id = "medical_healthcare",
            name = "Medical & Health",
            iconName = "local_hospital",
            colorHex = "#EF4444",
            monthlyBudget = 3000.0,
            isCustom = false,
            orderIndex = 9,
            type = TransactionType.EXPENSE
        ),
        Category(
            id = "shopping",
            name = "Shopping & Clothes",
            iconName = "shopping_bag",
            colorHex = "#D946EF",
            monthlyBudget = 4000.0,
            isCustom = false,
            orderIndex = 10,
            type = TransactionType.EXPENSE
        ),
        Category(
            id = "entertainment",
            name = "Entertainment & OTT",
            iconName = "movie",
            colorHex = "#8B5CF6",
            monthlyBudget = 2000.0,
            isCustom = false,
            orderIndex = 11,
            type = TransactionType.EXPENSE
        ),
        Category(
            id = "emi_loan",
            name = "EMI & Loans",
            iconName = "account_balance",
            colorHex = "#B91C1C",
            monthlyBudget = 10000.0,
            isCustom = false,
            orderIndex = 12,
            type = TransactionType.EXPENSE
        ),
        Category(
            id = "other_expenses",
            name = "Other Expenses",
            iconName = "category",
            colorHex = "#64748B",
            monthlyBudget = 2000.0,
            isCustom = false,
            orderIndex = 13,
            type = TransactionType.EXPENSE
        )
    )

    val INCOME_LIST: List<Category> = listOf(
        Category(
            id = "income_salary",
            name = "Salary / Wages",
            iconName = "payments",
            colorHex = "#10B981",
            monthlyBudget = 0.0,
            isCustom = false,
            orderIndex = 1,
            type = TransactionType.INCOME
        ),
        Category(
            id = "income_business",
            name = "Business & Sales",
            iconName = "store",
            colorHex = "#059669",
            monthlyBudget = 0.0,
            isCustom = false,
            orderIndex = 2,
            type = TransactionType.INCOME
        ),
        Category(
            id = "income_freelance",
            name = "Freelance & Projects",
            iconName = "laptop",
            colorHex = "#0284C7",
            monthlyBudget = 0.0,
            isCustom = false,
            orderIndex = 3,
            type = TransactionType.INCOME
        ),
        Category(
            id = "income_investment",
            name = "Investments & Dividends",
            iconName = "trending_up",
            colorHex = "#7C3AED",
            monthlyBudget = 0.0,
            isCustom = false,
            orderIndex = 4,
            type = TransactionType.INCOME
        ),
        Category(
            id = "income_rental",
            name = "Rental Income",
            iconName = "home",
            colorHex = "#2563EB",
            monthlyBudget = 0.0,
            isCustom = false,
            orderIndex = 5,
            type = TransactionType.INCOME
        ),
        Category(
            id = "income_bonus",
            name = "Bonus & Rewards",
            iconName = "card_giftcard",
            colorHex = "#F59E0B",
            monthlyBudget = 0.0,
            isCustom = false,
            orderIndex = 6,
            type = TransactionType.INCOME
        ),
        Category(
            id = "income_other",
            name = "Other Income",
            iconName = "account_balance_wallet",
            colorHex = "#64748B",
            monthlyBudget = 0.0,
            isCustom = false,
            orderIndex = 7,
            type = TransactionType.INCOME
        )
    )

    val LIST: List<Category> = EXPENSE_LIST + INCOME_LIST
}
