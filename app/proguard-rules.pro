# リリース版の R8（縮小・最適化・難読化）の規則。
# ライブラリ（Google Play 開発者サービス・Firebase・OkHttp・Retrofit・kotlinx.serialization など）は
# 必要な規則を自分で同梱しているので、ここではパッケージ丸ごとの -keep はしない。
# 以前は com.google.android.gms.** などを丸ごと残していて、コードの 7 割以上が最適化・難読化されず、
# Google Play から「DEX コードの最適化がしきい値を下回っています」と警告された（2026/10/18）。

# クラッシュログで行番号を読めるようにする（難読化の対応表 mapping.txt は AAB に入る）
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
-keepattributes Signature,Exceptions,*Annotation*,InnerClasses,EnclosingMethod

-allowaccessmodification
-repackageclasses ''

# Firebase Realtime Database で読み書きするクラス（フィールド名がそのままキーになる）
-keep class com.tktkcompany.kakoRaceKeiba.dto.** { *; }

# WorkManager（通知の予約に使う）。内部のデータベース（Room）の生成用コンストラクタが消えると起動時に落ちる
-keep class * extends androidx.room.RoomDatabase { <init>(); }
-keep class androidx.work.impl.WorkDatabase_Impl { *; }
# Worker はクラス名からリフレクションで作られる
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# グラフ（MPAndroidChart）はレイアウトの XML から作られる
-keep public class * extends com.github.mikephil.charting.charts.Chart {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-dontwarn com.github.mikephil.charting.**

-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

# 規則を同梱していないライブラリの警告を抑える（動作には関係しない）
-dontwarn org.threeten.bp.**
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
