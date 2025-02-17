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

# Keep kode yang digunakan dalam refleksi
-keepattributes *Annotation*

# Keep model class dan Retrofit
-keep class id.hash.tirayin.model.** { *; }
-keep class retrofit2.** { *; }

# Menghapus Log dan Debug Information
-assumenosideeffects class android.util.Log { *; }

# 🚀 Hindari penghapusan kelas yang digunakan dalam refleksi
-keepattributes *Annotation*
-keep class * {
    @androidx.annotation.Keep *;
}

# 🚀 Keep Apache POI (untuk Excel)
-keep class org.apache.poi.** { *; }
-dontwarn org.apache.poi.**

# 🚀 Keep Retrofit & Gson (Jika digunakan)
-keep class com.google.gson.** { *; }
-keep class retrofit2.** { *; }
-dontwarn com.google.gson.**

# 🚀 Keep UCrop (Untuk Image Cropping)
-keep class com.yalantis.ucrop.** { *; }
-dontwarn com.yalantis.ucrop.**

# 🚀 Keep Coil (Untuk Image Loading)
-keep class coil.** { *; }
-dontwarn coil.**

# 🚀 Hindari menghapus method yang digunakan dalam Native Code
-keepclassmembers class * {
    native <methods>;
}

# 🚀 Hindari penghapusan log
-assumenosideeffects class android.util.Log { *; }

# Tambahkan aturan untuk menghindari warning pada library yang tidak digunakan
-dontwarn com.github.javaparser.**
-dontwarn com.github.luben.zstd.**
-dontwarn java.awt.**
-dontwarn javax.xml.stream.**
-dontwarn net.sf.saxon.**
-dontwarn org.apache.maven.**
-dontwarn org.apache.tools.**
-dontwarn org.bouncycastle.**
-dontwarn org.brotli.dec.**
-dontwarn org.conscrypt.**
-dontwarn org.objectweb.asm.**
-dontwarn org.openjsse.**
-dontwarn org.osgi.framework.**
-dontwarn org.tukaani.xz.**


