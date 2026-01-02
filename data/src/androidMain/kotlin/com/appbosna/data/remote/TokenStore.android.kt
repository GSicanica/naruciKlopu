package com.appbosna.data.remote

import TokenStore
import android.content.Context
import android.content.SharedPreferences

class AndroidTokenStore(private val context: Context) : TokenStore {

    companion object {
        private const val PREFS_NAME = "auth_prefs"
        private const val KEY_TOKEN = "auth_token"
    }

    private val prefs: SharedPreferences
        get() = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun getToken(): String? {
        return prefs.getString(KEY_TOKEN, null)
    }

    override fun setToken(token: String?) {
        with(prefs.edit()) {
            if (token == null) remove(KEY_TOKEN)
            else putString(KEY_TOKEN, token)
            apply()
        }
    }
}
