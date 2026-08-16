package com.example.utils

import java.security.SecureRandom

object UserIdGenerator {
    private val CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray()
    private val random = SecureRandom()

    /**
     * Generates a unique random User ID in the format "APK18-XXXXXX"
     * Example: "APK18-A7K92P"
     */
    fun generate(): String {
        val sb = StringBuilder("APK18-")
        for (i in 0 until 6) {
            val idx = random.nextInt(CHARS.size)
            sb.append(CHARS[idx])
        }
        return sb.toString()
    }
}
