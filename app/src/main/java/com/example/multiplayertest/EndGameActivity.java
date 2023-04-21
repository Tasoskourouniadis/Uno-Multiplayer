package com.example.multiplayertest;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class EndGameActivity extends AppCompatActivity {

    Button doneButton;
    ImageView imageView;
    String gameResult;
    MediaPlayerService playerService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end_game);

        doneButton = findViewById(R.id.doneButton);
        imageView = findViewById(R.id.imageView);
        playerService = new MediaPlayerService();

        //Getting the result of the game from Intent
        gameResult = getIntent().getStringExtra("gameResult");

        if(gameResult.equals("win"))
        {
            imageView.setImageResource(R.drawable.you_win);
            playerService.initMediaPlayer(getApplicationContext(), 1, R.raw.sfx_win, false);
        }else{
            imageView.setImageResource(R.drawable.you_lose);
            playerService.initMediaPlayer(getApplicationContext(), 1, R.raw.sfx_lose, false);
        }

        //When the game finishes, send the appropriate broadcasts to finish Game and reset DB values
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent("finish_activity");
                sendBroadcast(intent);
                intent = new Intent("game_ended");
                sendBroadcast(intent);
                finish();
            }
        });
    }
}