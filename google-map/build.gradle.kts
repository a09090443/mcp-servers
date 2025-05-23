import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.0.21"
    kotlin("plugin.allopen") version "2.0.21"
    id("io.quarkus")
}

repositories {
    mavenCentral()
    mavenLocal()
}

val quarkusPlatformGroupId: String by project
val quarkusPlatformArtifactId: String by project
val quarkusPlatformVersion: String by project
val mcpServerStdioVersion = "1.2.0"
val googleApiClientVersion = "2.2.0"
val googleHttpClientVersion = "1.46.3"
val googleCloudPlacesVersion = "0.31.0"

dependencies {
    implementation(enforcedPlatform("${quarkusPlatformGroupId}:${quarkusPlatformArtifactId}:${quarkusPlatformVersion}"))
    implementation("io.quarkus:quarkus-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("io.quarkus:quarkus-arc")
    implementation("io.quarkiverse.mcp:quarkus-mcp-server-stdio:$mcpServerStdioVersion")

    implementation("com.google.api-client:google-api-client:${googleApiClientVersion}")
    implementation("com.google.http-client:google-http-client:${googleHttpClientVersion}")
    implementation("com.google.http-client:google-http-client-gson:${googleHttpClientVersion}")

    // 新版 Google Cloud Places API 客戶端庫
    implementation("com.google.maps:google-maps-places:${googleCloudPlacesVersion}")

    // Google Auth 依賴
    implementation("com.google.auth:google-auth-library-oauth2-http:1.33.1")

    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.mockk:mockk:1.14.0")
}

group = "tw.zipe.mcp.googlemap"
version = "1.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.withType<Test> {
    systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

allOpen {
    annotation("jakarta.ws.rs.Path")
    annotation("jakarta.enterprise.context.ApplicationScoped")
    annotation("jakarta.persistence.Entity")
    annotation("io.quarkus.test.junit.QuarkusTest")
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_21
        javaParameters = true
    }
}
