plugins {
    id("mcp-server.conventions")
}

dependencies {
    implementation(libs.gson)
    implementation(libs.google.api.services.gmail)
    implementation(libs.google.api.client)
    implementation(libs.google.oauth.client.jetty)
    implementation(libs.angus.mail)
}

group = "tw.zipe.mcp.gmail"
