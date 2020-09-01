package tk.kvakva.cryptocurrencyprices


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ticker(
    @Json(name = "ticker")
    val ticker: Ticker
) {
    @JsonClass(generateAdapter = true)
    data class Ticker(
        @Json(name = "avg")
        val avg: Double, // 0.00527497
        @Json(name = "buy")
        val buy: Double, // 0.0052615
        @Json(name = "high")
        val high: Double, // 0.00536993
        @Json(name = "last")
        val last: Double, // 0.00526384
        @Json(name = "low")
        val low: Double, // 0.00518001
        @Json(name = "sell")
        val sell: Double, // 0.00526608
        @Json(name = "server_time")
        val serverTime: Int, // 1598929681
        @Json(name = "updated")
        val updated: Int, // 1598929511
        @Json(name = "vol")
        val vol: Double, // 259.44232
        @Json(name = "vol_cur")
        val volCur: Double // 49226.886
    )
}