package tw.zipe.mcp.cwa

import jakarta.inject.Singleton
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.QueryParam
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient

@Singleton
@RegisterRestClient(baseUri = "https://opendata.cwa.gov.tw/api")
interface WeatherClient {

    @GET
    @Path("/v1/rest/datastore/F-C0032-001")
    fun getWeatherForecast(
        @QueryParam("Authorization") authorization: String,
        @QueryParam("locationName") locationName: List<String>,
        @QueryParam("elementName") elementName: List<String>,
        @QueryParam("timeFrom") timeFrom: String,
        @QueryParam("timeTo") timeTo: String,
        @QueryParam("sort") sort: String
    ): Map<String, Any>

    @GET
    @Path("/v1/rest/datastore/E-A0015-001")
    fun getEarthquakeData(
        @QueryParam("Authorization") authorization: String,
        @QueryParam("AreaName") areaName: List<String>,
        @QueryParam("timeFrom") timeFrom: String,
        @QueryParam("timeTo") timeTo: String,
        @QueryParam("limit") limit: Int,
        @QueryParam("sort") sort: String
    ): Map<String, Any>
}
