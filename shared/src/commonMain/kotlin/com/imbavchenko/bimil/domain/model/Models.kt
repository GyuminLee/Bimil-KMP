package com.imbavchenko.bimil.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class AppSettings(
    val theme: Theme = Theme.SYSTEM,
    val language: String = "SYSTEM",
    val region: Region = Region.GLOBAL,
    val isPinEnabled: Boolean = false,
    val isBiometricEnabled: Boolean = false,
    val autoLockSeconds: Int = -1,
    val schemaVersion: Int = 1
)

@Serializable
data class Category(
    val id: String,
    val name: String,
    val color: String = "#6B7280",
    val icon: String = "folder",
    val isDefault: Boolean = false,
    val sortOrder: Int = 0,
    val createdAt: Long = 0
) {
    companion object {
        val DEFAULT_CATEGORIES = listOf(
            Category(id = "finance", name = "Finance", color = "#10B981", icon = "bank", isDefault = false, sortOrder = 0),
            Category(id = "sns", name = "SNS", color = "#3B82F6", icon = "share", isDefault = false, sortOrder = 1),
            Category(id = "shopping", name = "Shopping", color = "#F59E0B", icon = "cart", isDefault = false, sortOrder = 2),
            Category(id = "work", name = "Work", color = "#8B5CF6", icon = "briefcase", isDefault = false, sortOrder = 3),
            Category(id = "entertainment", name = "Entertainment", color = "#EC4899", icon = "play", isDefault = false, sortOrder = 4),
            Category(id = "other", name = "Other", color = "#6B7280", icon = "folder", isDefault = true, sortOrder = 5)
        )
    }
}

@Serializable
data class AccountEntry(
    val id: String,
    val serviceName: String,
    val username: String,
    val websiteUrl: String? = null,
    val loginType: LoginType,
    val categoryId: String,
    val isFavorite: Boolean = false,
    val memo: String? = null,
    val iconData: ByteArray? = null,
    val viewCount: Int = 0,
    val createdAt: Long = 0,
    val updatedAt: Long = 0,
    val lastViewedAt: Long? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AccountEntry) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}

@Serializable
data class SsoHint(
    val id: String,
    val accountId: String,
    val provider: SsoProvider,
    val providerCustom: String? = null,
    val createdAt: Long = 0
)

@Serializable
data class PasswordHint(
    val id: String,
    val accountId: String,
    val minLength: Int = 8,
    val maxLength: Int? = null,
    val requiresSpecial: RequirementStatus = RequirementStatus.UNKNOWN,
    val requiresUppercase: RequirementStatus = RequirementStatus.UNKNOWN,
    val requiresLowercase: RequirementStatus = RequirementStatus.UNKNOWN,
    val requiresNumber: RequirementStatus = RequirementStatus.UNKNOWN,
    val allowedSpecialChars: String? = null,
    val personalHint: String? = null,
    val createdAt: Long = 0,
    val updatedAt: Long = 0
)

data class AccountWithHint(
    val account: AccountEntry,
    val ssoHint: SsoHint? = null,
    val passwordHint: PasswordHint? = null,
    val category: Category? = null
)
