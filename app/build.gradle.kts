plugins { id("com.android.application") }
android { namespace = "com.eymen.kids"; compileSdk = 35
 androidResources { noCompress += listOf("mp4", "m4v", "webm") }
 defaultConfig { applicationId = "com.eymen.kids"; minSdk = 24; targetSdk = 35; versionCode = 3; versionName = "1.2.0" } }
dependencies { implementation("androidx.webkit:webkit:1.12.1") }
