# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
-keep class com.google.android.gms.** { *; }
-keep class * extends java.lang.annotation.Annotation { *; }


```proguard
# =================================================================
#  Retrofit & OkHttp
#  RetrofitとOkHttpが内部的に使用するクラスを保持するためのルール
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
#  Gsonがリフレクションで利用するデータクラスを保持するためのルール
# =================================================================

# --- あなたのアプリのDTOパッケージを指定 ---
# ★★★ "com.tktkcompany.kakoRaceKeiba.dto" の部分は、
# あなたのプロジェクトのパッケージ名に合わせてください ★★★
-keep class com.tktkcompany.kakoRaceKeiba.dto.** { *; }

# シリアライズ/デシリアライズ対象のクラスとそのメンバーを保持
-keepclassmembers class com.tktkcompany.kakoRaceKeiba.dto.** { *; }

# もしDTOクラスに @SerializedName アノテーションを使っている場合は、
# 以下のルールも追加するとより安全です。
-keepattributes *Annotation*


# =================================================================
#  (おまけ) Firebaseなど他のライブラリを使用している場合の一般的なルール
# =================================================================
# Firebase
-keepattributes Signature
-keepnames class com.google.firebase.database.** { *; }

# Parcelable（Androidのデータ運搬用インターフェース）を実装したクラス
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}