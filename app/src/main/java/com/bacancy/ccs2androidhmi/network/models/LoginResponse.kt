package com.bacancy.ccs2androidhmi.network.models

data class LoginResponse(
    val error: Boolean,
    val statusCode: Int,
    val message: String,
    val data: LoginData
)