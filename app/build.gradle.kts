plugins {
    id("com.android.application")
}

android {
    namespace = "com.chinalwb.are.demo"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.chinalwb.are.demo"
        minSdk = 21
        targetSdk = 36
        versionCode = 2
        versionName = "2.0.0"
    }

    buildFeatures {
        viewBinding = true
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.txt"
            )
        }
    }
}

dependencies {
    implementation(project(":are"))
    implementation("androidx.constraintlayout:constraintlayout:1.3.0")
    implementation("com.google.android.material:material:1.14.0-alpha10")
    implementation("com.github.bumptech.glide:glide:5.0.5")
}