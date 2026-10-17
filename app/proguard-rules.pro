# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Preserve line number information for debugging stack traces.
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception

# Aggressive optimizations
-allowaccessmodification
-repackageclasses ''

# =================================================================
#  Google Play Services & Firebase
# =================================================================
-keep class com.google.android.gms.** { *; }
-keep class * extends java.lang.annotation.Annotation { *; }
-keepattributes Signature
-keepnames class com.google.firebase.database.** { *; }

# =================================================================
#  Retrofit & OkHttp
# =================================================================
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions

-dontwarn okhttp3.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okio.**
-keep class okio.** { *; }

# =================================================================
#  Gson & DTO (Data Transfer Object)
# =================================================================
# Keep your DTO classes to ensure reflection-based parsing works correctly
-keep class com.tktkcompany.kakoRaceKeiba.dto.** { *; }
-keepclassmembers class com.tktkcompany.kakoRaceKeiba.dto.** { *; }
-keepattributes *Annotation*

# =================================================================
#  ThreeTenABP (Date/Time Backport)
# =================================================================
-keep class org.threeten.bp.** { *; }
-dontwarn org.threeten.bp.**

# =================================================================
#  Jsoup (HTML Parser)
# =================================================================
-keep class org.jsoup.** { *; }
-dontwarn org.jsoup.**

# =================================================================
#  MPAndroidChart
# =================================================================
-keep class com.github.mikephil.charting.** { *; }
-dontwarn com.github.mikephil.charting.**
-keep public class * extends com.github.mikephil.charting.charts.Chart {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# =================================================================
#  Android Core
# =================================================================
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

# WorkManager（通知の予約に使う）。-repackageclasses / -allowaccessmodification と組み合わさると
# 内部のデータベース（Room）の生成用コンストラクタが消えて起動時に落ちるので残す
-keep class * extends androidx.room.RoomDatabase { <init>(); }
-keep class androidx.work.impl.WorkDatabase_Impl { *; }
# Worker はクラス名からリフレクションで作られる
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
