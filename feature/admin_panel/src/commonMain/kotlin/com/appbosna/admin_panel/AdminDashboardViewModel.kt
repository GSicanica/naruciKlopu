package com.appbosna.admin_panel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appbosna.data.domain.DateRange
import com.appbosna.data.domain.UserStats
import com.appbosna.data.usecase.GetDashboardAnalyticsUseCase
import com.appbosna.data.usecase.GetUserStatisticsUseCase
import com.appbosna.shared.util.RequestState
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminDashboardViewModel(
    private val getDashboardAnalyticsUseCase: GetDashboardAnalyticsUseCase,
    private val getUserStatisticsUseCase: GetUserStatisticsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AdminDashboardState())
    val state: StateFlow<AdminDashboardState> = _state.asStateFlow()

    init {
        loadDashboardData()
    }

    fun onEvent(event: AdminDashboardEvent) {
        when (event) {
            is AdminDashboardEvent.DateRangeChanged -> {
                _state.value = _state.value.copy(
                    selectedDateRange = event.dateRange,
                    errorMessage = null
                )
                loadDashboardData(event.dateRange)
            }

            AdminDashboardEvent.RefreshData -> refreshData()
            AdminDashboardEvent.ClearError -> clearError()
        }
    }

    private fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }

    private fun loadDashboardData(dateRange: DateRange = _state.value.selectedDateRange) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)

            try {
                val analyticsDeferred = async { getDashboardAnalyticsUseCase(dateRange) }
                val userStatsDeferred = async { getUserStatisticsUseCase() }

                val analyticsResult = analyticsDeferred.await()
                val userStatsResult = userStatsDeferred.await()

                when (analyticsResult) {
                    is RequestState.Success -> {
                        val finalUserStats = when (userStatsResult) {
                            is RequestState.Success -> userStatsResult.data
                            else -> UserStats(0, 0, 0, 0)
                        }

                        val analytics = analyticsResult.data.copy(
                            userStats = finalUserStats
                        )

                        _state.value = _state.value.copy(
                            isLoading = false,
                            dashboardAnalytics = analytics
                        )
                    }

                    is RequestState.Error ->
                        setError(analyticsResult.message)

                    else -> stopLoading()
                }

            } catch (e: Exception) {
                setError("Unexpected error: ${e.message}")
            }
        }
    }

    private fun refreshData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isRefreshing = true)

            try {
                val result = getDashboardAnalyticsUseCase(_state.value.selectedDateRange)

                when (result) {
                    is RequestState.Success -> {
                        _state.value = _state.value.copy(
                            dashboardAnalytics = result.data,
                            isRefreshing = false
                        )
                    }

                    is RequestState.Error ->
                        setError(result.message, refreshing = true)

                    else ->
                        stopRefreshing()
                }

            } catch (e: Exception) {
                setError("Refresh failed: ${e.message}", refreshing = true)
            }
        }
    }

    private fun stopLoading() {
        _state.value = _state.value.copy(isLoading = false)
    }

    private fun stopRefreshing() {
        _state.value = _state.value.copy(isRefreshing = false)
    }

    private fun setError(message: String?, refreshing: Boolean = false) {
        _state.value = _state.value.copy(
            isLoading = false,
            isRefreshing = if (refreshing) false else _state.value.isRefreshing,
            errorMessage = message
        )
    }
}
