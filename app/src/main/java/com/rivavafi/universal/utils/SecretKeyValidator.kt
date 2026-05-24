package com.rivavafi.universal.utils

object SecretKeyValidator {
    fun isValid(key: String): Boolean {
        val pattern = Regex("^rivrubi@[0-9]{5}$")
        return pattern.matches(key)
    }
}
