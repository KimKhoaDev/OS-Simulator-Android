plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.kimkhoa.ossim"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.kimkhoa.ossim"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0.0"
    }
    buildTypes {
        release { isMinifyEnabled = false }
    }
    buildFeatures { compose = true }
    packaging { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.compose.foundation)
    implementation(libs.compose.ui)
    implementation(libs.compose.animation)
    implementation(libs.backdrop)
    implementation(libs.shapes)
    implementation(libs.miuix.ui)
}
