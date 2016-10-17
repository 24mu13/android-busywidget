package net.ddns.woodhouse.busywidget;

import android.appwidget.AppWidgetManager;
import android.util.Log;
import android.util.SparseArray;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static android.content.ContentValues.TAG;


class RoomStorage {
    private FirebaseDatabase database;
    private SparseArray<DatabaseReference> references;

    RoomStorage() {
        references = new SparseArray<>();
        Log.i(TAG, "Connecting Firebase..");
        database = FirebaseDatabase.getInstance();
        Log.i(TAG, "Model initialized.");
    }

    void registerWidget(int widgetId, String roomId, ValueEventListenerWithContext listener) {

        // room look-up
        DatabaseReference ref = database.getReference(roomId);
        ref.addListenerForSingleValueEvent(
            new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.exists())
                        dataSnapshot.getRef().setValue(false);
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.w(TAG, "Error while looking up for room!", databaseError.toException());
                }
            });

        // add defined listener
        ref.addValueEventListener(listener);

        // add the value to the map
        references.put(widgetId, ref);

    }

    void unregisterWidget(int widgetId) {
        references.delete(widgetId);
    }

    void toggleWidget(int widgetId) {
        DatabaseReference ref = references.get(widgetId);
        if (ref != null) {
            ref.addListenerForSingleValueEvent(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            dataSnapshot.getRef().setValue(!dataSnapshot.getValue(Boolean.class));
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.w(TAG, "(Un)reservation room cancelled!", databaseError.toException());
                        }
                    });
        }
        else
            Log.e(TAG, "Error getting the related room");
    }

    int getWidgetId(String roomId) {
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

}
