import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm") version "1.9.21"
    kotlin("plugin.serialization") version "1.9.21"
    id("org.jetbrains.compose")
}

group = "io.lostpacket"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}
val ktor_version: String by project

dependencies {
    implementation("io.ktor:ktor-client-core:$ktor_version")
}
dependencies {
    // Note, if you develop a library, you should use compose.desktop.common.
    // compose.desktop.currentOs should be used in launcher-sourceSet
    // (in a separate module for demo project and in testMain).
    // With compose.desktop.common you will also lose @Preview functionality
    implementation("io.ktor:ktor-client-cio:$ktor_version")
    implementation(compose.desktop.currentOs)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
//    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
//    implementation("com.squareup.moshi:moshi:1.15.0")
//    implementation("com.squareup.moshi:moshi-kotlin:1.15.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation("com.darkrockstudios:mpfilepicker:3.1.0")
    //implementation(compose.materialIconsExtended)
//    implementation(compose.material3)
//    implementation("androidx.compose.material3:material3:1.2.0-alpha01")
//    //implementation("androidx.compose.material3:material3-window-size-class:1.2.0-alpha01")
//    implementation("androidx.compose.material3:material3-adaptive:1.0.0-alpha05")
//    implementation("androidx.compose.material3:material3-adaptive-desktop:1.0.0-alpha05")

    //implementation("androidx.compose.material:material-icons-extended:1.6.0-beta01")
    implementation("org.jetbrains.compose.material3:material3-desktop:1.6.0-beta01")

}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "emojidownload"
            packageVersion = "1.0.0"
        }
    }
}
