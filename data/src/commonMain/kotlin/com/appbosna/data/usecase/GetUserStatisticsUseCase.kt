package com.appbosna.data.usecase

import com.appbosna.data.domain.AdminRepository
import com.appbosna.data.domain.UserStats
import com.appbosna.shared.util.RequestState

class GetUserStatisticsUseCase(
    private val adminRepository: AdminRepository
) {

    suspend operator fun invoke(): RequestState<UserStats> {
        return try {
            when (val usersResult = adminRepository.getAllUsers()) {
                is RequestState.Success -> {

                    val users = usersResult.data
                    val now = getCurrentMillis()

                    val oneDayAgo = now - 24L * 60 * 60 * 1000
                    val oneWeekAgo = now - 7L * 24 * 60 * 60 * 1000
                    val oneMonthAgo = now - 30L * 24 * 60 * 60 * 1000

                    val newToday = users.count {
                        parseDateTimeToMillis(it.createdAt) >= oneDayAgo
                    }

                    val newWeek = users.count {
                        parseDateTimeToMillis(it.createdAt) >= oneWeekAgo
                    }

                    val newMonth = users.count {
                        parseDateTimeToMillis(it.createdAt) >= oneMonthAgo
                    }

                    RequestState.Success(
                        UserStats(
                            totalUsers = users.size,
                            newUsersToday = newToday,
                            newUsersThisWeek = newWeek,
                            newUsersThisMonth = newMonth
                        )
                    )
                }

                is RequestState.Error ->
                    RequestState.Error(usersResult.message)

                else -> RequestState.Loading
            }
        } catch (e: Exception) {
            RequestState.Error("User statistics failed: ${e.message}")
        }
    }

    /* --------------------------------------------------------- */
    /*  DATETIME PARSER  (PURE KOTLIN — radi svugdje)            */
    /* --------------------------------------------------------- */

    private fun parseDateTimeToMillis(dateTime: String): Long {
        return try {
            val parts = dateTime.trim().split(" ")
            val date = parts[0].split("-")
            val time = parts.getOrNull(1)?.split(":") ?: listOf("0","0","0")

            val year = date[0].toInt()
            val month = date[1].toInt()
            val day = date[2].toInt()

            val hour = time.getOrNull(0)?.toInt() ?: 0
            val minute = time.getOrNull(1)?.toInt() ?: 0
            val second = time.getOrNull(2)?.toInt() ?: 0

            val days = daysSince1970(year, month, day)
            days * 86_400_000L + (hour * 3600 + minute * 60 + second) * 1000L

        } catch (_: Exception) {
            0L
        }
    }

    private fun daysSince1970(year: Int, month: Int, day: Int): Long {
        var days = 0L
        for (y in 1970 until year)
            days += if (isLeap(y)) 366 else 365

        val lengths = intArrayOf(
            31, if (isLeap(year)) 29 else 28, 31, 30, 31, 30,
            31, 31, 30, 31, 30, 31
        )
        for (m in 1 until month)
            days += lengths[m-1]

        return days + (day - 1)
    }

    private fun isLeap(y: Int): Boolean =
        (y % 4 == 0 && y % 100 != 0) || (y % 400 == 0)
}
