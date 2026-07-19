plugins {
    id("mcp-server.conventions")
}

dependencies {
    implementation(libs.poi)
    implementation(libs.poi.ooxml)
    implementation(libs.gson)
}

group = "tw.zipe.mcp.excel"
