# Diary app ProGuard rules

# Keep Google API client classes
-keep class com.google.api.** { *; }
-keep class com.google.apis.** { *; }
-dontwarn com.google.api.**

# Keep Dropbox SDK classes
-keep class com.dropbox.** { *; }
-dontwarn com.dropbox.**

# Keep OkHttp
-dontwarn okhttp3.**
-keep class okhttp3.** { *; }

# Keep Gson
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
