package com.example.multiplayertest;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
import android.media.Image;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class WaitingRoomModule extends ConstraintLayout {

    private static final String TAG = "VolumeDialogChildModule";

    public LayoutInflater inflater;
    public View rootView;

    TextView playerNameTextView;
    TextView numberOfCardsTextView;
    ImageView playerAvatar;


    // Initializing constructors
    public WaitingRoomModule(Context context) {
        this(context, null);
    }

    public WaitingRoomModule(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WaitingRoomModule(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        rootView = inflater.inflate(R.layout.activity_waiting_room_module, null);
        this.addView(rootView);

        playerNameTextView = findViewById(R.id.playerNameTextView);
        numberOfCardsTextView = findViewById(R.id.numberOfCardsTextView);
        playerAvatar = findViewById(R.id.playerAvatar);

    }

    // Sets player name
    public void setPlayerName(String playerName){
        playerNameTextView.setText(playerName);
    }
    // Returns player name
    public String getPlayerName(){
        return playerNameTextView.getText().toString();
    }
    // Sets player avatar
    public void setPlayerAvatar(Integer id){
        playerAvatar.setImageResource(id);

    }
    // Sets number of current cards in hand
    public void setNumberOfCards(String number){
        numberOfCardsTextView.setVisibility(VISIBLE);
        numberOfCardsTextView.setText(number);
    }
}