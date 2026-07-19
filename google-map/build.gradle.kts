plugins {
    id("mcp-server.conventions")
}

dependencies {
    implementation(libs.google.api.client)
    implementation(libs.google.http.client)
    implementation(libs.google.http.client.gson)

    // 新版 Google Cloud Places API 客戶端庫
    implementation(libs.google.maps.places)

    // Google Auth 依賴
    implementation(libs.google.auth.library.oauth2.http)

    testImplementation(libs.mockk)
}

group = "tw.zipe.mcp.googlemap"
