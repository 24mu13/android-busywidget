package net.ddns.woodhouse.busywidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.util.SparseArray;
import android.widget.RemoteViews;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static android.content.ContentValues.TAG;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link BusyWidgetConfigureActivity BusyWidgetConfigureActivity}
 */
public class BusyWidgetProvider extends AppWidgetProvider {
    private static final String ACTION_CLICK = "net.ddns.woodhouse.busywidget.APPWIDGET_CLICK";

    private static FirebaseDatabase database;
    private static SparseArray<DatabaseReference> references;

    private static int getWidgetIdFromRoomId(SparseArray<DatabaseReference> references, String roomId) {
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

    protected static void updateWidget(Context context, AppWidgetManager manager, int widgetId) {
        if (database == null) {
            database = FirebaseDatabase.getInstance();
            Log.i(TAG, "Connected to Firebase.");
        }
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.busy_widget);

        // set the widget layout (id + image/state)
        String roomId = BusyWidgetConfigureActivity.loadRoomId(context, widgetId);
        views.setTextViewText(R.id.appwidget_text, roomId);

        // set the intent to change the state
        views.setOnClickPendingIntent(R.id.buttonIsBusy, getPendingSelfIntent(
                context, ACTION_CLICK, widgetId));

        // actually updates the view
        manager.updateAppWidget(widgetId, views);

        // set the listener for db changing
        references.put(widgetId, database.getReference(roomId));
        references.get(widgetId).addValueEventListener(new ValueEventListenerWithContext(context) {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String roomId = dataSnapshot.getRef().getKey();
                int widgetId = getWidgetIdFromRoomId(references, roomId);
                if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID)
                    if (dataSnapshot.exists()) {
                        AppWidgetManager manager = AppWidgetManager.getInstance(context);
                        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.busy_widget);
                        views.setImageViewResource(R.id.buttonIsBusy, dataSnapshot.getValue(Boolean.class) ?
                                R.drawable.button_blank_red_01 : R.drawable.button_blank_green_01);
                        views.setOnClickPendingIntent(R.id.buttonIsBusy, getPendingSelfIntent(
                                context, ACTION_CLICK, widgetId));
                        manager.updateAppWidget(widgetId, views);
                    }
                    else
                        references.get(widgetId).setValue(false);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "Listening for change in room cancelled!", databaseError.toException());
            }
        });

    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            references.delete(appWidgetId);
            BusyWidgetConfigureActivity.deleteRoomId(context, appWidgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (ACTION_CLICK.equals(intent.getAction())) {
            int widgetId = intent.getIntExtra(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            references.get(widgetId).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        dataSnapshot.getRef().setValue(!dataSnapshot.getValue(Boolean.class));
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "Updating room cancelled!", databaseError.toException());
                    }
                });
        }

    }

    public BusyWidgetProvider() {
        super();
        references = new SparseArray<>();
    }

    protected static PendingIntent getPendingSelfIntent(Context context, String action, Integer widgetId) {
        Intent intent = new Intent(context, BusyWidgetProvider.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }

}

