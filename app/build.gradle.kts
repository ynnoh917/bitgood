plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.paperwallet"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.paperwallet"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    // ❌ 已移除：signingConfigs 块，不再使用你的 bit.keystore

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            // 让 Release 构建也使用自动生成的 Debug 签名，方便直接安装测试
            signingConfig = signingConfigs.getByName("debug")
        }
        // debug 构建类型无需额外配置，Gradle 默认会自动处理签名
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
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/DEPENDENCIES"
            excludes += "META-INF/LICENSE"
            excludes += "META-INF/LICENSE.txt"
            excludes += "META-INF/NOTICE"
            excludes += "META-INF/NOTICE.txt"
        }
    }
}

// 👇 核心修复：在全局范围内强制统一 BouncyCastle 的模块和版本 👇
// 这是解决重复类冲突最彻底、最官方的方式
configurations.all {
    resolutionStrategy {
        dependencySubstitution {
            // 将所有旧版或冲突的模块，统一替换为 bcprov-jdk18on:1.78
            substitute(module("org.bouncycastle:bcprov-jdk15to18")).using(module("org.bouncycastle:bcprov-jdk18on:1.78"))
            substitute(module("org.bouncycastle:bcprov-jdk15on")).using(module("org.bouncycastle:bcprov-jdk18on:1.78"))
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    implementation("com.google.zxing:core:3.5.2")

    // 依赖声明时无需再手动 exclude，上面的 dependencySubstitution 会统一处理
    implementation("org.bitcoinj:bitcoinj-core:0.16.3")
    implementation("org.web3j:crypto:4.10.3")
    implementation("org.bouncycastle:bcprov-jdk18on:1.78")
}