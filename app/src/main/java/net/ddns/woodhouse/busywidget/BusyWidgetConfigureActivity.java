package net.ddns.woodhouse.busywidget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.View;
import android.widget.EditText;
import com.google.firebase.database.DatabaseReference;
import java.util.UUID;

/**
 * The configuration screen for the {@link BusyWidgetProvider BusyWidgetProvider} AppWidget.
 */
public class BusyWidgetConfigureActivity extends Activity {

    private static final String PREFS_NAME = "net.ddns.woodhouse.busywidget.BusyWidgetProvider";
    private static final String PREF_PREFIX_KEY = "bw_room_";

    private static SparseArray<DatabaseReference> references = new SparseArray<>();
    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    EditText mAppWidgetText;

    View.OnClickListener mOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            final Context context = BusyWidgetConfigureActivity.this;

            // When the button is clicked, store the string locally
            String roomId = mAppWidgetText.getText().toString();
            saveRoomId(context, mAppWidgetId, roomId);

            // It is the responsibility of the configuration activity to update the app widget
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            BusyWidgetProvider.createWidget(context, appWidgetManager, mAppWidgetId);

            // Make sure we pass back the original appWidgetId
            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            setResult(RESULT_OK, resultValue);
            finish();
        }
    };

    public BusyWidgetConfigureActivity() {
        super();
    }

    static int getWidgetIdFromRoomId(String roomId) {
        int result = AppWidgetManager.INVALID_APPWIDGET_ID;
        for(int i = 0; i < references.size(); i++) {
            int key = references.keyAt(i);
            DatabaseReference ref = references.get(key);
            if (ref.getKey().equals(roomId)) {
                result = key;
                break;
            }
        }
        return result;
    }

    static SparseArray<DatabaseReference> getReferences() {
        return references;
    }

    // Write the prefix to the SharedPreferences object for this widget
    static void saveRoomId(Context context, int appWidgetId, String id) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putString(PREF_PREFIX_KEY + appWidgetId, id);
        prefs.apply();
    }

    // Read the prefix from the SharedPreferences object for this widget.
    // If there is no preference saved, get the default from a resource
    static String loadRoomId(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        String roomId = prefs.getString(PREF_PREFIX_KEY + appWidgetId, null);
        if (roomId != null) {
            return roomId;
        } else {
            return UUID.randomUUID().toString();
        }
    }

    static void deleteRoomId(Context context, int appWidgetId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.remove(PREF_PREFIX_KEY + appWidgetId);
        prefs.apply();
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED);

        setContentView(R.layout.busy_widget_configure);
        mAppWidgetText = (EditText) findViewById(R.id.appwidget_text);
        findViewById(R.id.add_button).setOnClickListener(mOnClickListener);

        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        mAppWidgetText.setText(loadRoomId(BusyWidgetConfigureActivity.this, mAppWidgetId));
    }
}

