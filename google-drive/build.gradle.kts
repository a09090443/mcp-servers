plugins {
    id("mcp-server.conventions")
}

dependencies {
    implementation(libs.google.api.client)
    implementation(libs.google.oauth.client.jetty)
    implementation(libs.google.api.services.drive)
    implementation(libs.commons.io)
}

group = "tw.zipe.mcp.googledrive"
