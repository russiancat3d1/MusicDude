dependencies {
    // 1. Jetpack Compose (UI & Navigation)
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3") // Includes Material You
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // 2. AndroidX Media3 (ExoPlayer & Background Playback)
    val media3Version = "1.2.1"
    implementation("androidx.media3:media3-exoplayer:$media3Version")
    implementation("androidx.media3:media3-session:$media3Version")
    implementation("androidx.media3:media3-common:$media3Version")

    // 3. Room Database (For saving custom Playlists)
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    // Note: Make sure to add id("com.google.devtools.ksp") to your plugins at the top
    // ksp("androidx.room:room-compiler:$roomVersion") 

    // 4. Image Loading (Coil is perfect for Compose & loading local album art)
    implementation("io.coil-kt:coil-compose:2.6.0")
    
    // Core Android libs
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
}