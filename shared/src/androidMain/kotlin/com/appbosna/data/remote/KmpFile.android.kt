package com.appbosna.data.remote

import android.content.Context
import android.net.Uri

// Android implementacija - držimo Uri i Context
actual class KmpFile(
    val uri: Uri,
    val context: Context
)

// Čitanje bajtova preko ContentResolver-a
actual fun readFileBytes(file: KmpFile): ByteArray {
    val input = file.context.contentResolver.openInputStream(file.uri)
        ?: return ByteArray(0)
    return input.use { it.readBytes() }
}

