plugins {
    id("mcp-server.conventions")
}

dependencies {
    implementation(libs.gson)

    testImplementation(libs.mockk)
}

group = "tw.zipe.mcp.filesystem"
