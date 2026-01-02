package com.appbosna.data.config

/**
 * API Configuration Manager
 *
 * Centralized configuration for API backend selection
 *
 * Usage:
 * ```
 * // Switch API type directly
 * ApiConfig.currentApiType = ApiType.REST
 * ApiConfig.currentApiType = ApiType.GRAPHQL
 * ```
 */
object ApiConfig {
    /**
     * Current API type being used
     * Default: REST API
     *
     * Change this directly to switch between REST and GraphQL globally:
     *
     * ```
     * ApiConfig.currentApiType = ApiType.REST      // Use REST API
     * ApiConfig.currentApiType = ApiType.GRAPHQL   // Use GraphQL API
     * ```
     */
    var currentApiType: ApiType = ApiType.REST

    /**
     * Get current API type name for logging
     */
    fun getCurrentApiName(): String = currentApiType.name

    /**
     * Check if currently using GraphQL
     */
    fun isUsingGraphQL(): Boolean = currentApiType == ApiType.GRAPHQL

    /**
     * Check if currently using REST
     */
    fun isUsingRest(): Boolean = currentApiType == ApiType.REST
}
