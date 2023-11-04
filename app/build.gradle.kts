plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    compileSdk = 34
    packagingOptions {
        jniLibs {
            useLegacyPackaging = true
        }
    }
    defaultConfig {
        applicationId = "git.artdeell.skymodloader"
        minSdk = 25
        targetSdk = 34
        versionCode = 25
        versionName = "1.4.3"
        manifestPlaceholders["appAuthRedirectScheme"] = "com.googleusercontent.apps.425067885496-t5lthegcq17g1gaco1l90cc3cncr0q0l"

        externalNativeBuild {
            cmake {
                cppFlags("-std=c++17")
                abiFilters("arm64-v8a")
                targets("ciphered")
                arguments("-DANDROID_ARM_NEON=TRUE", "-DANDROID_STL=c++_shared")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("debug")
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
    externalNativeBuild {
        cmake {
            path(file("src/main/cpp/CMakeLists.txt"))
            version = "3.22.1"
        }
    }
    buildFeatures { // start gradle 8.0
        buildConfig = true
        aidl = true
        dataBinding = true
    }
    ndkVersion = "26.0.10792818"
    namespace = "git.artdeell.skymodloader"
}

dependencies {
    implementation("com.google.android.material:material:1.4.0")
    implementation("net.openid:appauth:0.11.1")
    implementation("net.fornwall:jelf:0.7.0")
    implementation("androidx.localbroadcastmanager:localbroadcastmanager:1.1.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.zxing:core:3.3.3")
    implementation("androidx.recyclerview:recyclerview:1.3.1")
    implementation("io.noties.markwon:core:4.6.2")
}