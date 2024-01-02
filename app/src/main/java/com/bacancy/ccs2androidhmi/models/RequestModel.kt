package com.bacancy.ccs2androidhmi.models

data class RequestModel(
    var id: Int,
    var requestFrame: ByteArray,
    var responseSize: Int
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RequestModel

        if (id != other.id) return false
        if (!requestFrame.contentEquals(other.requestFrame)) return false
        if (responseSize != other.responseSize) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + requestFrame.contentHashCode()
        result = 31 * result + responseSize
        return result
    }
}
