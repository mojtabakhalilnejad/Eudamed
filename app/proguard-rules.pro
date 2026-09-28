# Keep kotlinx.serialization models (needed because reflection-free serializers are generated at compile time,
# but R8 can still strip them if it doesn't see the usage through Retrofit).
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep,includedescriptorclasses class com.openregulatory.eudamedsearch.**$$serializer { *; }
-keepclassmembers class com.openregulatory.eudamedsearch.** {
    *** Companion;
}
-keepclasseswithmembers class com.openregulatory.eudamedsearch.** {
    kotlinx.serialization.KSerializer serializer(...);
}
