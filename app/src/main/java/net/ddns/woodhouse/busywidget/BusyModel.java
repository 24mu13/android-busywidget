package net.ddns.woodhouse.busywidget;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class BusyModel {

    private static BusyModel ourInstance = new BusyModel();
    private static DatabaseReference appRef;

    public static BusyModel getInstance() {
        return ourInstance;
    }

    private BusyModel() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        isBusy = false; // un-initialized
        appRef = database.getReference("isbusy");
        appRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                isBusy = dataSnapshot.getValue(Boolean.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // log error to get value
            }
        });
    }

    public boolean isBusy() {
        return isBusy;
    }

    public void setBusy(boolean busy) {
//        isBusy = busy;
        appRef.setValue(busy);
    }

    private boolean isBusy;

}
