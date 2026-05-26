# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# AdMob
-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.ads.** { *; }

# MPAndroidChart
-keep class com.github.mikephil.charting.** { *; }
-dontwarn com.github.mikephil.charting.**

# Support Library / AndroidX
-keep class androidx.preference.Preference { *; }
-keep class androidx.preference.PreferenceFragmentCompat { *; }
-keep class androidx.appcompat.widget.** { *; }

# Keep model classes
-keep class com.denzo.mypomodoro.database.** { *; }
-keep class com.denzo.mypomodoro.statistics.** { *; }
-keep class com.denzo.mypomodoro.Tasks.DataModel { *; }
