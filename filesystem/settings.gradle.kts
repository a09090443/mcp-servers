pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        mavenLocal()
    }
}

// 版本集中在根目錄的 gradle/libs.versions.toml，此模組仍是獨立的 Gradle 專案。
dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "filesystem"
