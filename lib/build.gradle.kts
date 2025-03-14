plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
    id("maven-publish")
}

// https://docs.gradle.org/current/userguide/publishing_maven.html
publishing {
    // https://github.com/russhwolf/multiplatform-settings/blob/main/build.gradle.kts#L55C1-L55C70
    publications.withType<MavenPublication>().configureEach {
        groupId = "com.dgmltn"
        artifactId = "ded"
        version = "1.0.0"
    }


//            pom {
//                name.set("Ded - Doug's Editor")
//                description.set("A Kotlin Multiplatform library for building text editors.")
//                url.set("https://github.com/dgmltn/ded")
//
//                licenses {
//                    license {
//                        name.set("The Apache Software License, Version 2.0")
//                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
//                        distribution.set("repo")
//                    }
//                }
//                developers {
//                    developer {
//                        id.set("dgmltn")
//                        name.set("Doug Melton")
//                    }
//                }
//                scm {
//                    url.set("https://github.com/dgmltn/ded")
//                }
//            }
    repositories {
        mavenLocal()
//        maven {
//            ...
//        }
    }
}

kotlin {
    androidTarget {
        publishAllLibraryVariants()
    }

    jvm("desktop") {
    }

//    listOf(
//        iosX64(),
//        iosArm64(),
//        iosSimulatorArm64()
//    ).forEach { iosTarget ->
//        iosTarget.binaries.framework {
//            baseName = "Sample"
//            isStatic = true
//        }
//    }

    sourceSets {
        val desktopMain by getting

        androidMain.dependencies {
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.androidx.activity.compose)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.kermit)

            implementation(libs.codroid.textmate)
            implementation(libs.oniguruma.lib)
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "com.dgmltn.ded"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    sourceSets {
        getByName("main") {
            manifest.srcFile("src/androidMain/AndroidManifest.xml")
            res.srcDirs("src/androidMain/res")
            resources.srcDirs("src/commonMain/resources")
        }
    }
    dependencies {
        debugImplementation(libs.compose.ui.tooling)
    }
}
dependencies {
    implementation(libs.kotlin.test)
}
