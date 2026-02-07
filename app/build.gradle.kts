plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
}

import java.util.Properties
import java.io.FileInputStream

android {
    namespace = "com.skul9x.danhvan"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.skul9x.danhvan"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    // --- BẮT ĐẦU PHẦN THÊM MỚI ---
    signingConfigs {
        create("release") {
            // Load keystore info from local.properties (not committed to version control)
            val localProperties = Properties()
            val localFile = rootProject.file("local.properties")
            if (localFile.exists()) {
                localFile.inputStream().use { localProperties.load(it) }
            }

            // Defaults or load from properties
            val storeFileName = localProperties.getProperty("storeFile")
            val storePwd = localProperties.getProperty("storePassword")
            val keyPwd = localProperties.getProperty("keyPassword")
            val keyAliasName = localProperties.getProperty("keyAlias")

            if (storeFileName != null && storePwd != null && keyPwd != null && keyAliasName != null) {
                storeFile = file(storeFileName)
                storePassword = storePwd
                keyPassword = keyPwd
                keyAlias = keyAliasName
            }
        }
    }
    // --- KẾT THÚC PHẦN THÊM MỚI ---

    buildTypes {
        release {
            // Dòng này để áp dụng cấu hình ký tên vào bản release
            signingConfig = signingConfigs.getByName("release")

            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    implementation("androidx.compose.material:material-icons-extended")

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Coil
    implementation(libs.coil.compose)

    // Gson
    implementation(libs.gson)

    // Material (XML)
    implementation(libs.material)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}