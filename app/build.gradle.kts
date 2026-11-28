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

    splits {
        abi {
            isEnable = true
            reset()
            include("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
            isUniversalApk = true
        }
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
        if (!apkDir.exists()) return@doLast

        val abiMap = mapOf(
            "app-arm64-v8a-debug.apk" to "fj-launcher-arm64-v8a.apk",
            "app-armeabi-v7a-debug.apk" to "fj-launcher-armeabi-v7a.apk",
            "app-x86-debug.apk" to "fj-launcher-x86.apk",
            "app-x86_64-debug.apk" to "fj-launcher-x86_64.apk",
            "app-universal-debug.apk" to "fj-launcher-universal.apk",
            "app-debug.apk" to "fj-launcher.apk"
        )

        abiMap.forEach { (srcName, destName) ->
            val src = File(apkDir, srcName)
            if (src.exists()) {
                val dest = File(rootProject.rootDir, destName)
                src.copyTo(dest, overwrite = true)
            }
        }

        val universalApk = File(apkDir, "app-universal-debug.apk")
        val rootApk = File(rootProject.rootDir, "fj-launcher.apk")
        if (universalApk.exists()) {
            universalApk.copyTo(rootApk, overwrite = true)
        }
    }
}

tasks.register("cleanDesktopIni") {
    doLast {
        rootProject.fileTree(mapOf("dir" to rootProject.rootDir, "include" to listOf("**/desktop.ini"))).forEach {
            it.delete()
        }
    }
}

tasks.named("preBuild").configure {
    dependsOn("cleanDesktopIni")
}

afterEvaluate {
    tasks.findByName("assembleDebug")?.finalizedBy("copyApkToRoot")
}
