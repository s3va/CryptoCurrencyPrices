package tk.kvakva.cryptocurrencyprices


import android.content.Intent
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import tk.kvakva.cryptocurrencyprices.databinding.ActivityMainBinding
import java.util.concurrent.TimeUnit

private const val TAG = "MY_MainActivity"

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel by viewModels<MainActivityViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)
        binding=DataBindingUtil.setContentView(this,R.layout.activity_main)
        binding.mainActViewModel=viewModel
        binding.lifecycleOwner=this

        val recViAd = AdapterCrypto(RecyViewListener { v: View, cP: CryptoPrices ->
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val client = OkHttpClient.Builder()
                        .connectTimeout(5, TimeUnit.SECONDS)
                        .writeTimeout(5, TimeUnit.SECONDS)
                        .readTimeout(5, TimeUnit.SECONDS)
                        .callTimeout(10, TimeUnit.SECONDS)
//                        .addInterceptor(
//                            HttpLoggingInterceptor().apply {
//                                level = HttpLoggingInterceptor.Level.BODY
//                            }
//                        )
                        .build()
                    val request = Request.Builder()
                        .url("https://yobit.net/api/2/${cP.pairs}/ticker")
                        .build()
                    val response = client.newCall(request).execute()
                    val _respBodyString=response.body?.string()?:""
                    Log.d(TAG, "onCreate: JSONObject:\n$_respBodyString")
                    val formatedJS = JSONObject(_respBodyString).toString(8)

                    runOnUiThread {
                        AlertDialog.Builder(this@MainActivity)
                            .setTitle(cP.pairs)
                            //.setMessage((response.body?.string() ?: "").replace(",","\n"))
                            .setMessage(formatedJS)
                            .setOnDismissListener {
                                Log.d(TAG, "onCreate: qwerqweqwerqwerqwerq")
                            }
                            .setPositiveButton("Positive") { p0, p1 ->
                                Log.d(TAG, "onCreate: Positive $p0")
                                Log.d(TAG, "onCreate: Positive $p1")
                            }
                            .setNegativeButton("Negative") { p0, p1 ->
                                Log.d(TAG, "onCreate: Negative $p0")
                                Log.d(TAG, "onCreate: Negative $p1")
                            }
                            .setNeutralButton("Neutral") { p0, p1 ->
                                Log.d(TAG, "onCreate: Neutral $p0")
                                Log.d(TAG, "onCreate: Neutral $p1")
                            }
                            .show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

        })

        button.setOnClickListener {
            startService(Intent(this,CryptoPriceAppWidget.UUUpdateService::class.java))
        }

        //recyclerViewCryptoList.layoutManager=LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false)
        //recyclerViewCryptoList.layoutManager=GridLayoutManager(this,if(resources.configuration.orientation==Configuration.ORIENTATION_PORTRAIT) 4 else 6)
        //recyclerViewCryptoList.adapter = recViAd

        swipeToRefresh.setOnRefreshListener {
            lifecycleScope.launch(Dispatchers.IO) {
/*                try {
                    val client = OkHttpClient.Builder()
                        .connectTimeout(5, TimeUnit.SECONDS)
                        .writeTimeout(5, TimeUnit.SECONDS)
                        .readTimeout(5, TimeUnit.SECONDS)
                        .callTimeout(10, TimeUnit.SECONDS)
//                        .addInterceptor(
//                            HttpLoggingInterceptor().apply {
//                                level = HttpLoggingInterceptor.Level.BODY
//                            }
//                        )
                        .build()
                    val request = Request.Builder()
                        .url("https://yobit.net/api/3/info")
                        .build()
                    val response = client.newCall(request).execute()
                    //Log.d(TAG, "onCreate: $response")
                    val jsonObject = JSONObject(response.body?.string()?:"")
                    //Log.d(TAG, "onCreate: ----\n$jsonObject\n----------")
                    val jsonPairs = jsonObject.getJSONObject("pairs")
                    //Log.d(TAG, "onCreate: jsonPaires ------------------\n$jsonPairs\n-------------")
                    val pairsKeys = jsonPairs.keys()
                    val l= mutableListOf<CryptoPrices>()
                    pairsKeys.forEach {
                        val p=jsonPairs.getJSONObject(it)
                        l.add(CryptoPrices(
                            it,
                            p.getInt("decimal_places"),
                            p.getDouble("fee"),
                            p.getDouble("fee_buyer"),
                            p.getDouble("fee_seller"),
                            p.getInt("hidden"),
                            p.getInt("max_price"),
                            p.getDouble("min_amount"),
                            p.getDouble("min_price"),
                            p.getDouble("min_total")
                        ))
                    }
                    runOnUiThread {
                        recViAd.data = l
                    }
                } catch (e: SocketException) {
                    val l= listOf<CryptoPrices>(CryptoPrices(e.message?:"",0,0.0,0.0,0.0,0,0,0.0,0.0,0.0),
                        CryptoPrices(e.stackTraceToString().substring(0,200),0,0.0,0.0,0.0,0,0,0.0,0.0,0.0)
                    )

                    runOnUiThread {
                        recViAd.data=l
                    }
                    e.printStackTrace()
                } catch (e: Exception) {
                    e.printStackTrace()
                }*/
                //delay(5000);
                ////////////////viewModel._fetchYo()
                swipeToRefresh.isRefreshing = false;
            }
        }
    }
}
