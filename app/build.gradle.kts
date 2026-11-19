plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example.hr_employeemanagementsystem"
    compileSdk = 35
    ndkVersion = "28.0.12433566"

    defaultConfig {
        applicationId = "com.example.hr_employeemanagementsystem"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        externalNativeBuild {
            cmake {
                arguments("-DANDROID_SUPPORT_FLEXIBLE_PAGE_SIZES=ON")
                cppFlags("-Wl,-z,max-page-size=16384")
            }
        }
    }

    packaging {
        jniLibs {
            useLegacyPackaging = false
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.materialIconsExtended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Room DB
    implementation(libs.roomRuntime)
    implementation(libs.roomKtx)
    ksp(libs.roomCompiler)

    // DataStore
    implementation(libs.datastorePreferences)

    // CameraX
    implementation(libs.cameraCore)
    implementation(libs.cameraCamera2)
    implementation(libs.cameraLifecycle)
    implementation(libs.cameraView)

    // Location & Maps
    implementation(libs.playServicesLocation)
    implementation(libs.playServicesMaps)
    implementation(libs.mapsCompose)

    // Gson
    implementation(libs.gson)

    // TensorFlow Lite (Updated for 16 KB page size support)
    implementation(libs.tensorflowLite)
    implementation(libs.tensorflowLiteGpu)

    // ML Kit Face Detection
    implementation(libs.mlkitFaceDetection)

    // Splash Screen
    implementation(libs.coreSplashScreen)
    
    // Coil (Image Loading)
    implementation(libs.coilCompose)

    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}