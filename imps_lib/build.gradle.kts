plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
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

            //buildConfigField("String", "BASE_URL", "\"https://sps-kyc.payworldmoney.com\"") //Live
            //buildConfigField("String", "secretKey", "\"p94kAoC4Q4pZv13FpocCMztwpTNzF6Ai\"")
            //buildConfigField("String", "IvParameterSpec", "\"*%*D^##key@%#@^&\"")

            buildConfigField("String", "BASE_URL", "\"https://test-ppi-imps-transact-route.payworldmoney.com\"") //Test

        }
        debug {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            //buildConfigField("String", "BASE_URL", "\"https://sps-kyc.payworldmoney.com\"") //Live
            //buildConfigField("String", "secretKey", "\"p94kAoC4Q4pZv13FpocCMztwpTNzF6Ai\"")
            //buildConfigField("String", "IvParameterSpec", "\"*%*D^##key@%#@^&\"")

            buildConfigField("String", "BASE_URL", "\"https://test-ppi-imps-transact-route.payworldmoney.com\"") //Test
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.media3.common)
    implementation(project(":sps_lib"))
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation (libs.androidx.cardview)

    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.gson)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)
    implementation(libs.lottie)
    implementation (libs.converter.scalars)
    implementation (libs.android.otpview.pinview)

}