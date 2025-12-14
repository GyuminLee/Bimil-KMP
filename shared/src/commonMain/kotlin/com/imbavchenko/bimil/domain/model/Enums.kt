package com.imbavchenko.bimil.domain.model

enum class Theme {
    LIGHT, DARK, SYSTEM
}

enum class LoginType {
    SSO, PASSWORD
}

enum class RequirementStatus {
    YES, NO, UNKNOWN
}

enum class Region(val displayName: String, val flagEmoji: String) {
    GLOBAL("Global", "🌍"),
    KR("Korea", "🇰🇷"),
    JP("Japan", "🇯🇵"),
    CN("China", "🇨🇳"),
    US("United States", "🇺🇸"),
    EU("Europe", "🇪🇺"),
    RU("Russia", "🇷🇺"),
    IN("India", "🇮🇳"),
    SEA("Southeast Asia", "🌏"),
    LATAM("Latin America", "🌎")
}

enum class SsoProvider(val displayName: String, val iconName: String) {
    // Global
    GOOGLE("Google", "google"),
    APPLE("Apple", "apple"),
    FACEBOOK("Facebook", "facebook"),
    MICROSOFT("Microsoft", "microsoft"),
    GITHUB("GitHub", "github"),
    TWITTER("Twitter/X", "twitter"),
    LINKEDIN("LinkedIn", "linkedin"),
    AMAZON("Amazon", "amazon"),

    // Korea
    KAKAO("Kakao", "kakao"),
    NAVER("Naver", "naver"),
    TOSS("Toss", "toss"),

    // Japan
    LINE("LINE", "line"),
    YAHOO_JAPAN("Yahoo! Japan", "yahoo"),
    RAKUTEN("Rakuten", "rakuten"),

    // China
    WECHAT("WeChat", "wechat"),
    ALIPAY("Alipay", "alipay"),
    QQ("QQ", "qq"),
    WEIBO("Weibo", "weibo"),

    // Russia
    VK("VK", "vk"),
    YANDEX("Yandex", "yandex"),
    MAILRU("Mail.ru", "mailru"),

    // Other
    CUSTOM("Other", "other");

    companion object {
        fun getByRegion(region: Region): List<SsoProvider> {
            val global = listOf(GOOGLE, APPLE, FACEBOOK, MICROSOFT, GITHUB)
            return when (region) {
                Region.GLOBAL -> global
                Region.KR -> global + listOf(KAKAO, NAVER, TOSS)
                Region.JP -> global + listOf(LINE, YAHOO_JAPAN, RAKUTEN)
                Region.CN -> listOf(WECHAT, ALIPAY, QQ, WEIBO)
                Region.US, Region.EU -> global + listOf(TWITTER, LINKEDIN, AMAZON)
                Region.RU -> global + listOf(VK, YANDEX, MAILRU)
                Region.IN -> global + listOf(TWITTER, LINKEDIN)
                Region.SEA -> global + listOf(LINE)
                Region.LATAM -> global + listOf(TWITTER)
            }
        }
    }
}

enum class SortOrder {
    NAME_ASC,
    NAME_DESC,
    RECENT_MODIFIED,
    MOST_VIEWED
}
