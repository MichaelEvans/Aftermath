-keep class **$$Aftermath { *; }
-keep class org.michaelevans.aftermath.** { *; }
-keepclasseswithmembers class * {
    @org.michaelevans.aftermath.* <fields>;
}
-keepclasseswithmembers class * {
    @org.michaelevans.aftermath.* <methods>;
}