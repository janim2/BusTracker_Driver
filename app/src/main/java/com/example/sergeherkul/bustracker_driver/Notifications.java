package com.example.sergeherkul.bustracker_driver;

import android.app.Notification;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.example.sergeherkul.bustracker_driver.Adapters.NotifyAdapter;
import com.example.sergeherkul.bustracker_driver.Model.Notify;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;

public class Notifications extends AppCompatActivity {

    private ArrayList notificationsArray = new ArrayList<Notify>();
    private RecyclerView notifications_RecyclerView;
    private RecyclerView.Adapter notifications_Adapter;
    private String school_id, driver_code, notification_title, notifications_message, notifications_time,
            notificationImage;

    private Accessories notifications_accessor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);
        getSupportActionBar().setTitle("Notifications");

        notifications_accessor = new Accessories(Notifications.this);

        school_id = notifications_accessor.getString("school_code");
        driver_code = notifications_accessor.getString("driver_code");

        notifications_RecyclerView = findViewById(R.id.notifications_recyclerView);
        //reviews adapter settings starts here
        if(isNetworkAvailable()){
            getNotifications_ID();
        }else{
            Toast.makeText(Notifications.this,"No Internet Connection",Toast.LENGTH_LONG).show();
        }
        notifications_RecyclerView.setHasFixedSize(true);

        notifications_Adapter = new NotifyAdapter(getNotificationsFromDatabase(),Notifications.this);
        notifications_RecyclerView.setAdapter(notifications_Adapter);
    }

    private void getNotifications_ID() {
        try{
            DatabaseReference notifications = FirebaseDatabase.getInstance().getReference("notifications").child(school_id).child(driver_code);

            notifications.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        for(DataSnapshot child : dataSnapshot.getChildren()){
                            Fetch_Notifications(child.getKey());
                        }
                    }else{
//                    Toast.makeText(getActivity(),"Cannot get ID",Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(Notifications.this,"Cancelled",Toast.LENGTH_LONG).show();
                }
            });
        }catch (NullPointerException e){

        }
    }

    private void Fetch_Notifications(String key) {
        DatabaseReference getNotifications = FirebaseDatabase.getInstance().getReference("notifications").child(school_id).child(driver_code).child(key);
        getNotifications.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot child : dataSnapshot.getChildren()){
                        if(child.getKey().equals("title")){
                            notification_title = child.getValue().toString();
                        }

                        if(child.getKey().equals("message")){
                            notifications_message = child.getValue().toString();
                        }

                        if(child.getKey().equals("time")){
                            notifications_time = child.getValue().toString();
                        }

                        if(child.getKey().equals("image")){
                            notificationImage = child.getValue().toString();
                        }

                        else{
//                            Toast.makeText(getActivity(),"Couldn't fetch posts",Toast.LENGTH_LONG).show();

                        }
                    }

                    Notify obj = new Notify(notification_title,notifications_message,notifications_time,notificationImage);
                    notificationsArray.add(obj);
                    notifications_RecyclerView.setAdapter(notifications_Adapter);
                    notifications_Adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(Notifications.this,"Cancelled",Toast.LENGTH_LONG).show();

            }
        });
    }

    public ArrayList<Notify> getNotificationsFromDatabase(){
        return  notificationsArray;
    }



    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
