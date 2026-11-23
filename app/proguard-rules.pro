# ProGuard / R8 Rules for Nexus Terminal

# Keep Kotlin reflection metadata
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Keep Model classes for serialization
-keep class com.nexus.terminal.data.models.** { *; }
-keep class com.nexus.terminal.core.project.ProjectModel { *; }
-keep class com.nexus.terminal.core.ai.AiMessage { *; }

# OkHttp rules
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }

# Material & AndroidX
-keep class com.google.android.material.** { *; }
