plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.diary"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.diary"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            // Google API client bundles duplicate HTTP transport classes
            excludes += "META-INF/DEPENDENCIES"
            excludes += "META-INF/INDEX.LIST"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.activity.compose)
    implementation(libs.navigation.compose)
    implementation(libs.lifecycle.viewmodel.compose)

    // Google auth + Drive
    implementation(libs.play.services.auth)           // AuthorizationClient
    implementation(libs.credentials)                  // Credential Manager
    implementation(libs.credentials.play.services.auth) // Credential Manager Play Services bridge
    implementation(libs.googleid)                     // GetSignInWithGoogleOption
    implementation(libs.google.api.client.android)
    implementation(libs.google.api.services.drive)

    // Dropbox
    implementation(libs.dropbox.core.sdk)
    implementation(libs.okhttp)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Custom Tabs (Dropbox OAuth browser flow)
    implementation(libs.androidx.browser)

    debugImplementation(libs.compose.ui.tooling)
}
