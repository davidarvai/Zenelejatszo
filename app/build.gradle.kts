plugins {
    id("com.android.application")
}

android {
    namespace = "com.tonevellah.player"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.tonevellah.player"
        minSdk = 21
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    //recyclerview animators
    implementation ("jp.wasabeef:recyclerview-animators:4.0.2")

    implementation ("com.google.android.exoplayer:exoplayer:2.19.0")

    implementation ("de.hdodenhof:circleimageview:3.1.0")

    implementation ("io.github.gautamchibde:audiovisualizer:2.2.5")

    implementation ("androidx.palette:palette:1.0.0")

    implementation ("com.github.jgabrielfreitas:BlurImageView:1.0.1")

    implementation ("androidx.media3:media3-exoplayer:1.1.0")

}