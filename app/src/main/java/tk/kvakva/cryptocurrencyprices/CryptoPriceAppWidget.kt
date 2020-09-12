package tk.kvakva.cryptocurrencyprices

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetManager.*
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.RemoteViews
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit


private const val TAG = "MY_Widget"
private const val ACTION_SIMPLEAPPWIDGET = "ACTION_BROADCASTWIDGETSAMPLE"

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in [CryptoPriceAppWidgetConfigureActivity]
 */
class CryptoPriceAppWidget : AppWidgetProvider() {

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        if (intent?.action.equals(ACTION_SIMPLEAPPWIDGET) && context != null) {
            Log.d(TAG, "onReceive: $ACTION_SIMPLEAPPWIDGET")
            val appWidgetManager = getInstance(context)
            val thisAppWidgetComponentName =
                ComponentName(context.applicationContext, CryptoPriceAppWidget::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidgetComponentName)
            onUpdate(context, appWidgetManager, appWidgetIds)
        }
        Log.d(TAG, "onReceive: ")
    }

    private lateinit var yoServerTime: String
    private lateinit var yoCu: String
    private lateinit var poCu: String
    lateinit var biCu: String

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        runBlocking {
            val client = OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .callTimeout(10, TimeUnit.SECONDS)
                .addInterceptor(
                    HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BODY
                    }
                )
                .build()
            val jsonTecker = withContext(Dispatchers.IO) {

                val request = Request.Builder()
                    .url("https://yobit.net/api/2/btc_usd/ticker")
                    .build()
                val response = client.newCall(request).execute()
                val respBodyString = response.body?.string() ?: ""
                Log.d("widget", "onCreate: JSONObject:\n$respBodyString")
                val jsonObj = JSONObject(respBodyString) //.toString(8)
                //var jsonTecker =
                response.close()
                jsonObj.getJSONObject("ticker")
            }
            yoServerTime =
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    Instant.ofEpochSecond(jsonTecker.getLong("server_time"))
                        .atZone(ZoneId.of("Europe/Moscow"))
                        .format(DateTimeFormatter.RFC_1123_DATE_TIME)
                } else {
                    "old Android version < 0"
                }
            yoCu = jsonTecker.getDouble("last").toString()

            val jsonTeckerp = withContext(Dispatchers.IO) {
                val request = Request.Builder()
                    .url("https://poloniex.com/public?command=returnTicker")
                    .build()
                val response = client.newCall(request).execute()
                val respBodyString = response.body?.string() ?: ""
                val jsonObj = JSONObject(respBodyString) //.toString(8)
                response.close()
                jsonObj.getJSONObject("USDC_BTC")
            }
            poCu = jsonTeckerp.getDouble("last").toString()
        }

        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            //updateAppWidget(context, appWidgetManager, appWidgetId)
            updAppWid(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        // When the user deletes the widget, delete the preference associated with it.
        for (appWidgetId in appWidgetIds) {
            deleteTitlePref(context, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }


    override fun onAppWidgetOptionsChanged(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetId: Int,
        newOptions: Bundle?
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)

        Log.d(TAG, "o: newOptions MIN_WIDTH  ${newOptions?.get(OPTION_APPWIDGET_MIN_WIDTH)}")
        Log.d(TAG, "o: newOptions MAX_WIDTH  ${newOptions?.get(OPTION_APPWIDGET_MAX_WIDTH)}")
        Log.d(TAG, "o: newOptions MIN_HEIGHT ${newOptions?.get(OPTION_APPWIDGET_MIN_HEIGHT)}")
        Log.d(TAG, "o: newOptions MAX_HEIGHT ${newOptions?.get(OPTION_APPWIDGET_MAX_HEIGHT)}")
    }

    private fun updAppWid(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val widgetText = loadTitlePref(context, appWidgetId)
        val widgetUpdateTime = loadTitlePref(context, appWidgetId)
        // Construct the RemoteViews object
        val views = RemoteViews(context.packageName, R.layout.crypto_price_app_widget)
        views.setTextViewText(R.id.appwidget_text, widgetText)

        views.setTextViewText(R.id.appwidget_text, yoServerTime)
        views.setTextViewText(R.id.btc_price, yoCu)
        views.setTextViewText(R.id.poPrice, poCu)
        val pendingIntent: PendingIntent = Intent(context, MainActivity::class.java)
            .let { intent ->
                PendingIntent.getActivity(context, 0, intent, 0)
            }
        views.setOnClickPendingIntent(R.id.app_widget, pendingIntent)
        val pi: PendingIntent = Intent(context, CryptoPriceAppWidget::class.java)
            .let { intent: Intent ->
                intent.action = ACTION_SIMPLEAPPWIDGET
                PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_ONE_SHOT)
            }
        views.setOnClickPendingIntent(R.id.refresh_bt, pi)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}


internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {

    val widgetText = loadTitlePref(context, appWidgetId)
    val widgetUpdateTime = loadTitlePref(context, appWidgetId)
    // Construct the RemoteViews object
    val views = RemoteViews(context.packageName, R.layout.crypto_price_app_widget)
    views.setTextViewText(R.id.appwidget_text, widgetText)

    //-------------
    GlobalScope.launch(Dispatchers.IO) {
        val client = OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .callTimeout(10, TimeUnit.SECONDS)
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
            )
            .build()
        var request = Request.Builder()
            .url("https://yobit.net/api/2/btc_usd/ticker")
            .build()
        var response = client.newCall(request).execute()
        var respBodyString = response.body?.string() ?: ""
        Log.d("widget", "onCreate: JSONObject:\n$respBodyString")
        var jsonObj = JSONObject(respBodyString) //.toString(8)
        var jsonTecker = jsonObj.getJSONObject("ticker")

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            views.setTextViewText(
                R.id.appwidget_text,
                Instant.ofEpochSecond(jsonTecker.getLong("server_time"))
                    .atZone(ZoneId.of("Europe/Moscow")).format(DateTimeFormatter.RFC_1123_DATE_TIME)
            )
        }

        views.setTextViewText(R.id.btc_price, jsonTecker.getDouble("last").toString())

        response.close()

        request = Request.Builder()
            .url("https://poloniex.com/public?command=returnTicker")
            .build()
        response = client.newCall(request).execute()
        respBodyString = response.body?.string() ?: ""
        jsonObj = JSONObject(respBodyString) //.toString(8)
        jsonTecker = jsonObj.getJSONObject("USDC_BTC")

        views.setTextViewText(R.id.poPrice, jsonTecker.getDouble("last").toString())

        response.close()

        val pendingIntent: PendingIntent = Intent(context, MainActivity::class.java)
            .let { intent ->
                PendingIntent.getActivity(context, 0, intent, 0)
            }
        views.setOnClickPendingIntent(R.id.app_widget, pendingIntent)
        val pi: PendingIntent = Intent(context, CryptoPriceAppWidget::class.java)
            .let { intent: Intent ->
                intent.action = ACTION_SIMPLEAPPWIDGET
                PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)
            }
        views.setOnClickPendingIntent(R.id.refresh_bt, pi)

        appWidgetManager.updateAppWidget(appWidgetId, views)

    }
    //-------------

    // Instruct the widget manager to update the widget


}