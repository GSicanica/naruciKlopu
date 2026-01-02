package com.appbosna.data.remote

/**
 * Platform-independent provider za čitanje bajtova iz fajla.
 * Više se ne koristi Firebase File – samo obična putanja (String).
 */
interface FileBytesProvider {
    suspend fun fileName(path: String): String
    suspend fun bytes(path: String): ByteArray
}
