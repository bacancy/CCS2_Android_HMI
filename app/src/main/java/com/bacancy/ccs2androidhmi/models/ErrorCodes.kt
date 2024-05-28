package com.bacancy.ccs2androidhmi.models

data class ErrorCodes(
    val id: Int? = null,
    val errorCodeName: String,
    val errorCodeSource: String,
    val errorCodeStatus: String,
    val errorCodeValue: Int,
    val errorCodeDateTime: String,
)
