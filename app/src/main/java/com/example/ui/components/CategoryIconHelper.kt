package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PropaneTank
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Weekend
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object CategoryIconHelper {
    fun getIcon(iconName: String): ImageVector {
        return when (iconName.lowercase()) {
            "shopping_cart" -> Icons.Default.ShoppingCart
            "water_drop" -> Icons.Default.WaterDrop
            "local_gas_station" -> Icons.Default.LocalGasStation
            "directions_bus" -> Icons.Default.DirectionsBus
            "restaurant" -> Icons.Default.Restaurant
            "bolt" -> Icons.Default.Bolt
            "opacity" -> Icons.Default.Opacity
            "propane_tank" -> Icons.Default.PropaneTank
            "home" -> Icons.Default.Home
            "phone_android" -> Icons.Default.PhoneAndroid
            "wifi" -> Icons.Default.Wifi
            "local_hospital" -> Icons.Default.LocalHospital
            "school" -> Icons.Default.School
            "shopping_bag" -> Icons.Default.ShoppingBag
            "movie" -> Icons.Default.Movie
            "weekend" -> Icons.Default.Weekend
            "spa" -> Icons.Default.Spa
            "flight" -> Icons.Default.Flight
            "account_balance" -> Icons.Default.AccountBalance
            "credit_card" -> Icons.Default.CreditCard
            "payments" -> Icons.Default.Payments
            "store" -> Icons.Default.Store
            "laptop" -> Icons.Default.Laptop
            "trending_up" -> Icons.Default.TrendingUp
            "card_giftcard" -> Icons.Default.CardGiftcard
            "account_balance_wallet" -> Icons.Default.AccountBalanceWallet
            "work" -> Icons.Default.Work
            "savings" -> Icons.Default.Savings
            "attach_money" -> Icons.Default.AttachMoney
            "monetization_on" -> Icons.Default.MonetizationOn
            else -> Icons.Default.Category
        }
    }

    fun parseColor(hex: String, defaultColor: Color = Color(0xFF10B981)): Color {
        return try {
            val cleanHex = hex.removePrefix("#")
            val colorLong = if (cleanHex.length == 6) {
                ("FF$cleanHex").toLong(16)
            } else {
                cleanHex.toLong(16)
            }
            Color(colorLong)
        } catch (e: Exception) {
            defaultColor
        }
    }
}

@Composable
fun CategoryIconBadge(
    iconName: String,
    colorHex: String,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    iconSize: Dp = 24.dp,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(12.dp)
) {
    val categoryColor = CategoryIconHelper.parseColor(colorHex)
    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(categoryColor.copy(alpha = 0.16f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = CategoryIconHelper.getIcon(iconName),
            contentDescription = null,
            tint = categoryColor,
            modifier = Modifier.size(iconSize)
        )
    }
}
