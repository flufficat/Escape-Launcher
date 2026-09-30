plugins {
    alias(libs.plugins.escapelauncher.android.feature)
}

android {
    namespace = "com.geecee.escapelauncher.feature.shortcuts"
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:model"))
    implementation(project(":core:di"))
}
