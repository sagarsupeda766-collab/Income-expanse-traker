# HisabPro — Income & Expense Tracker

A simple, fast, and intuitive personal finance Android app built with **Jetpack Compose**, **Kotlin**, and **Room Database**. It empowers users to track both **Income** and **Expenses**, monitor budgets, and analyze cashflow with interactive **Bar**, **Pie**, and **Line** charts.

---

## 🌟 Key Features

### 1. Dual Income & Expense Tracking
- **Cash Flow Overview**: Displays **Net Balance** (`Total Income − Total Expenses`) in large, prominent typography on the dashboard.
- **Side-by-Side Cashflow Tiles**:
  - **Total Income**: Highlighted in emerald green with positive badge (`+ ₹65,000`).
  - **Total Expenses**: Displayed in red with spending summary (`- ₹16,750`).
- **One-Tap Quick Entry**: Separate `+ Expense` and `+ Income` buttons on the home screen for lightning-fast entry.

### 2. Fast 1-Tap Category Add Dialog
- **Instant Type Switch**: Toggle between **Expense** and **Income** at the top of the dialog.
- **No Nested Modals**: Directly select any category chip in a single tap.
- **Categorization**:
  - **Income Sources**: Salary / Wages, Business & Sales, Freelance & Projects, Investments, Rental Income, Bonus & Rewards, Other Income.
  - **Expense Categories**: Grocery & Food, Milk & Dairy, Fuel, Transportation, Dining, House Rent, Utilities, Healthcare, Shopping, EMI, etc.
- **Quick Date Chips**: One-tap toggles for *Today*, *Yesterday*, or custom *Pick Date*.
- **Payment / Deposit Methods**: Cash, UPI, Debit Card, Credit Card, Bank Transfer.

### 3. Visual Analytics: Bar Chart, Pie Chart & Line Chart
The Analytics & Reports screen provides three dedicated interactive visualizations with a 1-tap view switcher:
- **📊 Bar Chart (Daily Activity)**:
  - Vertical gradient bars representing spending each day of the month.
  - Peak spending days highlighted in warm accents.
  - Interactive tap detection displaying the exact date, day name, and amount tooltip.
- **🥧 Pie Chart (Category & Source Donut)**:
  - Smooth animated donut slices drawn with distinct category colors.
  - Interactive slice selection: tap any slice or category legend chip to inspect exact amount and percentage share.
  - Quick toggle between **Expense Categories** and **Income Sources**.
  - Multi-segment linear distribution bar for rapid glance at top spending drivers.
- **📈 Line Chart (Cashflow Trend Curve)**:
  - Curved cubic Bezier path tracing spending and cashflow progression across the month.
  - Vertical gradient fill under the curve.
  - Horizontal reference grid lines and interactive data points with inspection tooltips.
- **📑 All Charts Mode**:
  - Displays all 3 charts in sequence for a complete executive financial summary.

### 4. Comprehensive Transactions & Search
- **Filter Chips**: Quickly filter by `All`, `↑ Expenses`, or `↓ Income`.
- **Date Filters**: *Today*, *Yesterday*, *Last 7 Days*, *This Month*, *Previous Month*, *Custom Range*.
- **Search & Sort**: Real-time search across titles, categories, notes, and amounts; sort by newest, oldest, highest, or lowest amount.
- **Quick Actions**: Edit, duplicate to today, or delete transactions with confirmation.

### 5. Month-over-Month Comparison
- Side-by-side comparison of spending patterns with previous months.
- Automatically calculates difference amounts and percentage increase or decrease.

### 6. Budget Management
- Set overall monthly budgets and per-category limits.
- Real-time progress bars with warning alerts when reaching 80% or exceeding the budget.

---

## 🏗️ Architecture & Tech Stack

| Layer | Technology | Details |
|---|---|---|
| **Language** | Kotlin 2.0+ | Modern idiomatic Kotlin with Coroutines & StateFlow |
| **UI Framework** | Jetpack Compose | Material Design 3 (M3) with Canvas-based Charts |
| **Local Database** | Room Database 2.6 | SQLite persistence with type-safe DAOs and automatic migrations |
| **Architecture** | MVVM | Clean separation of UI, ViewModel, and Repository |
| **Visual Charts** | Compose Canvas | Native Canvas rendering for Bar, Donut/Pie, and Curved Line charts |
| **Styling** | M3 Theme | Emerald, Sage, and Mint palette with high-contrast finance indicators |

---

## 💾 Data Schema

### Transaction Entity (`expenses`)
```kotlin
@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val categoryId: String,
    val categoryName: String,
    val title: String,
    val dateIso: String,          // YYYY-MM-DD
    val paymentMethod: String,     // CASH, UPI, etc.
    val type: String = "EXPENSE",  // EXPENSE or INCOME
    val notes: String = "",
    val createdAt: Long,
    val updatedAt: Long
)
```

### Category Entity (`categories`)
```kotlin
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
    val type: String = "EXPENSE"   // EXPENSE or INCOME
)
```

---

## 📦 APK Download & Installation

The production-ready debug APK has been built:
- **File Location**: `app/build/outputs/apk/debug/app-debug.apk`
- **Via AI Studio**: Click the **Settings / Menu** icon at the top of the interface and select **Download APK** or **Export Project (ZIP)**.
