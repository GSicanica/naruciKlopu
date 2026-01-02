package com.appbosna.di

import TokenStore
import com.appbosna.data.remote.AndroidTokenStore
import com.appbosna.manage_product.PhotoPicker
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

actual val targetModule = module {
    single<TokenStore> { AndroidTokenStore(androidContext()) }
    single<PhotoPicker> { PhotoPicker() }
}