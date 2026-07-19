plugins {
    id("mcp-server.conventions")
}

dependencies {
    implementation(libs.quarkus.rest.client.jackson)
    implementation(libs.gson)

    testImplementation(libs.mockito.core)
}

group = "tw.zipe.mcp.date"
