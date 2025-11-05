plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
}

android {
    namespace = "com.imps_lib.app"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            //Live
//            buildConfigField("String", "BASE_URL", "\"https://test-ppi-imps-transact-route.payworldmoney.com\"") //Test


            buildConfigField(
                "String",
                "BASE_URL",
                "\"https://ppi-imps-transact-route.payworldmoney.com\""
            )
        }
        debug {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
//            buildConfigField("String", "BASE_URL", "\"https://test-ppi-imps-transact-route.payworldmoney.com\"") //Test
            buildConfigField(
                "String",
                "BASE_URL",
                "\"https://ppi-imps-transact-route.payworldmoney.com\""
            )
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
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.media3.common)

    api(project(":sps_lib"))

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.gson)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)
    implementation(libs.lottie)
    implementation(libs.converter.scalars)
    implementation(libs.otpview)
    implementation(libs.androidx.cardview)
}

// -----------------------------
// 📦 Publishing to JitPack
// -----------------------------
afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
                groupId = "com.github.ajaysaw"
                artifactId = "imps_lib"
                version = "1.0.2"

                pom {
                    name.set("SPS SDK")
                    description.set("A custom SDK for integrating SPS payment or IMPS modules.")
                    url.set("https://github.com/ajaysaw/sps_sdk")
                    licenses {
                        license {
                            name.set("MIT License")
                            url.set("https://opensource.org/licenses/MIT")
                        }
                    }
                    developers {
                        developer {
                            id.set("ajaysaw")
                            name.set("Ajay Saw")
                            email.set("ajaysoft93@gmail.com")
                        }
                    }
                    scm {
                        connection.set("scm:git:github.com/ajaysaw/sps_sdk.git")
                        developerConnection.set("scm:git:ssh://github.com/ajaysaw/sps_sdk.git")
                        url.set("https://github.com/ajaysaw/sps_sdk")
                    }
                }
            }
        }
    }
}
