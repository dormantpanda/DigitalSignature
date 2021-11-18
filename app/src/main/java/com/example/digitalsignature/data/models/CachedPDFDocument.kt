package com.example.digitalsignature.data.models

data class CachedPDFDocument (
    val name: String,
    val contentBA: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CachedPDFDocument

        if (name != other.name) return false
        if (!contentBA.contentEquals(other.contentBA)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + contentBA.contentHashCode()
        return result
    }
}