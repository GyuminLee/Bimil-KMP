import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    jvm("desktop")

    sourceSets {
        val desktopMain by getting {
            dependencies {
                implementation(project(":shared"))
                implementation(compose.desktop.currentOs)
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(libs.koin.core)
                implementation(libs.kotlinx.coroutines.core)
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.8.1")
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.imbavchenko.bimil.desktop.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "Bimil"
            packageVersion = "1.0.0"
            description = "Password Hint Manager"
            copyright = "© 2024 Bimil"
            vendor = "Bimil"

            windows {
                menuGroup = "Bimil"
                upgradeUuid = "e4b5c6d7-8a9b-0c1d-2e3f-4a5b6c7d8e9f"
            }
        }
    }
}
