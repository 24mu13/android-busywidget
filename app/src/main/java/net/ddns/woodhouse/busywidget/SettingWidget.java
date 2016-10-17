package net.ddns.woodhouse.busywidget;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingWidget {

    private static final String PREFS_NAME = "net.ddns.woodhouse.busywidget.BusyWidgetProvider";
    private static final String PREF_PREFIX_KEY = "bw_room_";

    static void save(Context context, int widgetId, String roomId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putString(PREF_PREFIX_KEY + widgetId, roomId);
        prefs.apply();
    }

    static String load(Context context, int widgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        return prefs.getString(PREF_PREFIX_KEY + widgetId, null);
    }

    static void delete(Context context, int widgetId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.remove(PREF_PREFIX_KEY + widgetId);
        prefs.apply();
    }

}
