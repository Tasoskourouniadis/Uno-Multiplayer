package com.example.multiplayertest;

import static android.content.ContentValues.TAG;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class RoomSelection extends AppCompatActivity {

    private static final String TAG = "RoomSelection";

    ListView listView;
    Button createRoomButton;
    Button logOutButton;
    TextView nameTextView;
    ImageButton syncImageButton;

    List<String> roomsList;
    ArrayList playerList;

    String playerName="";
    String roomName="";

    FirebaseDatabase database;
    DatabaseReference roomRef;
    DatabaseReference roomsRef;

    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    CircularProgressIndicator progressIndicator;
    PowerManager pm;
    boolean isScreenOn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_selection);

        database = FirebaseDatabase.getInstance();

        //get the player name and assign it as room name
        preferences = getSharedPreferences("PREFS", 0);
        playerName = preferences.getString("PlayerName", "");
        roomName = playerName;

        createRoomButton = findViewById(R.id.createRoomButton);
        logOutButton = findViewById(R.id.logOutButton);
        nameTextView = findViewById(R.id.nameTextView);
        listView = findViewById(R.id.roomlistView);
        progressIndicator = findViewById(R.id.progressCircular);
        syncImageButton = findViewById(R.id.syncImageButton);


        progressIndicator.setVisibility(View.GONE);

        playerList = new ArrayList();
        //all existing available rooms
        roomsList= new ArrayList<>();
        nameTextView.setText("Logged in as: " + playerName);

        createRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //create room and add yourself as host
                createRoomButton.setText("CREATING ROOM");
                createRoomButton.setEnabled(false);
                roomName = playerName;
                roomRef= database.getReference("Rooms/" + roomName +"/Players/"+ playerName);
                roomRef.setValue("");
                joinRoom();
            }
        });

        //Logs out the player by deleting the name from DB and resetting shared prefs
        logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    progressIndicator.setVisibility(View.VISIBLE);
                    editor = preferences.edit();
                    editor.putString("PlayerName", "");
                    editor.commit();
                    database.getReference().child("/Players").child(playerName).removeValue();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();


            }
        });

        //Join the selected room
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                roomName = roomsList.get(i);
                joinRoom();
            }
        });

        syncImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addRoomsEventListener();
            }
        });

        //show if new room is available
        addRoomsEventListener();
    }



    private void joinRoom(){
                createRoomButton.setText("CREATE ROOM");
                createRoomButton.setEnabled(true);
                Intent intent = new Intent(getApplicationContext(), WaitingRoom.class);
                intent.putExtra("roomName", roomName);
                startActivity(intent);
                finish();
    }

    //Listener that add new rooms to the listView
    private void addRoomsEventListener(){
        roomsRef = database.getReference("Rooms");
        roomsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //show list of rooms
                roomsList.clear();
                listView.setAdapter(null);
                Iterable<DataSnapshot> rooms = dataSnapshot.getChildren();
                for(DataSnapshot snapshot : rooms){
                    roomsList.add(snapshot.getKey());
                    ArrayAdapter<String> adapter = new ArrayAdapter(RoomSelection.this,android.R.layout.simple_list_item_1, roomsList){
                        @Override
                        public View getView(int position, View convertView, ViewGroup parent) {
                            View view = super.getView(position, convertView, parent);
                            TextView text = (TextView) view.findViewById(android.R.id.text1);
                                text.setTextColor(Color.WHITE);
                            return view;
                        }
                    };
                    listView.setAdapter(adapter);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //error - nothing
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        isScreenOn = pm.isScreenOn();
        if(!isScreenOn) {
            //Send an intent that the screen is off so the music stops
            Intent intent = new Intent("screen_off");
            sendBroadcast(intent);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        isScreenOn = pm.isScreenOn();
        if (isScreenOn) {
            //Send an intent that the screen is off so the music starts
            Intent intent = new Intent("screen_on");
            sendBroadcast(intent);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


}