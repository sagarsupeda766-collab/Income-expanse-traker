package com.example.util

import java.text.DecimalFormat
import java.util.Locale
import kotlin.math.abs

object CurrencyFormatter {

    /**
     * Formats a double amount into Indian numbering system format:
     * e.g., 1500 -> "₹1,500"
     * 25000 -> "₹25,000"
     * 150000 -> "₹1,50,000"
     * 1234567.5 -> "₹12,34,567.50"
     */
    fun format(amount: Double, currencySymbol: String = "₹", showDecimals: Boolean = false): String {
        val isNegative = amount < 0
        val positiveAmount = abs(amount)

        val integerPart = positiveAmount.toLong()
        val decimalPart = ((positiveAmount - integerPart) * 100).toLong()

        val intStr = integerPart.toString()
        val formattedInt = formatIndianInteger(intStr)

        val result = if (showDecimals || decimalPart > 0) {
            val decStr = String.format(Locale.US, "%02d", decimalPart)
            "$formattedInt.$decStr"
        } else {
            formattedInt
        }

        return if (isNegative) {
            "-$currencySymbol$result"
        } else {
            "$currencySymbol$result"
        }
    }

    private fun formatIndianInteger(numStr: String): String {
        val len = numStr.length
        if (len <= 3) return numStr

        val lastThree = numStr.substring(len - 3)
        val remaining = numStr.substring(0, len - 3)

        val sb = StringBuilder()
        var count = 0
        for (i in remaining.length - 1 downTo 0) {
            sb.append(remaining[i])
            count++
            if (count % 2 == 0 && i > 0) {
                sb.append(',')
            }
        }

        val remainingFormatted = sb.reverse().toString()
        return "$remainingFormatted,$lastThree"
    }

    fun formatCompact(amount: Double, currencySymbol: String = "₹"): String {
        return when {
            abs(amount) >= 1_00_00_000 -> { // 1 Crore
                val cr = amount / 1_00_00_000
                String.format(Locale.US, "%s%.2f Cr", currencySymbol, cr)
            }
            abs(amount) >= 1_00_000 -> { // 1 Lakh
                val lakh = amount / 1_00_000
                String.format(Locale.US, "%s%.2f L", currencySymbol, lakh)
            }
            abs(amount) >= 1_000 -> { // 1 Thousand
                val k = amount / 1_000
                String.format(Locale.US, "%s%.1f k", currencySymbol, k)
            }
            else -> format(amount, currencySymbol, false)
        }
    }
}
