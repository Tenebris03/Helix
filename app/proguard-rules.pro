# Keep all Room generated code
-keep class * extends androidx.room.RoomDatabase
-keep class * extends androidx.room.Dao
-keep class * extends androidx.room.Entity
-keep class **_Impl { *; }

# Keep Room internal implementation classes
-keep class androidx.room.** { *; }
-keep class androidx.sqlite.** { *; }

# Keep Room's identity hash
-keepclassmembers class * extends androidx.room.RoomDatabase {
    static java.lang.String getIdentityHash();
}

# Keep our data models
-keep class com.tenebris.health_tracker.data.model.** { *; }

# Keep Koin for Dependency Injection
-keep class org.koin.** { *; }
-keep class io.insertkoin.** { *; }

# Keep Lifecycle and ViewModels for DI
-keep class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# Keep Kotlin Serialization and its metadata
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod
-keep class kotlinx.serialization.** { *; }
-keep @kotlinx.serialization.Serializable class * { *; }
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}
-keepclassmembers class * {
    *** Companion;
    *** serializer();
}

# Keep all ML Kit and GMS classes
-keep class com.google.mlkit.** { *; }
-keep class com.google.android.gms.** { *; }
-keep class com.google.firebase.** { *; }
-keep class com.google.android.libraries.** { *; }

# Keep TensorFlow Lite
-keep class org.tensorflow.lite.** { *; }

# ML Kit common
-keep class com.google.android.libraries.barhopper.** { *; }
-keep class com.google.android.gms.internal.mlkit_common.** { *; }
-keep class com.google.android.gms.internal.mlkit_vision_label_bundled.** { *; }

# Keep all static initializers and members for ML Kit
-keepclassmembers class com.google.mlkit.** {
    public static <fields>;
    public static <methods>;
}

# Keep our remote data transfer objects
-keep class com.tenebris.health_tracker.data.remote.** { *; }

# Keep Reflection metadata
-keepattributes Signature, EnclosingMethod, InnerClasses, *Annotation*
-keep class kotlin.reflect.** { *; }

# Additional Room safety
-keep class androidx.room.RoomOpenHelper { *; }
-keep class androidx.room.RoomDatabase$Builder { *; }
