package net.ddns.woodhouse.busywidget;

import android.content.Context;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class ValueEventListenerWithContext implements ValueEventListener {

    protected Context context;
    ValueEventListenerWithContext(Context context){
        super();
        this.context = context;
    }

    public void onDataChange(DataSnapshot var1) {}

    public void onCancelled(DatabaseError var1) {}

}
