package com.imbavchenko.bimil.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class Screen {
    @Serializable
    data object Home : Screen()

    @Serializable
    data class AccountDetail(val accountId: String) : Screen()

    @Serializable
    data class AddEditAccount(val accountId: String? = null) : Screen()

    @Serializable
    data object Settings : Screen()

    @Serializable
    data object Categories : Screen()

    @Serializable
    data object Backup : Screen()

    @Serializable
    data object Lock : Screen()
}
