package com.imbavchenko.bimil.presentation.localization

enum class Language(val code: String, val displayName: String, val nativeName: String) {
    ENGLISH("en", "English", "English"),
    KOREAN("ko", "Korean", "한국어"),
    JAPANESE("ja", "Japanese", "日本語"),
    CHINESE("zh", "Chinese", "中文"),
    GERMAN("de", "German", "Deutsch");

    companion object {
        fun fromCode(code: String): Language {
            return entries.find { it.code == code } ?: ENGLISH
        }
    }
}
