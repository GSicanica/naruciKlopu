package com.appbosna.shared.util

actual fun threadSleep(millis: Long) {
    Thread.sleep(millis)
}