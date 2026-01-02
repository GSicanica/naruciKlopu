package com.appbosna.data.remote

actual object ApiConfig {
    // Production API URL
    actual val baseUrl: String = "https://symfonycopilot.tmbv-hms.com/api"
    
    // For local development, use:
    // actual val baseUrl: String = "http://10.0.2.2:8000/api"  // Android Emulator
    // actual val baseUrl: String = "http://192.168.0.X:8000/api"  // Physical device
}
