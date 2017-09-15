package net.ddns.woodhouse.busywidget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import static android.content.ContentValues.TAG;

public class ValueEventListenerWithContext implements ValueEventListener {

    protected Context context;
    ValueEventListenerWithContext(Context context){
        super();
        this.context = context;
    }

    public void onValueChange(String roomId, int widgetId, Boolean value) {}

    public void onDataChange(DataSnapshot dataSnapshot) {
        String roomId = dataSnapshot.getRef().getKey();
        int widgetId = BusyWidgetConfigureActivity.getRoom().getWidgetId(roomId);
        if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID)
            if (dataSnapshot.exists())
                onValueChange(roomId, widgetId, dataSnapshot.getValue(Boolean.class));
    }

    public void onCancelled(DatabaseError databaseError) {
        Log.w(TAG, "Listening for change in room cancelled!", databaseError.toException());
    }

}
