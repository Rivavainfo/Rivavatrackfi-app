package com.rivavafi.universal.data.model

data class UserModel(
    val uid: String = "",
    val name: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val preference: String? = null,
    val profileImage: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val lastLoginAt: Long = System.currentTimeMillis(),
    val loginProvider: String = "",
    val isPhoneVerified: Boolean = false
)
