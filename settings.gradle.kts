pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        mavenLocal()
    }
}

// gradle/libs.versions.toml 位於 Gradle 預設路徑，會自動註冊為所有子專案可用的 libs。
rootProject.name = "mcp-servers"

include(
    "cwa-tw",
    "date",
    "excel",
    "filesystem",
    "gmail",
    "google-drive",
    "google-map",
    "tw-stock",
)
