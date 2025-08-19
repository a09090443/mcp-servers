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
val mcpServerStdioVersion = "1.4.1"
val gsonVersion = "2.12.1"
val mockitoCoreVersion = "5.16.1"

dependencies {
    implementation(enforcedPlatform("${quarkusPlatformGroupId}:${quarkusPlatformArtifactId}:${quarkusPlatformVersion}"))
    implementation("io.quarkus:quarkus-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("io.quarkus:quarkus-arc")
    implementation("io.quarkiverse.mcp:quarkus-mcp-server-stdio:$mcpServerStdioVersion")
    implementation("io.quarkus:quarkus-rest-client")
    implementation("io.quarkus:quarkus-rest-client-jackson")

    implementation("com.google.code.gson:gson:$gsonVersion")
    implementation("org.eclipse.microprofile.rest.client:microprofile-rest-client-api:4.0")

    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("org.mockito:mockito-core:$mockitoCoreVersion")
}

group = "tw.zipe.mcp.cwa"
version = "1.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.withType<Test> {
    systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
    enabled = true
}
allOpen {
    annotation("jakarta.ws.rs.Path")
    annotation("jakarta.enterprise.context.ApplicationScoped")
    annotation("jakarta.persistence.Entity")
    annotation("io.quarkus.test.junit.QuarkusTest")
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
        javaParameters = true
    }
}
