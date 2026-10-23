plugins { id("com.android.application") }
android { namespace = "com.eymen.kids"; compileSdk = 35
 defaultConfig { applicationId = "com.eymen.kids"; minSdk = 24; targetSdk = 35; versionCode = 2; versionName = "1.1.0" } }
dependencies { implementation("androidx.webkit:webkit:1.12.1") }
