package com.appbosna.data.usecase

import com.appbosna.data.domain.AdminRepository
import com.appbosna.data.domain.DailySummary
import com.appbosna.data.domain.DashboardAnalytics
import com.appbosna.data.domain.DateRange
import com.appbosna.data.domain.TopSellingProduct
import com.appbosna.data.domain.UserStats
import com.appbosna.shared.domain.Order
import com.appbosna.shared.util.RequestState
import io.ktor.util.date.getTimeMillis

class GetDashboardAnalyticsUseCase(
    private val adminRepository: AdminRepository
) {

    suspend operator fun invoke(dateRange: DateRange): RequestState<DashboardAnalytics> {
        return try {
            println("📊 Analytics Request: ${dateRange.startDate} to ${dateRange.endDate}")

            // Determine period string for API
            val period = determinePeriod(dateRange)
            println("🔍 Using period: $period")

            // Call new analytics endpoints
            val revenueResult = adminRepository.getRevenueAnalytics(period)
            val ordersByHourResult = adminRepository.getOrdersByHourAnalytics(period)

            // Get orders for detailed calculations (top products etc)
            val ordersResult = adminRepository.getOrdersByDateRange(
                dateRange.startDate,
                dateRange.endDate
            )

            when {
                revenueResult is RequestState.Error -> {
                    println("❌ Revenue analytics error: ${revenueResult.message}")
                    return RequestState.Error(revenueResult.message)
                }
                ordersByHourResult is RequestState.Error -> {
                    println("❌ Orders by hour error: ${ordersByHourResult.message}")
                    return RequestState.Error(ordersByHourResult.message)
                }
                ordersResult is RequestState.Error -> {
                    println("❌ Orders error: ${ordersResult.message}")
                    return RequestState.Error(ordersResult.message)
                }
                revenueResult is RequestState.Success &&
                ordersByHourResult is RequestState.Success &&
                ordersResult is RequestState.Success -> {
                    
                    val revenue = revenueResult.data
                    val ordersByHour = ordersByHourResult.data
                    val orders = ordersResult.data
                    
                    println("📦 Orders Found: ${orders.size}")
                    println("💰 Total Revenue: ${revenue.total_revenue}")
                    println("📊 Peak Hour: ${ordersByHour.peak_hours.busiest_hour}:00 with ${ordersByHour.peak_hours.busiest_hour_orders} orders")

                    val analytics = buildDashboardAnalytics(revenue, orders)
                    RequestState.Success(analytics)
                }
                else -> RequestState.Loading
            }
        } catch (e: Exception) {
            e.printStackTrace()
            RequestState.Error("Analytics calculation failed: ${e.message}")
        }
    }
    
    private fun determinePeriod(dateRange: DateRange): String {
        val now = getTimeMillis()
        val diffDays = (now - dateRange.startDate) / (24 * 60 * 60 * 1000)
        
        return when {
            diffDays <= 1 -> "today"
            diffDays <= 7 -> "week"
            diffDays <= 30 -> "month"
            else -> "year"
        }
    }
    
    private suspend fun buildDashboardAnalytics(
        revenue: com.appbosna.data.domain.RevenueAnalytics,
        orders: List<Order>
    ): DashboardAnalytics {
        val topSellingProducts = calculateTopSellingProducts(orders)
        val dailySummaries = revenue.daily_breakdown.map { day ->
            DailySummary(
                date = day.date,
                totalRevenue = day.revenue,
                orderCount = day.orders
            )
        }

        val userStatsResult = getUserStats()
        val userStats = when (userStatsResult) {
            is RequestState.Success -> userStatsResult.data
            else -> UserStats(
                totalUsers = 0,
                newUsersToday = 0,
                newUsersThisWeek = 0,
                newUsersThisMonth = 0
            )
        }

        return DashboardAnalytics(
            totalRevenue = revenue.total_revenue,
            totalOrders = revenue.total_orders,
            averageOrderValue = revenue.average_order_value,
            topSellingProducts = topSellingProducts,
            dailySummaries = dailySummaries,
            userStats = userStats
        )
    }

    // -------------------------------------------------------------------------
    // TOP SELLING PRODUCTS
    // -------------------------------------------------------------------------

    private fun calculateTopSellingProducts(orders: List<Order>): List<TopSellingProduct> {
        // Sve stavke iz svih narudžbi
        val allItems = orders.flatMap { it.items }

        return allItems
            .groupBy { it.productId }
            .mapNotNull { (productId, items) ->
                if (productId.isNullOrBlank()) return@mapNotNull null

                // quantity i price su ti stringovi – koristimo toIntSafe / toDoubleSafe
                val unitsSold = items.sumOf { item ->
                    item.quantity.toString().toIntSafe()
                }

                val totalRevenue = items.sumOf { item ->
                    val qty = item.quantity.toString().toIntSafe()
                    val price = item.price.toString().toDoubleSafe()
                    qty * price
                }

                val first = items.firstOrNull()
                if (first == null) return@mapNotNull null

                TopSellingProduct(
                    productId = productId,
                    productName = first.title,
                    unitsSold = unitsSold,
                    totalRevenue = totalRevenue,
                    thumbnail = first.thumbnail
                )
            }
            .sortedByDescending { it.unitsSold }
            .take(10)
    }

    // -------------------------------------------------------------------------
    // USER STATS (koristi MySQL datetime string iz users.createdAt)
    // -------------------------------------------------------------------------

    private suspend fun getUserStats(): RequestState<UserStats> {
        return try {
            when (val usersResult = adminRepository.getAllUsers()) {
                is RequestState.Success -> {
                    val users = usersResult.data

                    val now = getTimeMillis()
                    val oneDayAgo = now - 24L * 60 * 60 * 1000
                    val oneWeekAgo = now - 7L * 24 * 60 * 60 * 1000
                    val oneMonthAgo = now - 30L * 24 * 60 * 60 * 1000

                    val newUsersToday = users.count {
                        mysqlDateTimeToMillis(it.createdAt) >= oneDayAgo
                    }

                    val newUsersThisWeek = users.count {
                        mysqlDateTimeToMillis(it.createdAt) >= oneWeekAgo
                    }

                    val newUsersThisMonth = users.count {
                        mysqlDateTimeToMillis(it.createdAt) >= oneMonthAgo
                    }

                    RequestState.Success(
                        UserStats(
                            totalUsers = users.size,
                            newUsersToday = newUsersToday,
                            newUsersThisWeek = newUsersThisWeek,
                            newUsersThisMonth = newUsersThisMonth
                        )
                    )
                }

                is RequestState.Error -> RequestState.Error(usersResult.message)
                is RequestState.Loading -> RequestState.Loading
                is RequestState.Idle -> RequestState.Idle
            }
        } catch (e: Exception) {
            e.printStackTrace()
            RequestState.Error("User stats calculation failed: ${e.message}")
        }
    }

    // -------------------------------------------------------------------------
    // DATUM HELPERI – MySQL "YYYY-MM-DD HH:MM:SS" → epoch millis
    // -------------------------------------------------------------------------

    /**
     * MySQL datetime "YYYY-MM-DD HH:MM:SS" → epoch millis (UTC-ish, bez timezone-a).
     * Ako nešto ne valja u stringu, vraća 0L.
     */
    private fun mysqlDateTimeToMillis(dateTime: String): Long {
        return try {
            val trimmed = dateTime.trim()
            if (trimmed.isEmpty()) return 0L

            val parts = trimmed.split(" ")
            val datePart = parts.getOrNull(0) ?: return 0L
            val timePart = parts.getOrNull(1) ?: "00:00:00"

            val (year, month, day) = datePart.split("-").map { it.toInt() }
            val (hour, minute, second) = timePart.split(":").map { it.toInt() }

            val daysSince1970 = calcDaysSince1970(year, month, day)
            val millisFromDays = daysSince1970 * 24L * 60L * 60L * 1000L
            val millisFromTime = (hour * 3600 + minute * 60 + second) * 1000L

            millisFromDays + millisFromTime
        } catch (e: Exception) {
            0L
        }
    }

    /**
     * Broj dana od 1970-01-01 do (year-month-day).
     */
    private fun calcDaysSince1970(year: Int, month: Int, day: Int): Long {
        var days = 0L

        // godine
        for (y in 1970 until year) {
            days += if (isLeap(y)) 366 else 365
        }

        // mjeseci u zadanoj godini
        val monthLengths = intArrayOf(
            31,
            if (isLeap(year)) 29 else 28,
            31, 30, 31, 30,
            31, 31, 30, 31, 30, 31
        )

        for (m in 1 until month) {
            days += monthLengths[m - 1]
        }

        // dani u mjesecu (day počinje od 1)
        days += (day - 1)

        return days
    }

    private fun isLeap(year: Int): Boolean =
        (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
}

// ============================================================================
// EKSTENZIJE ZA SIGURNO PARSANJE STRINGOVA
// ============================================================================

fun String.toIntSafe(): Int =
    this.toIntOrNull() ?: 0

fun String.toDoubleSafe(): Double =
    this.toDoubleOrNull() ?: 0.0
