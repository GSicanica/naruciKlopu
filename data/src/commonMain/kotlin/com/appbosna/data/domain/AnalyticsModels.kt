package com.appbosna.data.domain

import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlinx.serialization.Serializable

/**
 * Response model for /api/analytics/revenue endpoint
 */
@Serializable
data class RevenueAnalytics(
    val total_revenue: Double,
    val total_orders: Int,
    val average_order_value: Double,
    val period: String,
    val date_from: String,
    val date_to: String,
    val daily_breakdown: List<DailyRevenue>,
    val comparison: RevenueComparison
)

@Serializable
data class DailyRevenue(
    val date: String,
    val revenue: Double,
    val orders: Int
)

@Serializable
data class RevenueComparison(
    val previous_period_revenue: Double,
    val revenue_change_percent: Double,
    val previous_period_orders: Int,
    val orders_change_percent: Double
)

/**
 * Response model for /api/analytics/orders-by-hour endpoint
 */
@Serializable
data class OrdersByHourAnalytics(
    val date_from: String,
    val date_to: String,
    val total_orders: Int,
    val hourly_data: List<HourlyOrders>,
    val peak_hours: PeakHours
)

@Serializable
data class HourlyOrders(
    val hour: Int,
    val hour_label: String,
    val orders: Int,
    val revenue: Double,
    val percentage: Double
)

@Serializable
data class PeakHours(
    val busiest_hour: Int,
    val busiest_hour_orders: Int,
    val quietest_hour: Int,
    val quietest_hour_orders: Int
)

/**
 * Günlük özet verileri için model
 */
@Serializable
data class DailySummary(
    val date: String,
    val totalRevenue: Double,
    val orderCount: Int,
    val averageOrderValue: Double = if (orderCount > 0) totalRevenue / orderCount else 0.0
)

/**
 * En çok satan ürünler için model
 */
@Serializable
data class TopSellingProduct(
    val productId: String,
    val productName: String,
    val unitsSold: Int,
    val totalRevenue: Double,
    val thumbnail: String? = null
)

/**
 * Kullanıcı istatistikleri için model
 */
@Serializable
data class UserStats(
    val totalUsers: Int,
    val newUsersToday: Int,
    val newUsersThisWeek: Int,
    val newUsersThisMonth: Int
)

/**
 * Dashboard analitikleri için ana model
 */
@Serializable
data class DashboardAnalytics(
    val totalRevenue: Double,
    val totalOrders: Int,
    val averageOrderValue: Double,
    val topSellingProducts: List<TopSellingProduct>,
    val dailySummaries: List<DailySummary>,
    val userStats: UserStats
)

/**
 * Tarih aralığı seçimi için model
 */
@Serializable
        @OptIn(ExperimentalTime::class)
data class DateRange(
    val startDate: Long,
    val endDate: Long
) {
    companion object {
        fun today(): DateRange {
            val now = Clock.System.now().toEpochMilliseconds()
            val startOfDay = now - (now % (24 * 60 * 60 * 1000))
            val endOfDay = startOfDay + (24 * 60 * 60 * 1000) - 1
            return DateRange(startOfDay, endOfDay)
        }
        
        fun lastWeek(): DateRange {
            val now = Clock.System.now().toEpochMilliseconds()
            val weekAgo = now - (7 * 24 * 60 * 60 * 1000)
            return DateRange(weekAgo, now)
        }
        
        fun lastMonth(): DateRange {
            val now = Clock.System.now().toEpochMilliseconds()
            val monthAgo = now - (30 * 24 * 60 * 60 * 1000)
            return DateRange(monthAgo, now)
        }
    }
} 