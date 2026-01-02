package com.appbosna.data.remote

import TokenStore

class AuthRepository(
    private val api: UnifiedApiService,
    private val tokenStore: TokenStore
) {

    suspend fun login(
        email: String,
        password: String
    ): LoginRes {
        val res = api.login(
            LoginReq(
                email = email,
                password = password
            )
        )
        
        if (res.error != null) {
            throw RuntimeException(res.error)
        }
        
        if (res.token.isNotBlank()) {
            tokenStore.setToken(res.token)
        } else {
            throw RuntimeException("No token in login response")
        }
        
        return res
    }

    suspend fun register(
        name: String,
        email: String,
        password: String
    ): LoginRes {
        val res = api.register(
            RegisterReq(
                name = name,
                email = email,
                password = password
            )
        )
        
        if (res.error != null) {
            throw RuntimeException(res.error)
        }
        
        if (res.token.isNotBlank()) {
            tokenStore.setToken(res.token)
        } else {
            throw RuntimeException("No token in register response")
        }
        
        return res
    }

    suspend fun currentUser(): ApiUser? {
        val token = tokenStore.getToken() ?: return null
        return try {
            api.me(token)
        } catch (_: Exception) {
            null
        }
    }

    suspend fun logout() {
        tokenStore.setToken(null)
    }

    /** Login preko e-maila bez lozinke */
    suspend fun loginWithEmail(
        email: String,
        name: String
    ): ApiUser {

        // Google UID simulacija
        val uid = email.lowercase().hashCode().toString()

        // Ovo vraća LoginRes
        val res = api.loginOrRegisterWithGoogle(
            name = name,
            email = email,
            uid = uid
        )

        println("🔑 AuthRepository.loginWithEmail: LoginRes received - token=${res.token.take(20)}..., user=${res.user?.email}, error=${res.error}")

        // Check for errors
        if (res.error != null) {
            throw RuntimeException(res.error)
        }

        // Check if user exists in response
        val user = res.user ?: throw RuntimeException("No user data in login response")

        // Spremi token → backendov JWT, ne user.id
        if (res.token.isNotBlank()) {
            tokenStore.setToken(res.token)
            println("🔑 AuthRepository: Token saved! Token = ${res.token.take(20)}...")
            println("🔑 AuthRepository: Verifying token was saved: ${tokenStore.getToken()?.take(20)}...")
        } else {
            throw RuntimeException("No token in login response")
        }

        // Vrati ApiUser iz LoginRes.user
        return user.copy(
            uid = uid,
            displayName = name
        )
    }
}
