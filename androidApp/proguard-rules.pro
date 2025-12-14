# Bimil ProGuard Rules

# Keep Kotlin serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep application models
-keep class com.imbavchenko.bimil.domain.model.** { *; }
-keep class com.imbavchenko.bimil.data.backup.** { *; }

# SQLDelight
-keep class com.imbavchenko.bimil.db.** { *; }

# Koin
-keepnames class androidx.lifecycle.ViewModel
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
