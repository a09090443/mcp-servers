plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    // 慣例插件要「套用」這些插件，就必須把它們放上 buildSrc 的 classpath。
    // 版本一樣取自根目錄的 catalog，不另外寫死。
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:${libs.versions.kotlin.get()}")
    implementation("org.jetbrains.kotlin:kotlin-allopen:${libs.versions.kotlin.get()}")
    implementation("io.quarkus:io.quarkus.gradle.plugin:${libs.versions.quarkus.get()}")

    // 讓慣例插件內能以型別安全的 libs.xxx 存取 catalog。
    // Gradle 尚未原生支援在 precompiled script plugin 使用 libs 存取子，
    // 這行是官方 issue 上的通用解法，移除後 conventions 檔會編譯失敗。
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}
