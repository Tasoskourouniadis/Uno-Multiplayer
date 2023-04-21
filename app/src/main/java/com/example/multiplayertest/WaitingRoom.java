package com.example.multiplayertest;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class WaitingRoom extends AppCompatActivity {

    private static final String TAG = "WaitingRoom";

    TextView roomNameTextView;
    Button exitButton;
    Button readyButton;
    Button twoPlayersButton;
    Button fourPlayersButton;

    ArrayList playerList;
    ArrayList readyList;

    String playerName = "";
    String roomName = "";
    String role = "";

    WaitingRoomModule hostPlayerModule;
    WaitingRoomModule guestPlayerModule1;
    WaitingRoomModule guestPlayerModule2;
    WaitingRoomModule guestPlayerModule3;


    FirebaseDatabase database;
    DatabaseReference readyRef;
    DatabaseReference playerRef;
    DatabaseReference dbCallRef;
    DatabaseReference noOfPlayers;
    ValueEventListener readyRefListener;
    ValueEventListener playerRefListener;

    SharedPreferences preferences;

    boolean isReady = false;
    boolean ready = false;
    boolean dbEntryDefined = false;
    Integer playerNo=2;

    MediaPlayerService playerService;
    PowerManager pm;
    Boolean isScreenOn;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting_room);


        roomNameTextView = findViewById(R.id.roomNameTextView);
        exitButton = findViewById(R.id.exitButton);
        readyButton = findViewById(R.id.readyButton);
        hostPlayerModule = findViewById(R.id.playerPlayerModule);
        guestPlayerModule1 = findViewById(R.id.guestPlayerModule1);
        guestPlayerModule2 = findViewById(R.id.guestPlayerModule2);
        guestPlayerModule3 = findViewById(R.id.guestPlayerModule3);
        twoPlayersButton = findViewById(R.id.twoPlayersButton);
        fourPlayersButton = findViewById(R.id.fourPlayersButton);

        playerList = new ArrayList<String>();
        readyList = new ArrayList<String>();

        database = FirebaseDatabase.getInstance();

        // Get player and room info
        preferences = getSharedPreferences("PREFS", 0);
        playerName = preferences.getString("PlayerName", "");
        roomName = getIntent().getStringExtra("roomName");

        roomNameTextView.setText("Room: " + roomName);

        playerService = new MediaPlayerService();

        //Assign player roles (host or guest)
        Bundle extras = getIntent().getExtras();
        if(extras != null){
            roomName = extras.getString("roomName");
            if(roomName.equals(playerName)){
                role = "host";
                registerReceiver(broadcastReceiver, new IntentFilter("game_ended"));
            }else{
                role = "guest";
                fourPlayersButton.setVisibility(View.GONE);
                twoPlayersButton.setVisibility(View.GONE);
            }
        }


        //Assign player name and avatar as soon as they join the group
        if(role.equals("host")){
            hostPlayerModule.setPlayerName(playerName);
            hostPlayerModule.setPlayerAvatar(R.drawable.avatar1_128);
            readyRef = database.getReference("Rooms/" + roomName + "/status/" + playerName+ "/");
            readyRef.setValue("0");

        }

        // When leaving room delete appropriate data based on role
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(role.equals("guest"))
                {
                    removeListeners();
                    database.getReference("/Rooms/" + roomName + "/Players/" + playerName).removeValue();
                    database.getReference("/Rooms/" + roomName + "/status/" + playerName).removeValue();
                }else{
                    removeListeners();
                    database.getReference("/Rooms/" + roomName + "/Status/").removeValue();
                    database.getReference("/Rooms/" + roomName + "/Players/").removeValue();
                    database.getReference("/Rooms/" + roomName + "/").removeValue();
                }
                Intent intent = new Intent(getApplicationContext(), RoomSelection.class);
                startActivity(intent);
            }
        });

        readyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //No 1 stands as Ready and 0 as Not Ready
                if(!isReady) {
                    if(role.equals("host")) {
                        readyRef = database.getReference("Rooms/" + roomName + "/status/" + playerName+ "/");
                    }else{
                        readyRef = database.getReference("Rooms/" + roomName + "/status/" + playerName+ "/");
                    }
                    readyRef.setValue("1");
                    isReady = true;
                }else{
                    if(role.equals("host")) {
                        readyRef = database.getReference("Rooms/" + roomName + "/status/" + playerName + "/");
                    }else{
                        readyRef = database.getReference("Rooms/" + roomName + "/status/" + playerName+ "/");
                    }
                    readyRef.setValue("0");
                    isReady = false;
                }
            }
        });

        // Sets game mode to two players
        twoPlayersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                twoPlayersButton.setBackgroundColor(getResources().getColor(R.color.button_active));
                fourPlayersButton.setBackgroundColor(getResources().getColor(R.color.button_inactive));
                playerNo=2;
                noOfPlayers = database.getReference("Rooms/" + roomName + "/NoOfPlayers/");
                noOfPlayers.setValue(playerNo);
            }
        });

        // Sets game mode to four players
        fourPlayersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fourPlayersButton.setBackgroundColor(getResources().getColor(R.color.button_active));
                twoPlayersButton.setBackgroundColor(getResources().getColor(R.color.button_inactive));
                playerNo=4;
                noOfPlayers = database.getReference("Rooms/" + roomName + "/NoOfPlayers/");
                noOfPlayers.setValue(playerNo);
            }
        });

        addPlayersEventListener();
        addReadyEventListener();
        numberOfPlayersListener();
    }

    // Listens to how many players joined the room
    private void numberOfPlayersListener(){
        noOfPlayers = database.getReference("Rooms/" + roomName + "/NoOfPlayers/");
        noOfPlayers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue() != null){
                    playerNo = ((Long)snapshot.getValue()).intValue();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void removeListeners() {
        playerRef.removeEventListener(playerRefListener);
        readyRef.removeEventListener(readyRefListener);
    }


    //Gets the list of players in the room and assigns roles and details
    public void addPlayersEventListener(){
        playerRef = database.getReference("Rooms/" + roomName + "/Players/");
        playerRefListener= playerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //show list of rooms
                playerList.clear();
                Iterable<DataSnapshot> players = dataSnapshot.getChildren();
                for(DataSnapshot snapshot : players){
                        playerList.add(snapshot.getKey());
                }
                if(playerList.size() == 1){
                    dbEntryDefined=false;
                }
                if(!dbEntryDefined) {
                    if (role.equals("guest")) {
                        Log.e(TAG, "onCreate: " + playerList);
                        dbCallRef = database.getReference("Rooms/" + roomName +"/Players/"+ playerName);
                        dbCallRef.setValue("", 1);
                        readyRef = database.getReference("Rooms/" + roomName + "/status/" + playerName + "/");
                        readyRef.setValue("0",1);
                dbEntryDefined = true;
                    }
                }


                if(role.equals("host")){
                    for (int i = 0; i < playerList.size(); i++) {
                            switch (i) {
                                case 0:
                                    guestPlayerModule1.setVisibility(View.INVISIBLE);
                                    guestPlayerModule2.setVisibility(View.INVISIBLE);
                                    guestPlayerModule3.setVisibility(View.INVISIBLE);
                                    break;
                                case 1:
                                    guestPlayerModule1.setVisibility(View.VISIBLE);
                                    guestPlayerModule1.setPlayerAvatar(R.drawable.avatar2_128);
                                    guestPlayerModule1.setPlayerName(playerList.get(i).toString());
                                    guestPlayerModule2.setVisibility(View.INVISIBLE);
                                    guestPlayerModule3.setVisibility(View.INVISIBLE);
                                    break;
                                case 2:
                                    guestPlayerModule1.setVisibility(View.VISIBLE);
                                    guestPlayerModule1.setPlayerAvatar(R.drawable.avatar2_128);
                                    guestPlayerModule1.setPlayerName(playerList.get(1).toString());
                                    guestPlayerModule2.setVisibility(View.VISIBLE);
                                    guestPlayerModule2.setPlayerAvatar(R.drawable.avatar3_128);
                                    guestPlayerModule2.setPlayerName(playerList.get(i).toString());
                                    guestPlayerModule3.setVisibility(View.INVISIBLE);
                                    break;
                                case 3:
                                    guestPlayerModule1.setVisibility(View.VISIBLE);
                                    guestPlayerModule1.setPlayerAvatar(R.drawable.avatar2_128);
                                    guestPlayerModule1.setPlayerName(playerList.get(1).toString());
                                    guestPlayerModule2.setVisibility(View.VISIBLE);
                                    guestPlayerModule2.setPlayerAvatar(R.drawable.avatar3_128);
                                    guestPlayerModule2.setPlayerName(playerList.get(2).toString());
                                    guestPlayerModule3.setVisibility(View.VISIBLE);
                                    guestPlayerModule3.setPlayerAvatar(R.drawable.avatar4_128);
                                    guestPlayerModule3.setPlayerName(playerList.get(i).toString());
                                    break;
                            }
                    }
                }

                if(role.equals("guest") && (playerList.size()==0)){
                    Toast.makeText(WaitingRoom.this, "Your Host has Left the group!", Toast.LENGTH_SHORT).show();
                    finishActivity();
                }

                if(role.equals("guest")) {
                    for (int i = 1; i < playerList.size(); i++) {
                        switch (i) {
                            case 1:
                                hostPlayerModule.setPlayerAvatar(R.drawable.avatar1_128);
                                hostPlayerModule.setPlayerName(playerList.get(0).toString());
                                guestPlayerModule1.setVisibility(View.VISIBLE);
                                guestPlayerModule1.setPlayerAvatar(R.drawable.avatar2_128);
                                guestPlayerModule1.setPlayerName(playerList.get(i).toString());
                                guestPlayerModule2.setVisibility(View.INVISIBLE);
                                guestPlayerModule3.setVisibility(View.INVISIBLE);
                                break;
                            case 2:
                                hostPlayerModule.setPlayerAvatar(R.drawable.avatar1_128);
                                hostPlayerModule.setPlayerName(playerList.get(0).toString());
                                guestPlayerModule1.setVisibility(View.VISIBLE);
                                guestPlayerModule1.setPlayerAvatar(R.drawable.avatar2_128);
                                guestPlayerModule1.setPlayerName(playerList.get(1).toString());
                                guestPlayerModule2.setVisibility(View.VISIBLE);
                                guestPlayerModule2.setPlayerAvatar(R.drawable.avatar3_128);
                                guestPlayerModule2.setPlayerName(playerList.get(i).toString());
                                guestPlayerModule3.setVisibility(View.INVISIBLE);
                                break;
                            case 3:
                                hostPlayerModule.setPlayerAvatar(R.drawable.avatar1_128);
                                hostPlayerModule.setPlayerName(playerList.get(0).toString());
                                guestPlayerModule1.setVisibility(View.VISIBLE);
                                guestPlayerModule1.setPlayerAvatar(R.drawable.avatar2_128);
                                guestPlayerModule1.setPlayerName(playerList.get(1).toString());
                                guestPlayerModule2.setVisibility(View.VISIBLE);
                                guestPlayerModule2.setPlayerAvatar(R.drawable.avatar3_128);
                                guestPlayerModule2.setPlayerName(playerList.get(2).toString());
                                guestPlayerModule3.setVisibility(View.VISIBLE);
                                guestPlayerModule3.setPlayerAvatar(R.drawable.avatar4_128);
                                guestPlayerModule3.setPlayerName(playerList.get(i).toString());
                                break;
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //error - nothing
            }
        });
    }

    // Keeps track of player status and when everyone is ready starts the game
    public void addReadyEventListener(){
        readyRef = database.getReference("Rooms/" + roomName + "/status");
        readyRefListener = readyRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                readyList.clear();
                Iterable<DataSnapshot> playerStatus = dataSnapshot.getChildren();
                for(DataSnapshot snapshot : playerStatus){
                    readyList.add(snapshot.getValue());
                }
                if(isReady){
                    readyButton.setAlpha(0.5F);

                }else {
                    readyButton.setAlpha(1F);
                }

                if(readyList.size() == playerNo) {
                    for(Object readyStatus : readyList){
                        ready = readyStatus.toString().equals("1");
                        if(!ready){break;}
                    }
                }
                    if(ready){
                        Intent intent = new Intent(getApplicationContext(), GameRoom.class);
                        intent.putExtra("roomName", roomName);
                        startActivity(intent);
                        ready = false;
                    }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        isScreenOn = pm.isScreenOn();
        if(!isScreenOn) {
            Intent intent = new Intent("screen_off");
            sendBroadcast(intent);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        isScreenOn = pm.isScreenOn();
        if(isScreenOn) {
            Intent intent = new Intent("screen_on");
            sendBroadcast(intent);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (playerRefListener != null && readyRefListener != null &&database != null) {
            if (role.equals("host")) {
                database.getReference().child("/Rooms").child(playerName).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                    }
                });
            }
        }
    }

    // When a game ends and host returns to waiting room, resets game data
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context arg0, Intent intent) {
            String action = intent.getAction();
            if (action.equals("game_ended")) {
                Log.e(TAG, "onReceive: " );
                //resetRoom
                if(role.equals("host")){
                    database.getReference("Rooms/" + roomName + "/CurrentCard/").removeValue();
                    database.getReference("Rooms/" + roomName + "/GameRotation/").removeValue();
                    database.getReference("Rooms/" + roomName + "/IsCardDraw/").removeValue();
                    database.getReference("Rooms/" + roomName + "/NewColour/").removeValue();
                    database.getReference("Rooms/" + roomName + "/PassTurn/").removeValue();
                    database.getReference("Rooms/" + roomName + "/PlayerCardNo/").removeValue();
                    database.getReference("Rooms/" + roomName + "/UnoClicked/").removeValue();
                    database.getReference("Rooms/" + roomName + "/PlayerTurn/").removeValue();
                }

            }
                readyRef = database.getReference("Rooms/" + roomName + "/status/" + playerName+ "/");
                readyRef.setValue("0");
                readyButton.setAlpha(1F);
        }
    };


    private void finishActivity(){
        startActivity(new Intent(getApplicationContext(), RoomSelection.class));
        finish();

    }

}