# Keep Room entities and DAOs so R8 doesn't strip them
-keep class com.example.oroiapp.model.** { *; }
-keep interface com.example.oroiapp.data.**Dao { *; }
-keep class com.example.oroiapp.data.AppDatabase { *; }

# Keep WorkManager workers (referenced by class name at runtime)
-keep class com.example.oroiapp.worker.** { *; }

# Keep Kotlin coroutines internals
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** { volatile <fields>; }

# Keep annotations and signatures needed by Room and Compose
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod

# Preserve line numbers in stack traces for crash debugging
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
