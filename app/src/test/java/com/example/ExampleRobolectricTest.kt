package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.util.CurrencyFormatter
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("HisabPro", appName)
    }

    @Test
    fun `verify indian currency formatting`() {
        assertEquals("₹1,500", CurrencyFormatter.format(1500.0, "₹"))
        assertEquals("₹25,000", CurrencyFormatter.format(25000.0, "₹"))
        assertEquals("₹1,50,000", CurrencyFormatter.format(150000.0, "₹"))
        assertEquals("₹10,50,000", CurrencyFormatter.format(1050000.0, "₹"))
    }
}
