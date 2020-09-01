package tk.kvakva.cryptocurrencyprices

data class CryptoPrices(
    val pairs: String,
    val decimal_places: Int, // 8
    val fee: Double, // 0.2
    val fee_buyer: Double, // 0.2
    val fee_seller: Double, // 0.2
    val hidden: Int, // 0
    val max_price: Int, // 10000
    val min_amount: Double, // 0.0001
    val min_price: Double, // 0.00000001
    val min_total: Double // 0.0001
)

