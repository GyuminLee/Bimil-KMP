package com.imbavchenko.bimil.data.encryption

expect class HashService() {
    fun generateSalt(): String
    fun hashPin(pin: String, salt: String): String
}
