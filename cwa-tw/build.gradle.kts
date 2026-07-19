plugins {
    id("mcp-server.conventions")
}

dependencies {
    implementation(libs.quarkus.rest.client)
    implementation(libs.quarkus.rest.client.jackson)
    implementation(libs.gson)
    implementation(libs.microprofile.rest.client.api)

    testImplementation(libs.mockito.core)
}

group = "tw.zipe.mcp.cwa"
