package net.ddns.woodhouse.busywidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link BusyWidgetConfigureActivity BusyWidgetConfigureActivity}
 */
public class BusyWidgetProvider extends AppWidgetProvider {
    private static final String ACTION_CLICK = "net.ddns.woodhouse.busywidget.APPWIDGET_CLICK";

    protected static void updateWidget(Context context, AppWidgetManager manager, int widgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.busy_widget);

        String roomId = SettingWidget.load(context, widgetId);
        if (roomId != null) {
            views.setTextViewText(R.id.appwidget_text, roomId);
            views.setOnClickPendingIntent(R.id.buttonIsBusy, getPendingSelfIntent(
                    context, ACTION_CLICK, widgetId));

            BusyWidgetConfigureActivity.getRoom().registerWidget(widgetId, roomId, (
                    new ValueEventListenerWithContext(context) {
                        @Override
                        public void onValueChange(String roomId, int widgetId, Boolean value) {
                            AppWidgetManager manager = AppWidgetManager.getInstance(context);
                            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.busy_widget);
                            views.setImageViewResource(R.id.buttonIsBusy, value ?
                                    R.drawable.button_blank_red_01 : R.drawable.button_blank_green_01);
                            views.setOnClickPendingIntent(R.id.buttonIsBusy, getPendingSelfIntent(
                                    context, ACTION_CLICK, widgetId));
                            manager.updateAppWidget(widgetId, views);
                        }
                    }));

            manager.updateAppWidget(widgetId, views);
        }

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
            BusyWidgetConfigureActivity.getRoom().unregisterWidget(appWidgetId);
            SettingWidget.delete(context, appWidgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (intent.getAction().equals(ACTION_CLICK)) {
            int widgetId = intent.getIntExtra(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            BusyWidgetConfigureActivity.getRoom().toggleWidget(widgetId);
        }
    }

    protected static PendingIntent getPendingSelfIntent(Context context, String action, Integer widgetId) {
        Intent intent = new Intent(context, BusyWidgetProvider.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

}

