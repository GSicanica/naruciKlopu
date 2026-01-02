

interface TokenStore {
    fun getToken(): String?
    fun setToken(token: String?)
}

expect fun createTokenStore(): TokenStore