import org.gradle.accessors.dm.LibrariesForLibs
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// 八個 MCP server 模組共用的建置慣例。
// 模組的 build.gradle.kts 只需 plugins { id("mcp-server.conventions") }，
// 再宣告自己的 group 與專屬相依即可。

plugins {
    kotlin("jvm")
    kotlin("plugin.allopen")
    id("io.quarkus")
}

val libs = the<LibrariesForLibs>()

repositories {
    mavenCentral()
    mavenLocal()
}

// 所有模組共用同一組版號，模組端不需再宣告。
version = "1.0-SNAPSHOT"

dependencies {
    // enforcedPlatform 會強制覆寫相依版本，改版前先看 CLAUDE.md 的「改版本時的兩個陷阱」。
    add("implementation", enforcedPlatform(libs.quarkus.bom))
    add("implementation", libs.quarkus.kotlin)
    add("implementation", libs.kotlin.stdlib.jdk8)
    add("implementation", libs.quarkus.arc)
    add("implementation", libs.mcp.server.stdio)

    add("testImplementation", libs.quarkus.junit5)
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_21
        // MCP 靠參數名產生 tool schema，缺這行 @Tool 的參數名會變成 arg0、arg1。
        javaParameters = true
    }
}

// CDI bean 需要非 final 的類別，Kotlin 類別預設 final。
// 新增的 bean 若用了清單外的註解，要在這裡補上，否則注入會失敗。
allOpen {
    annotation("jakarta.ws.rs.Path")
    annotation("jakarta.enterprise.context.ApplicationScoped")
    annotation("jakarta.persistence.Entity")
    annotation("io.quarkus.test.junit.QuarkusTest")
}

tasks.withType<Test>().configureEach {
    systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}
