package tk.kvakva.cryptocurrencyprices

import android.app.Application
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

private const val TAG = "MY_MainActivityViewMode"

class MainActivityViewModel(val appl: Application) : AndroidViewModel(appl) {

    private val _poHi = MutableLiveData<String>("-1")
    val poHi: LiveData<String>
        get() = _poHi

    private val _poLo = MutableLiveData<String>("-1")
    val poLo: LiveData<String>
    get() = _poLo

    private val _poLa = MutableLiveData<String>("-1")
    val poLa: LiveData<String>
        get() = _poLa

    private val _yoHi = MutableLiveData<String>("-1")
    val yoHi: LiveData<String>
        get() = _yoHi

    private val _yoLo = MutableLiveData<String>("-1")
    val yoLo: LiveData<String>
    get() = _yoLo

    private val _yoLa = MutableLiveData<String>("-1")
    val yoLa: LiveData<String>
        get() = _yoLa

    private val _yoTimeUpdated = MutableLiveData<String>("1969 may")
    val yoTimeUpdated: LiveData<String>
        get() = _yoTimeUpdated

    private val _yoServerTime = MutableLiveData<String>("1969 may")
    val yoServerTime: LiveData<String>
        get() = _yoServerTime


    private val _biHi = MutableLiveData<String>("-1")
    val biHi: LiveData<String>
        get() = _biHi

    private val _biLo = MutableLiveData<String>("-1")
    val biLo: LiveData<String>
        get() = _biLo

    private val _biLa = MutableLiveData<String>("-1")
    val biLa: LiveData<String>
        get() = _biLa

    private val _biTimeStamp = MutableLiveData<String>("1969 may")
    val biTimeStamp: LiveData<String>
        get() = _biTimeStamp


    suspend fun _fetchYo() {
        try {
            val client = OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .callTimeout(10, TimeUnit.SECONDS)
//                .addInterceptor(
//                    HttpLoggingInterceptor().apply {
//                        level = HttpLoggingInterceptor.Level.BODY
//                    }
//                )
                .build()
            var request = Request.Builder()
                .url("https://yobit.net/api/2/btc_usd/ticker")
                .build()
            var response =  client.newCall(request).execute()
            var _respBodyString = response.body?.string() ?: ""
            Log.d(TAG, "onCreate: JSONObject:\n$_respBodyString")
            var jsonObj = JSONObject(_respBodyString) //.toString(8)
            var jsonTecker = jsonObj.getJSONObject("ticker")
            _yoHi.postValue("hi: " + jsonTecker.getDouble("high").toString())
            _yoLo.postValue("lo: " + jsonTecker.getDouble("low").toString())
            _yoLa.postValue("la: " + jsonTecker.getDouble("last").toString())


            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                _yoTimeUpdated.postValue(
                    LocalDateTime.ofEpochSecond(
                        jsonTecker.getLong("updated"),
                        0,
                        ZoneOffset.UTC
                    ).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                )
            } else {
                TODO("VERSION.SDK_INT < O")
            }

            val instant = Instant.ofEpochSecond(jsonTecker.getLong("updated"))
            _yoTimeUpdated.postValue("up: ${instant.atZone(ZoneId.of("Europe/Moscow")).format(
                DateTimeFormatter.RFC_1123_DATE_TIME)}")

            _yoServerTime.postValue( "se: " +
                    Instant.ofEpochSecond(jsonTecker.getLong("server_time"))
                        .atZone(ZoneId.of("Europe/Moscow")).format(DateTimeFormatter.RFC_1123_DATE_TIME)
            )


//            val client = OkHttpClient.Builder()
//                .connectTimeout(5, TimeUnit.SECONDS)
//                .writeTimeout(5, TimeUnit.SECONDS)
//                .readTimeout(5, TimeUnit.SECONDS)
//                .callTimeout(10, TimeUnit.SECONDS)
//                .addInterceptor(
//                    HttpLoggingInterceptor().apply {
//                        level = HttpLoggingInterceptor.Level.BODY
//                    }
//                )
//                .build()
            request = Request.Builder()
                .url("https://poloniex.com/public?command=returnTicker")
                .build()
            response = client.newCall(request).execute()
            _respBodyString = response.body?.string() ?: ""
            jsonObj = JSONObject(_respBodyString) //.toString(8)
            jsonTecker = jsonObj.getJSONObject("USDC_BTC")

            _poHi.postValue("hi: " + jsonTecker.getDouble("high24hr").toString())
            _poLo.postValue("lo: " + jsonTecker.getDouble("low24hr").toString())
            _poLa.postValue("la: " + jsonTecker.getDouble("last").toString())


            request = Request.Builder()
                .url("https://api.bittrex.com/api/v1.1/public/getmarketsummary?market=usd-btc")
                .build()
            response = client.newCall(request).execute()
            _respBodyString = response.body?.string() ?: ""
            jsonObj = JSONObject(_respBodyString) //.toString(8)
            if(jsonObj.getBoolean("success")){
                val a=jsonObj.optJSONArray("result")
                if (a != null && a.length()>0) {
                    val o=a.getJSONObject(0)
                    _biHi.postValue("hi: " + o.getDouble("High"))
                    _biLo.postValue("lo: " + o.getDouble("Low"))
                    _biLa.postValue("la: " + o.getDouble("Last"))
                    _biTimeStamp.postValue("ti: " + o.getString("TimeStamp"))
                }
            }

            response.close()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    init {

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _fetchYo()
            }
        }
    }

}

