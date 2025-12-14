package com.imbavchenko.bimil.data.database

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.imbavchenko.bimil.db.BimilDatabase

actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(
            schema = BimilDatabase.Schema,
            context = context,
            name = "bimil.db"
        )
    }
}
