package com.example.multiplayertest;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    EditText editText;
    Button button;

    String  playerName = "";

    FirebaseDatabase database;
    DatabaseReference playerRef;

    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    boolean serviceBound = false;

    MediaPlayerService playerService;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        playerService = new MediaPlayerService();

        //Register BroadcastReceivers
        registerReceiver(broadcastReceiver, new IntentFilter("screen_on"));
        registerReceiver(broadcastReceiver, new IntentFilter("screen_off"));

        editText = findViewById(R.id.editText);
        button = findViewById(R.id.button);

        database = FirebaseDatabase.getInstance();


        //check if the player exists and get reference
        preferences = getSharedPreferences("PREFS", 0);

        playerName = preferences.getString("PlayerName", "");
        if(!playerName.equals("")){
        }

        editText.setText(playerName);

        //Start music
        if(!playerService.isNotNull(0)) {
            if(!playerService.isMpPlaying(0)) {
                playerService.initMediaPlayer(getApplicationContext(), 0, R.raw.main_uno_music, true);
            }
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //logging the player in and posting name to DB
                playerName = editText.getText().toString();
                editText.setText("");
                if(!playerName.equals("")){
                    playerRef = database.getReference("Players/" + playerName);
                    addEventListener();
                    playerRef.setValue("");
                    startActivity(new Intent(getApplicationContext(), RoomSelection.class));
                }
                else{
                    Toast.makeText(MainActivity.this, "Please Enter a Name", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

       private void  addEventListener(){
        //read from database
        playerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!playerName.equals("")){
                    preferences = getSharedPreferences("PREFS", 0);
                    editor = preferences.edit();
                    editor.putString("PlayerName", playerName);
                    editor.commit();
                //success - continue to the next activity after saving the player name
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //error
                Toast.makeText(MainActivity.this, "Error Logging in!", Toast.LENGTH_SHORT).show();
            }
        });
    }


    //Binding this Client to the MediaPlayer Service
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //Bound to LocalService, cast the IBinder and get LocalService instance
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            playerService = binder.getService();
            serviceBound = true;
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean("ServiceState", serviceBound);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        serviceBound = savedInstanceState.getBoolean("ServiceState");
    }


    //BroadcastReceiver that pauses or resumes Media on Screen Off-On
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context arg0, Intent intent) {
            String action = intent.getAction();
            if (action.equals("screen_off")) {
                playerService.pauseMedia();

            }
            if (action.equals("screen_on")) {
                playerService.resumeMedia();

            }
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        playerService.pauseMedia();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        playerService.resumeMedia();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serviceBound) {
            unbindService(serviceConnection);
            //service is active
            unregisterReceiver(broadcastReceiver);
            playerService.stopSelf();
        }
    }

}