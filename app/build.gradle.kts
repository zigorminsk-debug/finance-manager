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

    // Постоянный ключ подписи для всех сборок
    signingConfigs {
        create("release") {
            // Читаем из keystore.properties если есть, иначе используем дефолтные значения
            val keystorePropertiesFile = rootProject.file("app/keystore.properties")
            val keystoreProperties = java.util.Properties()
            if (keystorePropertiesFile.exists()) {
                keystoreProperties.load(java.io.FileInputStream(keystorePropertiesFile))
            }
            val storeFilePath = keystoreProperties.getProperty("storeFile") ?: "release.keystore"
            val storeFileObj = if (java.io.File(storeFilePath).isAbsolute) {
                java.io.File(storeFilePath)
            } else {
                // Относительно app директории
                java.io.File(projectDir, storeFilePath)
            }
            // Fallback to file in app/ if not found
            val finalStoreFile = if (storeFileObj.exists()) storeFileObj else java.io.File(projectDir, "release.keystore")
            
            storeFile = finalStoreFile
            storePassword = keystoreProperties.getProperty("storePassword") ?: "byfinance123"
            keyAlias = keystoreProperties.getProperty("keyAlias") ?: "byfinance"
            keyPassword = keystoreProperties.getProperty("keyPassword") ?: "byfinance123"
            
            // Для GitHub Actions - можно переопределить через env переменные
            storePassword = System.getenv("KEYSTORE_PASSWORD") ?: storePassword
            keyPassword = System.getenv("KEY_PASSWORD") ?: keyPassword
        }
        // Debug тоже подписываем релизным ключом для постоянства
        getByName("debug") {
            val keystorePropertiesFile = rootProject.file("app/keystore.properties")
            val keystoreProperties = java.util.Properties()
            if (keystorePropertiesFile.exists()) {
                keystoreProperties.load(java.io.FileInputStream(keystorePropertiesFile))
            }
            val storeFilePath = keystoreProperties.getProperty("storeFile") ?: "release.keystore"
            val storeFileObj = if (java.io.File(storeFilePath).isAbsolute) {
                java.io.File(storeFilePath)
            } else {
                java.io.File(projectDir, storeFilePath)
            }
            val finalStoreFile = if (storeFileObj.exists()) storeFileObj else java.io.File(projectDir, "release.keystore")
            
            storeFile = finalStoreFile
            storePassword = keystoreProperties.getProperty("storePassword") ?: "byfinance123"
            keyAlias = keystoreProperties.getProperty("keyAlias") ?: "byfinance"
            keyPassword = keystoreProperties.getProperty("keyPassword") ?: "byfinance123"
            storePassword = System.getenv("KEYSTORE_PASSWORD") ?: storePassword
            keyPassword = System.getenv("KEY_PASSWORD") ?: keyPassword
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
