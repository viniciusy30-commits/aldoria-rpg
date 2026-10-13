plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.aldoria.rpg"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.aldoria.rpg"
        minSdk = 24
        targetSdk = 34
        // o número da versão sobe sozinho a cada build do GitHub (é ele que o app compara para atualizar)
        val run = (System.getenv("GITHUB_RUN_NUMBER") ?: "1").toIntOrNull() ?: 1
        versionCode = run
        versionName = "1.$run"
    }

    // chave fixa: todo APK sai assinado igual, então dá para instalar por cima sem perder o progresso
    signingConfigs {
        getByName("debug") {
            storeFile = file("aldoria.keystore")
            storePassword = "aldoria123"
            keyAlias = "aldoria"
            keyPassword = "aldoria123"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
}
