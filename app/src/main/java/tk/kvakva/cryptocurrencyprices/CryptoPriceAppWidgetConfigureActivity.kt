package tk.kvakva.cryptocurrencyprices

//import android.app.Activity
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.os.persistableBundleOf
import kotlinx.android.synthetic.main.crypto_price_app_widget_configure.*

private const val TAG = "WidgetConfigureActivity"
/**
 * The configuration screen for the [CryptoPriceAppWidget] AppWidget.
 */
class CryptoPriceAppWidgetConfigureActivity : AppCompatActivity() {
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    //private lateinit var appWidgetText: EditText
    private var onClickListener = View.OnClickListener {
        //val context = this@NewAppWidgetConfigureActivity
        val context = this@CryptoPriceAppWidgetConfigureActivity

        // When the button is clicked, store the string locally
        val widgetText = appwidget_ctext.text.toString()
        val twidgetText = textLayoytInterval.text.toString().toLongOrNull()?:0
        saveTitlePref(context, appWidgetId, widgetText, twidgetText)

        // It is the responsibility of the configuration activity to update the app widget
        val appWidgetManager = AppWidgetManager.getInstance(context)
        updateAppWidget(context, appWidgetManager, appWidgetId)

        Log.i(TAG, "OnClickListener: ************************ .setPeriodic($twidgetText * 1000)")
                    val jobInfo = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                JobInfo.Builder(appWidgetId,ComponentName(context.applicationContext,
                    CryptoPriceAppWidget.UUUpdateService::class.java)
                )
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
                    .setPeriodic(twidgetText * 1000)
                    .setTransientExtras(bundleOf(INT_WIDGET_KEY to appWidgetId))
                    .setExtras(persistableBundleOf(INT_WIDGET_KEY to appWidgetId))
            } else {
                JobInfo.Builder(0, ComponentName(context.applicationContext,
                    CryptoPriceAppWidget.UUUpdateService::class.java)
                )
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
                    .setPeriodic(twidgetText * 1000)
                    .setExtras(persistableBundleOf(INT_WIDGET_KEY to appWidgetId))
            }
            //.setOverrideDeadline(20000)
                //.setMinimumLatency(2000)

            (context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler).schedule(jobInfo.build())

        // Make sure we pass back the original appWidgetId
        val resultValue = Intent()
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(RESULT_OK, resultValue)
        finish()
    }

    public override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED)

        setContentView(R.layout.crypto_price_app_widget_configure)
        //appWidgetText = findViewById<View>(R.id.appwidget_text) as EditText
        findViewById<View>(R.id.add_button).setOnClickListener(onClickListener)

        // Find the widget id from the intent.
        val intent = intent
        val extras = intent.extras
        if (extras != null) {
            appWidgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        appwidget_ctext.setText(
            loadTitlePref(
                this@CryptoPriceAppWidgetConfigureActivity,
                appWidgetId
            )
        )

        textLayoytInterval.setText(loadUPdateIntervalPref(
            this@CryptoPriceAppWidgetConfigureActivity,
            appWidgetId).toString()
        )
        
    }

}

private const val PREFS_NAME = "tk.kvakva.cryptocurrencyprices.CryptoPriceAppWidget"
private const val PREF_PREFIX_KEY = "appwidget_"
private const val PREF_PREFIX_TKEY = "tappwidget_"

// Write the prefix to the SharedPreferences object for this widget
internal fun saveTitlePref(context: Context, appWidgetId: Int, text: String, updTimeInterv: Long) {
    val prefs = context.getSharedPreferences(PREFS_NAME, 0).edit()
    prefs.putString(PREF_PREFIX_KEY + appWidgetId, text)
    prefs.putLong(PREF_PREFIX_TKEY + appWidgetId, updTimeInterv)
    prefs.apply()
}

// Read the prefix from the SharedPreferences object for this widget.
// If there is no preference saved, get the default from a resource
internal fun loadTitlePref(context: Context, appWidgetId: Int): String {
    val prefs = context.getSharedPreferences(PREFS_NAME, 0)
    val titleValue = prefs.getString(PREF_PREFIX_KEY + appWidgetId, "YoBit.NET & PoLoniex.COM")
    //return titleValue ?: context.getString(R.string.appwidget_text)
    return titleValue ?: "YoBit.NET & PoLoniex.COM"
}

internal fun loadUPdateIntervalPref(context: Context, appWidgetId: Int): Long {
    val prefs = context.getSharedPreferences(PREFS_NAME, 0)
    val timeValue = prefs.getLong(PREF_PREFIX_TKEY + appWidgetId, 0)
    Log.d("WIDPREFLOad", "loadUPdateIntervalPref: $timeValue")
    //return titleValue ?: context.getString(R.string.appwidget_text)
    return timeValue
}

internal fun deleteTitlePref(context: Context, appWidgetId: Int) {
    val prefs = context.getSharedPreferences(PREFS_NAME, 0).edit()
    prefs.remove(PREF_PREFIX_KEY + appWidgetId)
    prefs.remove(PREF_PREFIX_TKEY + appWidgetId)
    prefs.apply()
}