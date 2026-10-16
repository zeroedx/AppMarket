@file:Suppress("UnstableApiUsage")

import com.android.build.api.variant.impl.VariantOutputImpl
import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
}

dependencies {
    implementation(projects.app.shared)
    implementation(projects.domain)
    implementation(projects.data)
    implementation(libs.koin.core)
    implementation(libs.koin.compose.viewmodel)
    implementation(libs.ktor.client.core)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.core.ktx)
    implementation(libs.coil.singleton)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.hiddenapibypass)
    implementation(libs.rikka.shizuku.provider)
    implementation(libs.rikka.shizuku.api)
    implementation(libs.focus.api)
}

val signingProperties = Properties()
runCatching {
    project.rootProject.file("local.properties").reader(Charsets.UTF_8).use(signingProperties::load)
}
val keystorePath = signingProperties.getProperty("KEYSTORE_PATH") ?: System.getenv("KEYSTORE_PATH")
val keystorePwd = signingProperties.getProperty("KEYSTORE_PASS") ?: System.getenv("KEYSTORE_PASS")
val keystoreAlias = signingProperties.getProperty("KEY_ALIAS") ?: System.getenv("KEY_ALIAS")
val keyPwd = signingProperties.getProperty("KEY_PASSWORD") ?: System.getenv("KEY_PASSWORD")

// A release build must never silently fall back to the debug key or an unsigned APK:
// all four signing inputs have to be present, otherwise the release variant is refused.
val releaseSigningConfigured =
    listOf(keystorePath, keystorePwd, keystoreAlias, keyPwd).all { !it.isNullOrBlank() }

android {
    namespace = ProjectConfig.PACKAGE_NAME
    compileSdk {
        version = release(ProjectConfig.Android.COMPILE_SDK) {
            minorApiLevel = ProjectConfig.Android.COMPILE_SDK_MINOR
        }
    }

    defaultConfig {
        applicationId = ProjectConfig.PACKAGE_NAME
        minSdk = ProjectConfig.Android.MIN_SDK
        targetSdk = ProjectConfig.Android.TARGET_SDK
        versionCode = resolveVersionCode()
        versionName = ProjectConfig.VERSION_NAME

        ndk {
            //noinspection ChromeOsAbiSupport
            abiFilters += listOf("arm64-v8a", "armeabi-v7a")
        }
    }

    if (releaseSigningConfigured) {
        signingConfigs {
            register("release") {
                storeFile = file(keystorePath!!)
                storePassword = keystorePwd
                keyAlias = keystoreAlias
                keyPassword = keyPwd
                enableV3Signing = true
                enableV4Signing = true
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            vcsInfo.include = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules-android.pro")
            if (releaseSigningConfigured) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
        debug {
            if (releaseSigningConfigured) signingConfig = signingConfigs.getByName("release")
        }
    }

    androidResources {
        localeFilters.addAll(listOf("en", "zh"))
    }

    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }

    packaging {
        jniLibs {
            excludes += "lib/*/libandroidx.graphics.path.so"
        }
        resources {
            excludes += arrayOf(
                "/META-INF/*",
                "/META-INF/androidx/**",
                "/META-INF/versions/**",
                "/org/bouncycastle/**",
                "/org/apache/commons/**",
                "/kotlin/**",
                "/kotlinx/**",
                "/okhttp3/**",
                "/*.txt",
                "/*.bin",
                "/*.json",
            )
        }
    }
}

androidComponents {
    onVariants { variant ->
        variant.outputs.forEach { output ->
            (output as? VariantOutputImpl)?.outputFileName?.set(
                output.versionName.zip(output.versionCode) { versionName, versionCode ->
                    "${ProjectConfig.APP_NAME}-v${versionName}(${versionCode})${if (variant.buildType == "debug") "_debug" else ""}.apk"
                }
            )
        }
    }
}

// Release packaging must not run without a real signing key: fail before the APK is produced
// instead of emitting an unsigned artifact or silently reusing the debug key.
val validateReleaseSigning = tasks.register<ValidateReleaseSigningTask>("validateReleaseSigning") {
    description = "Fails when release signing inputs (keystore/alias/passwords) are missing."
    configured.set(releaseSigningConfigured)
}

tasks.matching { it.name in setOf("packageRelease", "bundleRelease", "assembleRelease") }
    .configureEach { dependsOn(validateReleaseSigning) }
