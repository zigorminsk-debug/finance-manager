plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("kotlin-parcelize")
}

android {
    namespace = "com.byfinancemanager"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.byfinancemanager"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    // Постоянный ключ подписи для всех сборок (исправлено падение + подпись)
    signingConfigs {
        create("release") {
            val keystoreFile = file("release.keystore")
            if (keystoreFile.exists()) {
                storeFile = keystoreFile
                storePassword = "byfinance123"
                keyAlias = "byfinance"
                keyPassword = "byfinance123"
                // PKCS12 type for openssl-generated keystore
                // storeType = "PKCS12" // autodetected
                println("Using permanent release keystore: ${keystoreFile.absolutePath}, exists=${keystoreFile.exists()}, size=${keystoreFile.length()}")
            } else {
                println("WARNING: release.keystore not found at ${keystoreFile.absolutePath}, using debug keystore as fallback")
                // Fallback to debug keystore to avoid build failure
                storeFile = file("debug.keystore")
                storePassword = "android"
                keyAlias = "androiddebugkey"
                keyPassword = "android"
            }
            // Allow override via env for CI
            storePassword = System.getenv("KEYSTORE_PASSWORD") ?: storePassword
            keyPassword = System.getenv("KEY_PASSWORD") ?: keyPassword
            keyAlias = System.getenv("KEY_ALIAS") ?: keyAlias
        }
        getByName("debug") {
            val keystoreFile = file("release.keystore")
            if (keystoreFile.exists()) {
                // Подписываем debug тем же постоянным ключом для консистентности
                storeFile = keystoreFile
                storePassword = "byfinance123"
                keyAlias = "byfinance"
                keyPassword = "byfinance123"
                println("Using permanent keystore for DEBUG: ${keystoreFile.absolutePath}")
            } else {
                println("Using default debug keystore")
            }
            storePassword = System.getenv("KEYSTORE_PASSWORD") ?: storePassword
            keyPassword = System.getenv("KEY_PASSWORD") ?: keyPassword
            keyAlias = System.getenv("KEY_ALIAS") ?: keyAlias
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
        debug {
            isDebuggable = true
            signingConfig = signingConfigs.getByName("debug")
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
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    
    // Compose BOM
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3:1.1.2")
    implementation("androidx.compose.material:material-icons-extended")
    
    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.6")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    
    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    
    // Datastore
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    
    // Networking - NBRB API
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.google.code.gson:gson:2.10.1")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    
    // Date time
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")
    
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.02.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}
