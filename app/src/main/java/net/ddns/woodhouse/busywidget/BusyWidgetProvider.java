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

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        CharSequence widgetText = BusyWidgetConfigureActivity.loadTitlePref(context, appWidgetId);

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.busy_widget);
        views.setTextViewText(R.id.appwidget_text, widgetText);

        // Set the appropriate value for the state
        BusyModel.getInstance().setBusy(!BusyModel.getInstance().isBusy());
        views.setImageViewResource(R.id.buttonIsBusy, (BusyModel.getInstance().isBusy()) ?
                R.drawable.button_blank_red_01 : R.drawable.button_blank_green_01);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);

            // Set the click listener to simply update
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.busy_widget);
            Intent intent = new Intent(context, BusyWidgetProvider.class);
            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                    0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            views.setOnClickPendingIntent(R.id.buttonIsBusy, pendingIntent);

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        for (int appWidgetId : appWidgetIds) {
            BusyWidgetConfigureActivity.deleteTitlePref(context, appWidgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

//        if (UPDATE_INTENT.equals(intent.getAction())) {
//            BusyModel.getInstance().setBusy(!BusyModel.getInstance().isBusy());
//        }

    }

}

