package com.imbavchenko.bimil.data.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.imbavchenko.bimil.db.BimilDatabase
import java.io.File

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        val appDir = getAppDataDirectory()
        appDir.mkdirs()
        val dbFile = File(appDir, "bimil.db")
        val dbPath = dbFile.absolutePath
        val isNewDatabase = !dbFile.exists()

        val driver = JdbcSqliteDriver("jdbc:sqlite:$dbPath")
        if (isNewDatabase) {
            BimilDatabase.Schema.create(driver)
        }
        return driver
    }

    private fun getAppDataDirectory(): File {
        val os = System.getProperty("os.name").lowercase()
        val userHome = System.getProperty("user.home")

        return when {
            os.contains("win") -> {
                val appData = System.getenv("APPDATA")
                if (appData != null) {
                    File(appData, "Bimil")
                } else {
                    File(File(File(userHome, "AppData"), "Roaming"), "Bimil")
                }
            }
            os.contains("mac") -> File("$userHome/Library/Application Support", "Bimil")
            else -> File("$userHome/.local/share", "bimil")
        }
    }
}
