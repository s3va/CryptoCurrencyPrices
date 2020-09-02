package tk.kvakva.cryptocurrencyprices

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.util.Log
import android.widget.RemoteViews
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in [CryptoPriceAppWidgetConfigureActivity]
 */
class CryptoPriceAppWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
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
}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {

    val widgetText = loadTitlePref(context, appWidgetId)
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
        var _respBodyString = response.body?.string() ?: ""
        Log.d("widget", "onCreate: JSONObject:\n$_respBodyString")
        var jsonObj = JSONObject(_respBodyString) //.toString(8)
        var jsonTecker = jsonObj.getJSONObject("ticker")

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            views.setTextViewText(R.id.appwidget_text,
                    Instant.ofEpochSecond(jsonTecker.getLong("server_time"))
                        .atZone(ZoneId.of("Europe/Moscow")).format(DateTimeFormatter.RFC_1123_DATE_TIME)
            )
        }

        views.setTextViewText(R.id.btc_price,jsonTecker.getDouble("last").toString())

        response.close()

        request = Request.Builder()
            .url("https://poloniex.com/public?command=returnTicker")
            .build()
        response = client.newCall(request).execute()
        _respBodyString = response.body?.string() ?: ""
        jsonObj = JSONObject(_respBodyString) //.toString(8)
        jsonTecker = jsonObj.getJSONObject("USDC_BTC")

        views.setTextViewText(R.id.poPrice,jsonTecker.getDouble("last").toString())

        response.close()

        appWidgetManager.updateAppWidget(appWidgetId, views)

    }
    //-------------

    // Instruct the widget manager to update the widget
}