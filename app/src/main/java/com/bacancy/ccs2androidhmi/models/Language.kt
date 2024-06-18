package com.bacancy.ccs2androidhmi.models

data class Language(
    val code: String = "en",
    val name: String = "English",
    val defaultName: String = "English",
    var isSelected: Boolean = false
)
