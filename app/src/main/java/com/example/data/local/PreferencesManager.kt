package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("hisabpro_prefs", Context.MODE_PRIVATE)

    private val _monthlyBudget = MutableStateFlow(prefs.getFloat(KEY_MONTHLY_BUDGET, 35000f).toDouble())
    val monthlyBudget: StateFlow<Double> = _monthlyBudget.asStateFlow()

    private val _currencySymbol = MutableStateFlow(prefs.getString(KEY_CURRENCY_SYMBOL, "₹") ?: "₹")
    val currencySymbol: StateFlow<String> = _currencySymbol.asStateFlow()

    private val _themeMode = MutableStateFlow(prefs.getString(KEY_THEME_MODE, "SYSTEM") ?: "SYSTEM")
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    fun setMonthlyBudget(amount: Double) {
        prefs.edit().putFloat(KEY_MONTHLY_BUDGET, amount.toFloat()).apply()
        _monthlyBudget.value = amount
    }

    fun setCurrencySymbol(symbol: String) {
        prefs.edit().putString(KEY_CURRENCY_SYMBOL, symbol).apply()
        _currencySymbol.value = symbol
    }

    fun setThemeMode(mode: String) {
        prefs.edit().putString(KEY_THEME_MODE, mode).apply()
        _themeMode.value = mode
    }

    fun hasSeededSampleData(): Boolean {
        return prefs.getBoolean(KEY_HAS_SEEDED_SAMPLE, false)
    }

    fun setSampleDataSeeded(seeded: Boolean) {
        prefs.edit().putBoolean(KEY_HAS_SEEDED_SAMPLE, seeded).apply()
    }

    companion object {
        private const val KEY_MONTHLY_BUDGET = "monthly_budget"
        private const val KEY_CURRENCY_SYMBOL = "currency_symbol"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_HAS_SEEDED_SAMPLE = "has_seeded_sample"
    }
}
