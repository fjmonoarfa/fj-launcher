plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.fj.launcher"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.fj.launcher"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    splits {
        abi {
            isEnable = true
            reset()
            include("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
            isUniversalApk = true
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
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.04.01")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

tasks.register("copyApkToRoot") {
    doNotTrackState("Copies APK to root directory")
    doLast {
        val apkDir = layout.buildDirectory.dir("outputs/apk/debug").get().asFile
        if (apkDir.exists()) {
            apkDir.listFiles()?.forEach { file ->
                if (file.extension == "apk") {
                    val destName = when {
                        file.name.contains("universal") -> "fj-launcher-universal.apk"
                        file.name.contains("arm64-v8a") -> "fj-launcher-arm64-v8a.apk"
                        file.name.contains("armeabi-v7a") -> "fj-launcher-armeabi-v7a.apk"
                        file.name.contains("x86_64") -> "fj-launcher-x86_64.apk"
                        file.name.contains("x86") -> "fj-launcher-x86.apk"
                        else -> "fj-launcher.apk"
                    }
                    val dest = File(rootProject.rootDir, destName)
                    file.copyTo(dest, overwrite = true)
                }
            }
            val universal = File(rootProject.rootDir, "fj-launcher-universal.apk")
            val defaultApk = File(rootProject.rootDir, "fj-launcher.apk")
            if (universal.exists()) {
                universal.copyTo(defaultApk, overwrite = true)
            }
        }
    }
}

afterEvaluate {
    tasks.findByName("assembleDebug")?.finalizedBy("copyApkToRoot")
}
