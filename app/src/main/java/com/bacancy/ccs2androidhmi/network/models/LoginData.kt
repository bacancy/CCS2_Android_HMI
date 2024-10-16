package com.bacancy.ccs2androidhmi.network.models

data class LoginData(
    val accessToken: String,
    val refreshToken: String,
    val role: String
)