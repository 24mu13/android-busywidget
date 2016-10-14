package net.ddns.woodhouse.busywidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
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

    protected static void createWidget(Context context, AppWidgetManager manager, int widgetId) {
        updateWidget(context, manager, widgetId);

        // set the listener for db changing
        String roomId = BusyWidgetConfigureActivity.loadRoomId(context, widgetId);
        DatabaseReference ref = database.getReference(roomId);
        ref.addValueEventListener(new ValueEventListenerWithContext(context) {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String roomId = dataSnapshot.getRef().getKey();
                int widgetId = BusyWidgetConfigureActivity.getWidgetIdFromRoomId(roomId);
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
                        BusyWidgetConfigureActivity.getReferences().get(widgetId).setValue(false);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "Listening for change in room cancelled!", databaseError.toException());
            }
        });
        BusyWidgetConfigureActivity.getReferences().put(widgetId, ref);

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
            BusyWidgetConfigureActivity.getReferences().delete(appWidgetId);
            BusyWidgetConfigureActivity.deleteRoomId(context, appWidgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (intent.getAction().equals(ACTION_CLICK)) {
            int widgetId = intent.getIntExtra(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            DatabaseReference ref = BusyWidgetConfigureActivity.getReferences().get(widgetId);
            if (ref != null) {
                ref.addListenerForSingleValueEvent(
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
            else
                Log.e(TAG, "Error getting the related room");
        }

    }

    public BusyWidgetProvider() {
        super();
    }

    protected static PendingIntent getPendingSelfIntent(Context context, String action, Integer widgetId) {
        Intent intent = new Intent(context, BusyWidgetProvider.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

}

