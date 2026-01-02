package com.appbosna.data.config

/**
 * API Type Configuration
 *
 * Determines which API backend to use for network requests
 */
enum class ApiType {
    /**
     * REST API - Traditional RESTful endpoints
     * - Multiple endpoints per resource
     * - Standard HTTP methods (GET, POST, PUT, DELETE)
     * - May over-fetch or under-fetch data
     */
    REST,

    /**
     * GraphQL API - Modern query language
     * - Single endpoint for all operations
     * - Request only needed fields
     * - Type-safe with compile-time checking
     * - Better for mobile apps (reduces bandwidth)
     */
    GRAPHQL;

    companion object {
        /**
         * Default API type
         * Can be changed based on environment or feature flags
         */
        val DEFAULT = REST

        /**
         * Check if GraphQL is available/enabled
         */
        fun isGraphQLAvailable(): Boolean = true

        /**
         * Check if REST is available/enabled
         */
        fun isRestAvailable(): Boolean = true
    }
}
