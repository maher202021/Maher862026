# Add project specific ProGuard rules here.
# By default, the ProGuard rules in this file are appended to the default ProGuard
# rules for the Android toolchain.
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep Room generated code
-keep class * extends androidx.room.RoomDatabase
-keep class * implements androidx.room.RoomOpenHelper

# Keep kotlinx serialization classes
-keepclassmembers class * {
    *** Companion;
    *** serializer(...);
}
