package com.imbavchenko.bimil.data.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.imbavchenko.bimil.db.BimilDatabase

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(BimilDatabase.Schema, "bimil.db")
    }
}
