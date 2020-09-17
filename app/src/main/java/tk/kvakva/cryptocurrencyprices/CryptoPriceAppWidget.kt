package tk.kvakva.cryptocurrencyprices

import android.app.PendingIntent
import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetManager.*
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.RemoteViews
import androidx.core.os.bundleOf
import androidx.core.os.persistableBundleOf
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit


private const val TAG = "MY_Widget"
private const val ACTION_SIMPLEAPPWIDGET = "ACTION_BROADCASTWIDGETSAMPLE"
internal const val INT_WIDGET_KEY = "WI_KEY"
private const val TESTURI_WIDGET_KEY = "TE_KEY"

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in [CryptoPriceAppWidgetConfigureActivity]
 */
class CryptoPriceAppWidget : AppWidgetProvider() {

    class UUUpdateService : JobService() {
        private val _TAG = "UUUpdateService"
        private lateinit var yoServerTime: String
        private lateinit var yoCu: String
        private lateinit var poCu: String

        /**
         * Called by the system every time a client explicitly starts the service by calling
         * [android.content.Context.startService], providing the arguments it supplied and a
         * unique integer token representing the start request.  Do not call this method directly.
         *
         *
         * For backwards compatibility, the default implementation calls
         * [.onStart] and returns either [.START_STICKY]
         * or [.START_STICKY_COMPATIBILITY].
         *
         *
         * Note that the system calls this on your
         * service's main thread.  A service's main thread is the same
         * thread where UI operations take place for Activities running in the
         * same process.  You should always avoid stalling the main
         * thread's event loop.  When doing long-running operations,
         * network calls, or heavy disk I/O, you should kick off a new
         * thread, or use [android.os.AsyncTask].
         *
         * @param intent The Intent supplied to [android.content.Context.startService],
         * as given.  This may be null if the service is being restarted after
         * its process has gone away, and it had previously returned anything
         * except [.START_STICKY_COMPATIBILITY].
         * @param flags Additional data about this start request.
         * @param startId A unique integer representing this specific request to
         * start.  Use with [.stopSelfResult].
         *
         * @return The return value indicates what semantics the system should
         * use for the service's current started state.  It may be one of the
         * constants associated with the [.START_CONTINUATION_MASK] bits.
         *
         * @see .stopSelfResult
         */
//        override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//
//
//            return super.onStartCommand(intent, flags, startId)
//        }




        /**
         * Called to indicate that the job has begun executing.  Override this method with the
         * logic for your job.  Like all other component lifecycle callbacks, this method executes
         * on your application's main thread.
         *
         *
         * Return `true` from this method if your job needs to continue running.  If you
         * do this, the job remains active until you call
         * [.jobFinished] to tell the system that it has completed
         * its work, or until the job's required constraints are no longer satisfied.  For
         * example, if the job was scheduled using
         * [setRequiresCharging(true)][JobInfo.Builder.setRequiresCharging],
         * it will be immediately halted by the system if the user unplugs the device from power,
         * the job's [.onStopJob] callback will be invoked, and the app
         * will be expected to shut down all ongoing work connected with that job.
         *
         *
         * The system holds a wakelock on behalf of your app as long as your job is executing.
         * This wakelock is acquired before this method is invoked, and is not released until either
         * you call [.jobFinished], or after the system invokes
         * [.onStopJob] to notify your job that it is being shut down
         * prematurely.
         *
         *
         * Returning `false` from this method means your job is already finished.  The
         * system's wakelock for the job will be released, and [.onStopJob]
         * will not be invoked.
         *
         * @param params Parameters specifying info about this job, including the optional
         * extras configured with [     This object serves to identify this specific running job instance when calling][JobInfo.Builder.setExtras]
         */
        override fun onStartJob(params: JobParameters?): Boolean {
            val widgetId = params?.extras?.getInt(INT_WIDGET_KEY)?:0
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                Log.i(_TAG, "onStartJob: params= ${params?.extras.toString()} ${params?.transientExtras.toString()}")
            } else {
                Log.i(_TAG, "onStartJob: params= ${params?.extras.toString()}")
            }
            val views = RemoteViews(packageName, R.layout.crypto_price_app_widget)
            CoroutineScope(Dispatchers.Default).launch {

                val client = OkHttpClient.Builder()
                    .connectTimeout(5, TimeUnit.SECONDS)
                    .writeTimeout(5, TimeUnit.SECONDS)
                    .readTimeout(5, TimeUnit.SECONDS)
                    .callTimeout(10, TimeUnit.SECONDS)
                    //.addInterceptor(
                    //    HttpLoggingInterceptor().apply {
                    //        level = HttpLoggingInterceptor.Level.BODY
                    //    }
                    //)
                    .build()

                val jsonTecker = async(Dispatchers.IO) {
                    Log.i(_TAG, "onStartJob: before delay")
                    delay(14000)
                    Log.i(_TAG, "onStartJob: after delay")
                    val request = Request.Builder()
                        .url("https://yobit.net/api/2/btc_usd/ticker")
                        .build()
                    val response = client.newCall(request).execute()
                    val respBodyString = response.body?.string() ?: ""
                    Log.d(_TAG, "onStartJob: JSONObject:\n$respBodyString")
                    val jsonObj = JSONObject(respBodyString) //.toString(8)
                    //var jsonTecker =
                    response.close()
                    jsonObj.getJSONObject("ticker")
                }

                yoServerTime =
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        Instant.ofEpochSecond(jsonTecker.await().getLong("server_time"))
                            .atZone(ZoneId.of("Europe/Moscow"))
                            .format(DateTimeFormatter.RFC_1123_DATE_TIME)
                    } else {
                        "old Android version < 0"
                    }
                yoCu = jsonTecker.await().getDouble("last").toString()

                val jsonTeckerp = async(Dispatchers.IO) {
                    val request = Request.Builder()
                        .url("https://poloniex.com/public?command=returnTicker")
                        .build()
                    val response = client.newCall(request).execute()
                    val respBodyString = response.body?.string() ?: ""
                    val jsonObj = JSONObject(respBodyString) //.toString(8)
                    response.close()
                    jsonObj.getJSONObject("USDC_BTC")
                }
                poCu = jsonTeckerp.await().getDouble("last").toString()


//            val pendingIntent: PendingIntent = Intent(this, MainActivity::class.java)
//                .let { _intent ->
//                    PendingIntent.getActivity(this, 0, _intent, 0)
//                }
//            views.setOnClickPendingIntent(R.id.app_widget, pendingIntent)
                val pi: PendingIntent = Intent(
                    this@UUUpdateService.applicationContext,
                    CryptoPriceAppWidget::class.java
                )
                    .let { _intent: Intent ->
                        _intent.action = ACTION_SIMPLEAPPWIDGET
                        _intent.putExtra(INT_WIDGET_KEY,widgetId)
                        //_intent.setData(Uri.parse("https://www.ru/" + UUID.randomUUID()))
                        PendingIntent.getBroadcast(
                            this@UUUpdateService,
                            widgetId,
                            _intent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                        )
                    }
                views.setOnClickPendingIntent(R.id.refresh_bt, pi)
                views.setTextViewText(R.id.appwidget_text, yoServerTime)
                views.setTextViewText(R.id.btc_price, yoCu)
                views.setTextViewText(R.id.poPrice, poCu)

                if(widgetId==0)
                    getInstance(this@UUUpdateService).updateAppWidget(
                        ComponentName(this@UUUpdateService, CryptoPriceAppWidget::class.java), views
                    )
                else {
                    views.setTextViewText(R.id.textViewId,"$widgetId")
                    getInstance(this@UUUpdateService).updateAppWidget(
                        widgetId, views
                    )
                }
                Log.i(_TAG, "onStartJob: end")
                jobFinished(params,false)
            }
            Log.i(_TAG, "onStartJob: return false")

            return false
        }

        /**
         * This method is called if the system has determined that you must stop execution of your job
         * even before you've had a chance to call [.jobFinished].
         *
         *
         * This will happen if the requirements specified at schedule time are no longer met. For
         * example you may have requested WiFi with
         * [android.app.job.JobInfo.Builder.setRequiredNetworkType], yet while your
         * job was executing the user toggled WiFi. Another example is if you had specified
         * [android.app.job.JobInfo.Builder.setRequiresDeviceIdle], and the phone left its
         * idle maintenance window. You are solely responsible for the behavior of your application
         * upon receipt of this message; your app will likely start to misbehave if you ignore it.
         *
         *
         * Once this method returns, the system releases the wakelock that it is holding on
         * behalf of the job.
         *
         * @param params The parameters identifying this job, as supplied to
         * the job in the [.onStartJob] callback.
         * @return `true` to indicate to the JobManager whether you'd like to reschedule
         * this job based on the retry criteria provided at job creation-time; or `false`
         * to end the job entirely.  Regardless of the value returned, your job must stop executing.
         */
        override fun onStopJob(params: JobParameters?): Boolean {
            return true
        }

    }


    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "onReceive: intent.extras --> ${intent?.extras}\nvvvvvvvvvvvvvvvvvvvv intent action --> ${intent?.action}")
        super.onReceive(context, intent)

        Log.i(TAG, "onReceive: ${intent?.data}")
        Log.d(TAG, "onReceive: intent.extras --> ${intent?.extras}")
        val testUri = intent?.data
        val widgetId = intent?.extras?.getInt(INT_WIDGET_KEY)?:0
        if (intent?.action.equals(ACTION_SIMPLEAPPWIDGET) && context != null) {
            Log.d(TAG, "onReceive: $ACTION_SIMPLEAPPWIDGET\n${intent?.extras}")
//            val appWidgetManager = getInstance(context)
//            val thisAppWidgetComponentName =
//                ComponentName(context.applicationContext, CryptoPriceAppWidget::class.java)
//            val appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidgetComponentName)
//
//            val b = goAsync()
//            Log.i(TAG, "onReceive: val b=goAsync()")
//            onUpdate(context, appWidgetManager, appWidgetIds)
//            Log.i(TAG, "onReceive: b.finish()")
//            b.finish()

            val jobInfo = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                JobInfo.Builder(0,ComponentName(context.applicationContext,UUUpdateService::class.java))
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
                    .setTransientExtras(bundleOf(INT_WIDGET_KEY to widgetId))
                    .setExtras(persistableBundleOf(INT_WIDGET_KEY to widgetId))
            } else {
                JobInfo.Builder(0,ComponentName(context.applicationContext,UUUpdateService::class.java))
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
                    .setExtras(persistableBundleOf(INT_WIDGET_KEY to widgetId))
            }
            //.setOverrideDeadline(20000)
                //.setMinimumLatency(2000)

            (context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler).schedule(jobInfo.build())


//            try {
//                val cn = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
//                    context.startForegroundService(Intent(context, UUUpdateService::class.java))
//                else
//                    context.startService(Intent(context, UUUpdateService::class.java))
//
//                if (cn != null) {
//                    Log.i(
//                        TAG,
//                        "onReceive: ${cn.className} ${cn.packageName}\n ${cn.shortClassName} ${cn.flattenToShortString()}"
//                    )
//                } else
//                    Log.i(TAG, "onReceive: startService return NULL!!!!!")
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
        } else {
            Log.i(TAG, "onReceive: Else ACTION_SIMPLEAPPWIDGET onReceive: intent.action --> ${intent?.action}")
        }
        Log.d(TAG, "onReceive: END")
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
                //.addInterceptor(
                //    HttpLoggingInterceptor().apply {
                //        level = HttpLoggingInterceptor.Level.BODY
                //    }
                //)
                .build()
            val jsonTecker = withContext(Dispatchers.IO) {
                Log.i(TAG, "onUpdate: before delay\nappWidgets: ${appWidgetIds.asList()}")
                //delay(14000)
                Log.i(TAG, "onUpdate: after delay")
                val request = Request.Builder()
                    .url("https://yobit.net/api/2/btc_usd/ticker")
                    .build()
                val response = client.newCall(request).execute()
                val respBodyString = response.body?.string() ?: ""
                Log.d(TAG, "onUpdate: JSONObject:\n$respBodyString")
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
    }

    private fun updAppWid(
        context: Context,
        appWidgetManager: AppWidgetManager,
        xappWidgetId: Int
    ) {
        var appWidgetId=xappWidgetId
        val widgetText = loadTitlePref(context, appWidgetId)
        val widgetUpdateTime = loadUPdateIntervalPref(context, appWidgetId)
        // Construct the RemoteViews object
        val views = RemoteViews(context.packageName, R.layout.crypto_price_app_widget)
        views.setTextViewText(R.id.appwidget_text, widgetText)

        views.setTextViewText(R.id.appwidget_text, yoServerTime)
        views.setTextViewText(R.id.btc_price, yoCu)
        views.setTextViewText(R.id.poPrice, poCu)
//        val pendingIntent: PendingIntent = Intent(context, MainActivity::class.java)
//            .let { intent ->
//                PendingIntent.getActivity(context, 0, intent, 0)
//            }
//        views.setOnClickPendingIntent(R.id.app_widget, pendingIntent)
        val pi: PendingIntent = Intent(context, CryptoPriceAppWidget::class.java)
            .let { intent: Intent ->
                intent.putExtra(INT_WIDGET_KEY,appWidgetId)
                intent.action = ACTION_SIMPLEAPPWIDGET
                PendingIntent.getBroadcast(context, appWidgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            }
        views.setOnClickPendingIntent(R.id.refresh_bt, pi)
        Log.i(
            TAG,
            "updAppWid: !!!!!!!!!!!!!!!!!! $appWidgetId !!!!!!!!!!!!!!!!!! views.setOnClickPendingIntent(R.id.refresh_bt, pi) !!!!!!!"
        )
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}


internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    xappWidgetId: Int
) {

    var appWidgetId = xappWidgetId
    val TAG = "updateAppWidget"
    val widgetText = loadTitlePref(context, appWidgetId)
    val widgetUpdateTime = loadTitlePref(context, appWidgetId)
    // Construct the RemoteViews object
    val views = RemoteViews(context.packageName, R.layout.crypto_price_app_widget)
    views.setTextViewText(R.id.appwidget_text, widgetText)

    //-------------
    GlobalScope.launch(Dispatchers.IO) {
        Log.i(TAG, "updateAppWidget: before delay")
        //delay(14000)
        Log.i(TAG, "updateAppWidget: after delay")

        val client = OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .callTimeout(10, TimeUnit.SECONDS)
//            .addInterceptor(
//                HttpLoggingInterceptor().apply {
//                    level = HttpLoggingInterceptor.Level.BODY
//                }
//            )
            .build()
        var request = Request.Builder()
            .url("https://yobit.net/api/2/btc_usd/ticker")
            .build()
        var response = client.newCall(request).execute()
        var respBodyString = response.body?.string() ?: ""
        Log.d(TAG, "updateAppWidget: JSONObject:\n$respBodyString")
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

//        val pendingIntent: PendingIntent = Intent(context, MainActivity::class.java)
//            .let { intent ->
//                PendingIntent.getActivity(context, 0, intent, 0)
//            }
//        views.setOnClickPendingIntent(R.id.app_widget, pendingIntent)

//        val pi: PendingIntent = Intent(context, CryptoPriceAppWidget::class.java)
//            .let { intent: Intent ->
//                intent.putExtra(INT_WIDGET_KEY,appWidgetId)
//                intent.action = ACTION_SIMPLEAPPWIDGET
//                PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
//            }
//        views.setOnClickPendingIntent(R.id.refresh_bt, pi)
        val pi: PendingIntent = Intent(context, CryptoPriceAppWidget::class.java)
            .let { _intent: Intent ->
                _intent.putExtra(INT_WIDGET_KEY,appWidgetId)
                _intent.action = ACTION_SIMPLEAPPWIDGET
                PendingIntent.getBroadcast(context,appWidgetId,_intent,PendingIntent.FLAG_UPDATE_CURRENT)
            }

        views.setOnClickPendingIntent(R.id.refresh_bt, pi)
        Log.i(
            TAG,
            "updateAppWidget: !!!!!!!!!!!!!!!!!!!!!!!!!!!!$appWidgetId!!!!!!!!!!!!!!!!!! views.setOnClickPendingIntent(R.id.refresh_bt, pi) !!!!!!!!!!!!!!!!!!!!!!!!!"
        )
        views.setTextViewText(R.id.textWidTitle,widgetText)
        views.setTextViewText(R.id.textWidTitleId,"$appWidgetId")

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
    //-------------

    // Instruct the widget manager to update the widget


}