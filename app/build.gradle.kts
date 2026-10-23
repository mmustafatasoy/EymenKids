plugins { id("com.android.application") }
android {
  namespace = "com.eymen.kids"
  compileSdk = 36
  androidResources { noCompress += listOf("mp4", "m4v", "webm") }
  defaultConfig {
    applicationId = "com.eymen.kids"
    minSdk = 24
    targetSdk = 35
    versionCode = 4
    versionName = "1.3.0"
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }
}
dependencies {
  implementation("androidx.webkit:webkit:1.12.1")
  implementation("androidx.media3:media3-exoplayer:1.11.1")
  implementation("androidx.media3:media3-ui:1.11.1")
}
