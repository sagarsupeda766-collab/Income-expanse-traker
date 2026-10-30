package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.Category
import com.example.domain.model.Expense
import com.example.domain.model.PaymentMethod
import com.example.domain.model.TransactionType

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val iconName: String,
    val colorHex: String,
    val monthlyBudget: Double = 0.0,
    val isCustom: Boolean = false,
    val orderIndex: Int = 0,
    val type: String = "EXPENSE" // "EXPENSE" or "INCOME"
) {
    fun toDomain(): Category {
        return Category(
            id = id,
            name = name,
            iconName = iconName,
            colorHex = colorHex,
            monthlyBudget = monthlyBudget,
            isCustom = isCustom,
            orderIndex = orderIndex,
            type = TransactionType.fromString(type)
        )
    }

    companion object {
        fun fromDomain(domain: Category): CategoryEntity {
            return CategoryEntity(
                id = domain.id,
                name = domain.name,
                iconName = domain.iconName,
                colorHex = domain.colorHex,
                monthlyBudget = domain.monthlyBudget,
                isCustom = domain.isCustom,
                orderIndex = domain.orderIndex,
                type = domain.type.name
            )
        }
    }
}

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val categoryId: String,
    val categoryName: String,
    val title: String,
    val dateIso: String, // YYYY-MM-DD
    val paymentMethod: String,
    val notes: String = "",
    val receiptUri: String? = null,
    val type: String = "EXPENSE", // "EXPENSE" or "INCOME"
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): Expense {
        return Expense(
            id = id,
            amount = amount,
            categoryId = categoryId,
            categoryName = categoryName,
            title = title,
            dateIso = dateIso,
            paymentMethod = PaymentMethod.fromString(paymentMethod),
            notes = notes,
            receiptUri = receiptUri,
            type = TransactionType.fromString(type),
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    companion object {
        fun fromDomain(domain: Expense): ExpenseEntity {
            return ExpenseEntity(
                id = domain.id,
                amount = domain.amount,
                categoryId = domain.categoryId,
                categoryName = domain.categoryName,
                title = domain.title,
                dateIso = domain.dateIso,
                paymentMethod = domain.paymentMethod.name,
                notes = domain.notes,
                receiptUri = domain.receiptUri,
                type = domain.type.name,
                createdAt = domain.createdAt,
                updatedAt = domain.updatedAt
            )
        }
    }
}
