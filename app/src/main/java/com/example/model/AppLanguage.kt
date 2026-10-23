package com.example.model

enum class AppLanguage(
    val code: String,
    val nativeName: String,
    val englishName: String,
    val flag: String,
    val shortLabel: String
) {
    ENGLISH(
        code = "en",
        nativeName = "English",
        englishName = "English",
        flag = "🇬🇧",
        shortLabel = "EN"
    ),
    JUBA_ARABIC(
        code = "ar_juba",
        nativeName = "عربي جوبا (Juba Arabic)",
        englishName = "Juba Arabic",
        flag = "🇸🇸",
        shortLabel = "عربي"
    ),
    DINKA(
        code = "din",
        nativeName = "Thuɔŋjäŋ (Dinka)",
        englishName = "Dinka",
        flag = "🇸🇸",
        shortLabel = "DNK"
    ),
    NUER(
        code = "nus",
        nativeName = "Thok Naath (Nuer)",
        englishName = "Nuer",
        flag = "🇸🇸",
        shortLabel = "NR"
    )
}
