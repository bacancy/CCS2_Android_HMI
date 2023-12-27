package com.bacancy.ccs2androidhmi.models

data class GunStatesInfo(
    val id: Int,
    val gunStateLabel: String,
    val gunStateValue: String,
    val gunStateMode: String, //White, Red, Yellow, Green, Blue
)
