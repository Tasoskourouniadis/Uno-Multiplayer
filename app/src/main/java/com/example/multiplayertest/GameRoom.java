package com.example.multiplayertest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Random;

public class GameRoom extends AppCompatActivity {


    private static final String TAG = "Game Room";
    ArrayList playerList;
    ArrayList playerTurnList;

    ImageView cardPileImageView;
    ImageView cardDeckImageView;
    ImageView unoImageButton;
    ImageView restrictedActionImageView;
    ImageView redColourImageView;
    ImageView blueColourImageView;
    ImageView yellowColourImageView;
    ImageView greenColourImageView;
    ImageView rotationIndicatorImageView;

    ImageView cardEventImageView;
    ImageView playerEventImageView;
    ImageView opponentEventImageView1;
    ImageView opponentEventImageView2;
    ImageView opponentEventImageView3;
    mRelativeLayout cardLinearLayout;
    CardModule cardModule;

    ConstraintLayout changeColourConstrainLayout;
    ConstraintLayout parentConstraintLayout;

    ArrayList cardList;
    ArrayList shuffledCardList;
    ArrayList<ArrayList> cardDetails;
    Deque cardStack;

    String playerName = "";
    String roomName = "";
    String role = "";
    Integer namePos;

    WaitingRoomModule playerPlayerModule;
    WaitingRoomModule oponentPlayerModule1;
    WaitingRoomModule oponentPlayerModule2;
    WaitingRoomModule oponentPlayerModule3;

    FirebaseDatabase database;
    DatabaseReference playerRef;
    DatabaseReference cardRef;
    DatabaseReference turnRef;
    DatabaseReference newColourRef;
    DatabaseReference rotationRef;
    DatabaseReference cardNoRef;
    DatabaseReference passTurnRef;
    DatabaseReference unoRef;
    DatabaseReference isCardDrawRef;

    SharedPreferences preferences;

    Integer margin = 0;

    View previousClicked;
    View nextClicked;

    Integer currentCardResourceId = 0;
    String currentCardNumber = "";
    String currentCardColor = "";

    Integer selectedCardResourceId = 0;
    String selectedCardNumber = "";
    String selectedCardColor = "";

    Integer childToBeRemovedIndex;

    String turnName;

    CardDetails cardDetailsClass;

    Boolean isGameReversed = false;
    Boolean isUnoClicked = false;
    Boolean previousPlayerPassed = false;
    Boolean isCardDraw = false;
    Boolean playerHasUno1 = false, playerHasUno2 = false, playerHasUno3 = false, playerHasUno4 = false;

    MediaPlayerService playerService;
    boolean isScreenOn;
    PowerManager pm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_room);


        playerService = new MediaPlayerService();
        parentConstraintLayout = findViewById(R.id.parentConstraintLayout);

        playerPlayerModule = findViewById(R.id.playerPlayerModule);
        oponentPlayerModule1 = findViewById(R.id.guestPlayerModule1);
        oponentPlayerModule2 = findViewById(R.id.guestPlayerModule2);
        oponentPlayerModule3 = findViewById(R.id.guestPlayerModule3);
        cardPileImageView = findViewById(R.id.cardPileImageView);
        cardDeckImageView = findViewById(R.id.cardDeckImageView);
        unoImageButton = findViewById(R.id.unoImageButton);
        restrictedActionImageView = findViewById(R.id.restrictedActionImageView);
        restrictedActionImageView.setVisibility(View.INVISIBLE);

        changeColourConstrainLayout = findViewById(R.id.changeColourConstrainLayout);
        redColourImageView = findViewById(R.id.redColourImageView);
        blueColourImageView = findViewById(R.id.blueColourImageView);
        yellowColourImageView = findViewById(R.id.yellowColourImageView);
        greenColourImageView = findViewById(R.id.greenColourImageView);

        cardEventImageView = findViewById(R.id.cardEventImageView);
        playerEventImageView = findViewById(R.id.playerEventImageView);
        opponentEventImageView1 = findViewById(R.id.opponentEventImageView1);
        opponentEventImageView2 = findViewById(R.id.opponentEventImageView2);
        opponentEventImageView3 = findViewById(R.id.opponentEventImageView3);
        cardEventImageView.setVisibility(View.INVISIBLE);
        playerEventImageView.setVisibility(View.INVISIBLE);
        opponentEventImageView1.setVisibility(View.INVISIBLE);
        opponentEventImageView2.setVisibility(View.INVISIBLE);
        opponentEventImageView3.setVisibility(View.INVISIBLE);

        rotationIndicatorImageView = findViewById(R.id.rotationIndicatorImageView);

        cardLinearLayout = findViewById(R.id.cardLinearLayout);

        cardDetailsClass = new CardDetails();

        cardDetails = new ArrayList<ArrayList>();
        playerList = new ArrayList<String>();
        playerTurnList = new ArrayList<String>();

        cardStack = new ArrayDeque<Integer>();

        database = FirebaseDatabase.getInstance();

        preferences = getSharedPreferences("PREFS", 0);
        playerName = preferences.getString("PlayerName", "");
        roomName = getIntent().getStringExtra("roomName");

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            roomName = extras.getString("roomName");
            if (roomName.equals(playerName)) {
                role = "host";
            } else {
                role = "guest";
            }

        }

        cardList = new ArrayList<>();

        registerReceiver(broadcastReceiver, new IntentFilter("finish_activity"));

        // Creation of an ArrayList with every uno Card
        cardList.add(R.drawable.red_1);
        cardList.add(R.drawable.red_2);
        cardList.add(R.drawable.red_3);
        cardList.add(R.drawable.red_4);
        cardList.add(R.drawable.red_5);
        cardList.add(R.drawable.red_6);
        cardList.add(R.drawable.red_7);
        cardList.add(R.drawable.red_8);
        cardList.add(R.drawable.red_9); //8

        cardList.add(R.drawable.blue_1);
        cardList.add(R.drawable.blue_2);
        cardList.add(R.drawable.blue_3);
        cardList.add(R.drawable.blue_4);
        cardList.add(R.drawable.blue_5);
        cardList.add(R.drawable.blue_6);
        cardList.add(R.drawable.blue_7);
        cardList.add(R.drawable.blue_8);
        cardList.add(R.drawable.blue_9); //17

        cardList.add(R.drawable.yellow_1);
        cardList.add(R.drawable.yellow_2);
        cardList.add(R.drawable.yellow_3);
        cardList.add(R.drawable.yellow_4);
        cardList.add(R.drawable.yellow_5);
        cardList.add(R.drawable.yellow_6);
        cardList.add(R.drawable.yellow_7);
        cardList.add(R.drawable.yellow_8);
        cardList.add(R.drawable.yellow_9); //26

        cardList.add(R.drawable.green_1);
        cardList.add(R.drawable.green_2);
        cardList.add(R.drawable.green_3);
        cardList.add(R.drawable.green_4);
        cardList.add(R.drawable.green_5);
        cardList.add(R.drawable.green_6);
        cardList.add(R.drawable.green_7);
        cardList.add(R.drawable.green_8);
        cardList.add(R.drawable.green_9); //35

        cardList.add(R.drawable.red_skip);
        cardList.add(R.drawable.red_reverse);
        cardList.add(R.drawable.red_draw2); //38
        cardList.add(R.drawable.blue_skip);
        cardList.add(R.drawable.blue_reverse);
        cardList.add(R.drawable.blue_draw2); //41
        cardList.add(R.drawable.yellow_skip);
        cardList.add(R.drawable.yellow_reverse);
        cardList.add(R.drawable.yellow_draw2); //44
        cardList.add(R.drawable.green_skip);
        cardList.add(R.drawable.green_reverse);
        cardList.add(R.drawable.green_draw2); //47

        cardList.add(R.drawable.change_colour1);
        cardList.add(R.drawable.change_colour2);
        cardList.add(R.drawable.change_colour3);
        cardList.add(R.drawable.change_colour4); //51
        cardList.add(R.drawable.draw_four1);
        cardList.add(R.drawable.draw_four2);
        cardList.add(R.drawable.draw_four3);
        cardList.add(R.drawable.draw_four4); //55

        cardList.add(R.drawable.green_0);
        cardList.add(R.drawable.yellow_0);
        cardList.add(R.drawable.blue_0);
        cardList.add(R.drawable.red_0); //59

        // Copy the ArrayList to another and add the rest of the cards so that the total is 108 cards
        shuffledCardList = new ArrayList(cardList);
        for (int i = 0; i < 47; i++) {
            shuffledCardList.add(cardList.get(i));
        }

        // Shuffle the cards
        Collections.shuffle(shuffledCardList);


        // If the player has 2 cards is allowed to press Uno
        unoImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animateView(unoImageButton, 50);
                if (cardLinearLayout.getChildCount() == 2) {
                    unoImageButton.setAlpha(1f);
                    isUnoClicked = true;
                    unoRef = database.getReference("Rooms/" + roomName + "/UnoClicked/");
                    unoRef.setValue("true");
                }
            }
        });

        // When a player Draws a card, checks the drawn card if its eligible for play
        cardDeckImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                giveCard(1);
                if (checkDrawnCard()) {
                    simpleDialog();
                } else {
                    nextTurn();
                    passTurnRef = database.getReference("Rooms/" + roomName + "/PassTurn/");
                    passTurnRef.setValue("1");
                    previousPlayerPassed = true;
                }
            }
        });

        //Change colour buttons when Change colour card is played
        redColourImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animateView(redColourImageView, 90);
                changeColourConstrainLayout.setVisibility(View.INVISIBLE);
                // Posts the New Colour and the Current Card Res. ID to DB
                newColourRef = database.getReference("Rooms/" + roomName + "/NewColour/");
                newColourRef.setValue("red");
                cardRef = database.getReference("Rooms/" + roomName + "/CurrentCard/");
                cardRef.setValue(currentCardResourceId);
                nextTurn();

            }
        });

        blueColourImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animateView(blueColourImageView, 90);
                changeColourConstrainLayout.setVisibility(View.INVISIBLE);
                newColourRef = database.getReference("Rooms/" + roomName + "/NewColour/");
                newColourRef.setValue("blue");
                cardRef = database.getReference("Rooms/" + roomName + "/CurrentCard/");
                cardRef.setValue(currentCardResourceId);
                nextTurn();

            }
        });

        yellowColourImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animateView(yellowColourImageView, 90);
                changeColourConstrainLayout.setVisibility(View.INVISIBLE);
                newColourRef = database.getReference("Rooms/" + roomName + "/NewColour/");
                newColourRef.setValue("yellow");
                cardRef = database.getReference("Rooms/" + roomName + "/CurrentCard/");
                cardRef.setValue(currentCardResourceId);
                nextTurn();

            }
        });

        greenColourImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animateView(greenColourImageView, 90);
                changeColourConstrainLayout.setVisibility(View.INVISIBLE);
                newColourRef = database.getReference("Rooms/" + roomName + "/NewColour/");
                newColourRef.setValue("green");
                cardRef = database.getReference("Rooms/" + roomName + "/CurrentCard/");
                cardRef.setValue(currentCardResourceId);
                nextTurn();

            }
        });

        addPlayersEventListener();


        if (role.equals("host")) {
        //Set a random Card from 0-9 as first playing card
            Random random = new Random();
            int randomNumber = random.nextInt(1 + 35);
            cardPileImageView.setImageResource((Integer) cardList.get(randomNumber));
        // Initialize basic Variables in DB when the game starts
            cardRef = database.getReference("Rooms/" + roomName + "/CurrentCard/");
            cardRef.setValue(cardList.get(randomNumber));
            rotationRef = database.getReference("Rooms/" + roomName + "/GameRotation/");
            rotationRef.setValue("Clockwise");
            newColourRef = database.getReference("Rooms/" + roomName + "/NewColour/");
            newColourRef.setValue("null");
            passTurnRef = database.getReference("Rooms/" + roomName + "/PassTurn/");
            passTurnRef.setValue("null");
            unoRef = database.getReference("Rooms/" + roomName + "/UnoClicked/");
            unoRef.setValue("false");
            isCardDrawRef = database.getReference("Rooms/" + roomName + "/IsCardDraw/");
            isCardDrawRef.setValue("false");
        }


        cardNoRef = database.getReference("Rooms/" + roomName + "/PlayerCardNo/" + playerName + "/");
        //Created an Array of card details (Res. ID, Colour, Number)
        cardDetails = cardDetailsClass.createArray();
        //Clear any card Modules
        cardLinearLayout.removeAllViews();

        //Listeners
        giveCard(7);
        turnOfPlayersListener();
        isUnoClickedListener();
        reverseCardListener();
        cardNumberOfPlayersListener();
        turnPassedListener();
        topOfThePileListener();
        isCardDrawListener();
    }

    //Check if current card is draw 2 or draw 4. Avoids improper utilisation of Draw cards when the card is on top of pile for more than 1 turn
    public void isCardDrawListener() {
        isCardDrawRef = database.getReference("Rooms/" + roomName + "/IsCardDraw/");
        isCardDrawRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    isCardDraw = snapshot.getValue().equals("true");
                    Log.e(TAG, "onDataChange: " + snapshot);
                    if (turnName != null) {
                        specialCardListener();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //Listens if a player pressed UNO
    public void isUnoClickedListener() {
        unoRef = database.getReference("Rooms/" + roomName + "/UnoClicked/");
        unoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    if (snapshot.getValue().equals("true")) {
                        if(!playerService.isMpPlaying(1)) {
                            playerService.initMediaPlayer(getApplicationContext(), 1, R.raw.sfx_effect_uno, false);
                        }
                        if (playerPlayerModule.getPlayerName().equals(turnName)) {
                            playerEventImageView.setImageResource(R.drawable.uno_event);
                            animateUnoEvent(playerEventImageView, true);
                            playerHasUno1 = true;
                        }
                        if (oponentPlayerModule1.getPlayerName().equals(turnName)) {
                            opponentEventImageView1.setImageResource(R.drawable.uno_event);
                            animateUnoEvent(opponentEventImageView1, true);
                            playerHasUno2 = true;

                        }
                        if (playerList.size() >= 2) {
                            if (oponentPlayerModule2.getPlayerName().equals(turnName)) {
                                opponentEventImageView2.setImageResource(R.drawable.uno_event);
                                animateUnoEvent(opponentEventImageView2, true);
                                playerHasUno3 = true;

                            }
                        }
                        if (playerList.size() >= 3) {
                            if (oponentPlayerModule3.getPlayerName().equals(turnName)) {
                                opponentEventImageView3.setImageResource(R.drawable.uno_event);
                                animateUnoEvent(opponentEventImageView3, true);
                                playerHasUno4 = true;

                            }
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //Listens if the player has passed his turn
    public void turnPassedListener() {
        passTurnRef = database.getReference("Rooms/" + roomName + "/PassTurn/");
        passTurnRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    previousPlayerPassed = snapshot.getValue().equals("1");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    //Gets the current number of cards each player has
    public void cardNumberOfPlayersListener() {
        cardNoRef = database.getReference("Rooms/" + roomName + "/PlayerCardNo/");
        cardNoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> data = dataSnapshot.getChildren();
                for (DataSnapshot snapshot : data) {

                    if (snapshot.getKey().equals(playerTurnList.get(0))) {
                        playerPlayerModule.setNumberOfCards(snapshot.getValue().toString());
                        if (Integer.parseInt((String) snapshot.getValue()) >= 2 && playerHasUno1) {
                            animateUnoEvent(playerEventImageView, false);
                            playerHasUno1 = false;
                        }
                    }
                    if (snapshot.getKey().equals(playerTurnList.get(1))) {
                        oponentPlayerModule1.setNumberOfCards(snapshot.getValue().toString());
                        if (Integer.parseInt((String) snapshot.getValue()) >= 2 && playerHasUno2) {
                            animateUnoEvent(opponentEventImageView1, false);
                            playerHasUno2 = false;
                        }
                    }
                    if (playerList.size() >= 3) {
                        if (snapshot.getKey().equals(playerTurnList.get(2))) {
                            oponentPlayerModule2.setNumberOfCards(snapshot.getValue().toString());
                            if (Integer.parseInt((String) snapshot.getValue()) >= 2 && playerHasUno3) {
                                animateUnoEvent(opponentEventImageView2, false);
                                playerHasUno3 = false;
                            }
                        }
                    }
                    if (playerList.size() >= 4) {
                        if (snapshot.getKey().equals(playerTurnList.get(3))) {
                            oponentPlayerModule3.setNumberOfCards(snapshot.getValue().toString());
                            if (Integer.parseInt((String) snapshot.getValue()) >= 2 && playerHasUno4) {
                                animateUnoEvent(opponentEventImageView3, false);
                                playerHasUno4 = false;

                            }
                        }
                    }
                    if (snapshot.getValue().toString().equals("0")) {
                        endOfGame(snapshot.getKey());
                    }
                }
                unoRef = database.getReference("Rooms/" + roomName + "/UnoClicked/");
                unoRef.setValue("false");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public void endOfGame(String key) {
        parentConstraintLayout.setAlpha(0.5f);
        Intent intent = new Intent(this, EndGameActivity.class);
        if (key.equals(playerName)) {
            //You win game sequence
            intent.putExtra("gameResult", "win");
        } else {
            //You lose game sequence
            intent.putExtra("gameResult", "lose");
        }

        startActivity(intent);

    }

    //Blinking animation of playing player
    public void glowEffect(WaitingRoomModule module) {
        AlphaAnimation blinkanimation = new AlphaAnimation(1, 0.5f);
        blinkanimation.setDuration(800);
        blinkanimation.setInterpolator(new LinearInterpolator());
        blinkanimation.setRepeatCount(Animation.INFINITE);
        blinkanimation.setRepeatMode(Animation.REVERSE);
        module.startAnimation(blinkanimation);
        if(turnName.equals(playerName)){
            if(!playerService.isMpPlaying(2)) {
                playerService.initMediaPlayer(getApplicationContext(), 2, R.raw.sfx_play, false);
            }
        }
    }


    //Rotates rotation indicator
    public void rotate(View view, Integer from, Integer to) {
        RotateAnimation rotateAnimation = new RotateAnimation(from, to, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setDuration(5000);
        rotateAnimation.setRepeatCount(Animation.INFINITE);
        view.startAnimation(rotateAnimation);
    }


    //Animates Uno Button
    public void animateView(View view, Integer duration) {
        ScaleAnimation scaleAnimatio = new ScaleAnimation(1.0f, 1.15f, 1.0f, 1.15f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.45f);
        scaleAnimatio.setDuration(duration);
        view.startAnimation(scaleAnimatio);

    }

    //checks if the player can play with the card that got drawn
    public boolean checkDrawnCard() {
        String drawnCardColor = null;
        String drawnCardNumber = null;
        Integer drawnCardResId;
        drawnCardResId = cardModule.getImageResource();
        for (int i = 0; i < cardDetails.get(0).size(); i++) { //Finds drawn card details
            if (drawnCardResId.equals(cardDetails.get(0).get(i))) {
                drawnCardColor = cardDetails.get(2).get(i).toString();
                drawnCardNumber = cardDetails.get(1).get(i).toString();
                break;
            }
        }

        return drawnCardNumber.equals(currentCardNumber) || drawnCardColor.equals(currentCardColor) || drawnCardNumber.equals("changeColor") || drawnCardNumber.equals("draw4");
    }

    //Listens the DB for the game's rotations and changes the Rotation Indicator accordingly
    public void reverseCardListener() {
        rotationRef = database.getReference("Rooms/" + roomName + "/GameRotation/");
        rotationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    isGameReversed = snapshot.getValue().equals("CounterClockwise");
                }
                if (isGameReversed) {
                    rotate(rotationIndicatorImageView, 360, 0);
                } else {
                    rotate(rotationIndicatorImageView, 0, 360);

                }
                rotationIndicatorColorSelector();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    //Listens for any special cards draw 4, 2.
    public void specialCardListener() {
        if (turnName.equals(playerName) && isCardDraw) {
            if (currentCardNumber.equals("draw2") && isCardDraw) {
                giveCard(2);
                nextTurn();
            }
            if (currentCardNumber.equals("draw4")&& isCardDraw) {
                giveCard(4);
                nextTurn();
            }
        }
        if (playerPlayerModule.getPlayerName().equals(turnName)) {
            animationDrawEvent(playerEventImageView);
        }
        if (oponentPlayerModule1.getPlayerName().equals(turnName)) {
            animationDrawEvent(opponentEventImageView1);
        }
        if (playerList.size() >= 3) {
            if (oponentPlayerModule2.getPlayerName().equals(turnName)) {
                animationDrawEvent(opponentEventImageView2);
            }
        }
        if (playerList.size() >= 4) {
            if (oponentPlayerModule3.getPlayerName().equals(turnName)) {
                animationDrawEvent(opponentEventImageView3);
            }
        }

    }

    //Finds who is playing
    public void turnOfPlayersListener() {
        turnRef = database.getReference("Rooms/" + roomName + "/PlayerTurn/");
        turnRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    turnName = snapshot.getValue().toString();
                    if (!turnName.equals(playerName)) {
                        cardLinearLayout.setWithholdTouchEventsFromChildren(true);
                        unoImageButton.setClickable(false);
                        cardDeckImageView.setClickable(false);
                    } else {
                        cardLinearLayout.setWithholdTouchEventsFromChildren(false);
                        unoImageButton.setClickable(true);
                        cardDeckImageView.setClickable(true);

                        selectedCardResourceId=0;
                        selectedCardNumber="";
                        selectedCardColor="";

                    }
                if (playerPlayerModule.getPlayerName().equals(turnName)) {
                    glowEffect(playerPlayerModule);
                } else {
                    playerPlayerModule.clearAnimation();
                }
                if (playerList.size() >= 2) {
                    if (oponentPlayerModule1.getPlayerName().equals(turnName)) {
                        glowEffect(oponentPlayerModule1);

                    } else {
                        oponentPlayerModule1.clearAnimation();
                    }
                }
                if (playerList.size() >= 3) {
                    if (oponentPlayerModule2.getPlayerName().equals(turnName)) {
                        glowEffect(oponentPlayerModule2);

                    } else {
                        oponentPlayerModule2.clearAnimation();
                    }
                }
                if (playerList.size() >= 4) {
                    if (oponentPlayerModule3.getPlayerName().equals(turnName)) {
                        glowEffect(oponentPlayerModule3);
                    } else {
                        oponentPlayerModule3.clearAnimation();
                    }
                }

                }



            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    //Listens to the card on the top of the pile
    public void topOfThePileListener() {
        cardRef = database.getReference("Rooms/" + roomName + "/CurrentCard/");
        cardRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    currentCardResourceId = ((Long) snapshot.getValue()).intValue();
                    cardPileImageView.setImageResource(currentCardResourceId);
                    findCurrentPlayingCardDetails();
                    if (currentCardNumber != null) {
                        if (currentCardNumber.equals("changeColor") || currentCardNumber.equals("draw4")) {
                            changeColourListener();
                        }
                        animateCardEvent(cardEventImageView);
                    }

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //Sets a blinking animation for a player with UNO
    public void animateUnoEvent(ImageView view, Boolean isAnimating) {
        AlphaAnimation blinkanimation = new AlphaAnimation(1, 0.5f);
        blinkanimation.setDuration(800);
        blinkanimation.setInterpolator(new LinearInterpolator());
        blinkanimation.setRepeatCount(Animation.INFINITE);
        blinkanimation.setRepeatMode(Animation.REVERSE);

        if (isAnimating) {
            view.setVisibility(View.VISIBLE);
            view.startAnimation(blinkanimation);
        } else {
            view.setVisibility(View.INVISIBLE);
            view.clearAnimation();
        }
    }

    //Animations for +2 or +4 Events
    public void animationDrawEvent(ImageView view) {
        AnimatorSet animation = new AnimatorSet();
        animation.playTogether(
                ObjectAnimator.ofFloat(view, "rotation", 0, 360),
                ObjectAnimator.ofFloat(view, "scaleX", 1, 1.5f),
                ObjectAnimator.ofFloat(view, "scaleY", 1, 1.5f)
        );
        animation.setDuration(1000);
        ImageView finalView = view;
        animation.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                finalView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                finalView.setVisibility(View.INVISIBLE);

            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });


        if (!previousPlayerPassed) {
            if (currentCardNumber.equals("draw4") && isCardDraw) {
                view.setImageResource(R.drawable.draw_four_36);
                animation.start();

            }
            if (currentCardNumber.equals("draw2") && isCardDraw) {
                view.setImageResource(R.drawable.draw_two_36);
                animation.start();

            }
        }


    }

    //Sets the animations for change colour or skip card Event
    public void animateCardEvent(ImageView view) {

        AnimatorSet animation = new AnimatorSet();
        animation.playTogether(
                ObjectAnimator.ofFloat(view, "rotation", 0, 360),
                ObjectAnimator.ofFloat(view, "scaleX", 1, 1.5f),
                ObjectAnimator.ofFloat(view, "scaleY", 1, 1.5f)
        );
        animation.setDuration(1000);
        ImageView finalView = view;
        animation.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                finalView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                finalView.setVisibility(View.INVISIBLE);

            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

        if (!previousPlayerPassed) {
            if (currentCardNumber.equals("skip")) {
                if(!playerService.isMpPlaying(1)) {
                    playerService.initMediaPlayer(getApplicationContext(), 1, R.raw.sfx_effect_skip, false);
                }
                view.setImageResource(R.drawable.skip_64);
                animation.start();

            }
            if (currentCardNumber.equals("change_color")) {
            }
            if (currentCardNumber.equals("reverse")) {
                if(!playerService.isMpPlaying(1)) {
                    playerService.initMediaPlayer(getApplicationContext(), 1, R.raw.sfx_effect_reverse, false);
                }
                view.setImageResource(R.drawable.reverse_128);
                animation.start();
            }
        }

    }


    //Change rotation indicator's colour when the change colour card or +4 is played
    public void changeColourListener() {
        newColourRef = database.getReference("Rooms/" + roomName + "/NewColour/");
        newColourRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    currentCardColor = snapshot.getValue().toString();
                    rotationIndicatorColorSelector();
                    switch (currentCardColor) {
                        case "red":
                            if(!playerService.isMpPlaying(1)) {
                                playerService.initMediaPlayer(getApplicationContext(), 1, R.raw.sfx_colour_red, false);
                            }
                            break;
                        case "yellow":
                            if(!playerService.isMpPlaying(1)) {
                                playerService.initMediaPlayer(getApplicationContext(), 1, R.raw.sfx_colour_yellow, false);
                            }
                            break;
                        case "blue":
                            if(!playerService.isMpPlaying(1)) {
                             playerService.initMediaPlayer(getApplicationContext(), 1, R.raw.sfx_colour_blue, false);
                            }
                            break;
                        case "green":
                            if(!playerService.isMpPlaying(1)) {
                             playerService.initMediaPlayer(getApplicationContext(), 1, R.raw.sfx_colour_green, false);
                            }
                            break;
                    }
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    //Find details about the current card on top of the Pile and sets the rotation colours
    public void findCurrentPlayingCardDetails() {
        Log.e(TAG, "findCardDetails: " + cardDetails.get(0).size() + currentCardResourceId);
        for (int i = 0; i < cardDetails.get(0).size(); i++) {
            if (currentCardResourceId.equals(cardDetails.get(0).get(i))) {
                currentCardNumber = cardDetails.get(1).get(i).toString();
                currentCardColor = cardDetails.get(2).get(i).toString();
                break;

            }
        }
        rotationIndicatorColorSelector();

    }

    //Changes the colour of the rotation indicator based on the game rotation
    public void rotationIndicatorColorSelector() {
        if (currentCardColor != null) {
            if (isGameReversed) {
                switch (currentCardColor) {
                    case "red":
                        rotationIndicatorImageView.setImageResource(R.drawable.counter_rotation_red);
                        break;
                    case "blue":
                        rotationIndicatorImageView.setImageResource(R.drawable.counter_rotation_blue);
                        break;
                    case "yellow":
                        rotationIndicatorImageView.setImageResource(R.drawable.counter_rotation_yellow);
                        break;
                    case "green":
                        rotationIndicatorImageView.setImageResource(R.drawable.counter_rotation_green);
                        break;
                }
            } else {
                switch (currentCardColor) {
                    case "red":
                        rotationIndicatorImageView.setImageResource(R.drawable.rotation_red);
                        break;
                    case "blue":
                        rotationIndicatorImageView.setImageResource(R.drawable.rotation_blue);
                        break;
                    case "yellow":
                        rotationIndicatorImageView.setImageResource(R.drawable.rotation_yellow);
                        break;
                    case "green":
                        rotationIndicatorImageView.setImageResource(R.drawable.rotation_green);
                        break;
                }

            }

        }
    }

    //Finds details about the selected Card
    public Boolean findSelectedCardDetails() {
        for (int i = 0; i < cardDetails.get(0).size(); i++) {
            if (selectedCardResourceId.equals(cardDetails.get(0).get(i))) {
                selectedCardNumber = cardDetails.get(1).get(i).toString();
                selectedCardColor = cardDetails.get(2).get(i).toString();
                break;
            }
        }
        return selectedCardColor.equals(currentCardColor) || selectedCardNumber.equals(currentCardNumber)
                || selectedCardNumber.equals("draw4") || selectedCardNumber.equals("changeColor");

    }

    //Gives a specified number of cards to the player and posts to DB the number of each players card's
    public void giveCard(Integer numberOfCards) {
        Random random = new Random();
        //giving 7 cards to players at the start of the game
        if (numberOfCards == 7) {
            for (int i = 0; i < 7; i++) {
                int randomNumber = random.nextInt(1 + 106);
                cardModule = new CardModule(this.getApplicationContext());
                cardModule.setCardImageView((Integer) shuffledCardList.get(randomNumber));
                //setting a margin between cards so that they dont overlap
                cardLinearLayout.requestLayout();
                cardModule.setMargins(margin, 0, 0, 0);
                margin = margin + 90;
                //adding the view in layout and setting a tag
                cardLinearLayout.addView(cardModule);
                cardModule.setTag(cardLinearLayout.getChildCount());
                //setting the listener on the modules
                addCardClickedEventListener(cardModule);

            }
        } else if (numberOfCards == 1) {
            if(!playerService.isMpPlaying(2)) {
                playerService.initMediaPlayer(getApplicationContext(), 2, R.raw.sfx_draw, false);
            }
            cardModule = new CardModule(this.getApplicationContext());
            int randomNumber = random.nextInt(1 + 106);
            cardModule.setCardImageView((Integer) shuffledCardList.get(randomNumber));
            //setting a margin between cards so that they dont overlap
            margin = cardLinearLayout.getChildCount() * 90;
            cardModule.setMargins(margin, 0, 0, 0);
            //adding the view in layout and setting a tag
            cardLinearLayout.addView(cardModule);
            cardModule.setTag(cardLinearLayout.getChildCount());
            //setting the listener on the modules
            addCardClickedEventListener(cardModule);
        } else if (numberOfCards == 2) {
            if(!playerService.isMpPlaying(2)) {
                playerService.initMediaPlayer(getApplicationContext(), 2, R.raw.sfx_effect_draw2, false);
            }
            for (int i = 0; i < 2; i++) {
                cardModule = new CardModule(this.getApplicationContext());
                int randomNumber = random.nextInt(1 + 106);
                cardModule.setCardImageView((Integer) shuffledCardList.get(randomNumber));
                //setting a margin between cards so that they dont overlap
                margin = cardLinearLayout.getChildCount() * 90;
                cardModule.setMargins(margin, 0, 0, 0);
                //adding the view in layout and setting a tag
                cardLinearLayout.addView(cardModule);
                cardModule.setTag(cardLinearLayout.getChildCount());
                //setting the listener on the modules
                addCardClickedEventListener(cardModule);

            }
        } else if (numberOfCards == 4) {
            for (int i = 0; i < 4; i++) {
                if(!playerService.isMpPlaying(2)) {
                    playerService.initMediaPlayer(getApplicationContext(), 2, R.raw.sfx_effect_draw4, false);
                }
                cardModule = new CardModule(this.getApplicationContext());
                int randomNumber = random.nextInt(1 + 106);
                cardModule.setCardImageView((Integer) shuffledCardList.get(randomNumber));
                //setting a margin between cards so that they dont overlap
                margin = cardLinearLayout.getChildCount() * 90;
                cardModule.setMargins(margin, 0, 0, 0);
                //adding the view in layout and setting a tag
                cardLinearLayout.addView(cardModule);
                cardModule.setTag(cardLinearLayout.getChildCount());
                //setting the listener on the modules
                addCardClickedEventListener(cardModule);

            }
        }

        margin = 0;
        if (isUnoClicked) {
            isUnoClicked = false;
            unoImageButton.setAlpha(0.5f);
        }
        cardNoRef = database.getReference("Rooms/" + roomName + "/PlayerCardNo/" + playerName + "/");
        cardNoRef.setValue(String.valueOf(cardLinearLayout.getChildCount()));
    }

    //Manages clicks on card modules and their animations
    public void addCardClickedEventListener(CardModule cardModule) {
        final int[] margin = {0};
        cardModule.setListener(new OnCardClickedEvent() {
            @Override
            public void onCardClicked(CardModule cardModule) {
                previousClicked = nextClicked;
                nextClicked = cardModule;

                selectedCardResourceId = cardModule.getImageResource();

                if (previousClicked != null) {
                    if (findSelectedCardDetails()) {
                        //if the card clicked is not clicked again return it to the original position
                        if (nextClicked != previousClicked) {
                            cardModule = (CardModule) previousClicked;
                            cardModule.resetAnimation();
                        } else {

                            //removing the card clicked twice and putting it on the pile
                            currentCardResourceId = cardModule.getImageResource();
                            cardPileImageView.setImageResource(currentCardResourceId);
                            childToBeRemovedIndex = cardLinearLayout.indexOfChild(cardModule);
                            cardLinearLayout.removeViewAt(childToBeRemovedIndex);
                            //Moving the cards back together after a removal
                            if (childToBeRemovedIndex < cardLinearLayout.getChildCount()) {
                                margin[0] = 90 * (childToBeRemovedIndex - 1);
                                for (int i = childToBeRemovedIndex; i < cardLinearLayout.getChildCount(); i++) {
                                    cardModule = (CardModule) cardLinearLayout.getChildAt(i);
                                    margin[0] = margin[0] + 90;
                                    cardModule.setMargins(margin[0], 0, 0, 0);
                                }
                            }

                            //Posting the current top of pile card to the database
                            //Pass the turn to the next player
                            if (!isCardChangeColour()) {
                                cardRef = database.getReference("Rooms/" + roomName + "/CurrentCard/");
                                cardRef.setValue(currentCardResourceId);

                                nextTurn();
                            }

                        }
                    } else {
                        //Cancel animation when another card is selected
                        cardModule.resetAnimation();
                        forbiddenActionAnimation();
                        if (previousClicked != null) {
                            cardModule = (CardModule) previousClicked;
                            cardModule.resetAnimation();
                        }

                    }

                } else if (!findSelectedCardDetails()) {
                    cardModule.resetAnimation();
                    forbiddenActionAnimation();
                }

            }
        });
    }


    //Returns true if the selected card has 'Change Colour' properties
    public Boolean isCardChangeColour() {
        if (findSelectedCardDetails()) {
            if (selectedCardNumber.equals("changeColor") || selectedCardNumber.equals("draw4")) {
                changeColourConstrainLayout.setVisibility(View.VISIBLE);



            }
        }
        return selectedCardNumber.equals("changeColor") || selectedCardNumber.equals("draw4");
    }

    //Gives the turn to the next player
    public void nextTurn() {
        cardNoRef = database.getReference("Rooms/" + roomName + "/PlayerCardNo/" + playerName + "/");
        cardNoRef.setValue(String.valueOf(cardLinearLayout.getChildCount()));
        if(isCardDraw){
            isCardDraw = false;
            isCardDrawRef.setValue("false");
        }
        if (playerList.size() > 2) {  //Next turn when the game is for more than two players
            if (selectedCardNumber != null) { //Check if a player has selected and played a card
                    if (selectedCardNumber.equals("skip")) { //If card is skip
                        turnRef.setValue(playerTurnList.get(2));
                    } else {
                        if (isGameReversed) { //check if gave is reversed (CounterClockwise)
                            if (selectedCardNumber.equals("reverse")) { //check if the played card is 'reverse' and reverse the rotation
                                rotationRef.setValue("Clockwise");
                                turnRef.setValue(playerTurnList.get(1));
                            } else {
                                turnRef.setValue(playerTurnList.get(3));
                            }
                        } else { //if gave is NOT reversed (Clockwise)
                            if (selectedCardNumber.equals("reverse")) {
                                rotationRef.setValue("CounterClockwise");
                                turnRef.setValue(playerTurnList.get(3));
                            } else {
                                turnRef.setValue(playerTurnList.get(1));
                            }

                        }
                    }
                } else {
                    if (isGameReversed) { // If played Drew a card and cannot play then pass turn
                        turnRef.setValue(playerTurnList.get(3));
                    } else {
                        turnRef.setValue(playerTurnList.get(1));
                    }
                }
        } else if (playerList.size() == 2){ //Next turn when the game is for two players
            Log.e(TAG, "nextTurn: " + playerTurnList );
            if (selectedCardNumber != null) {
                if (selectedCardNumber.equals("skip")) {
                    turnRef.setValue(playerTurnList.get(0));
                    selectedCardNumber="";
                } else {
                    if (isGameReversed) {
                        if (selectedCardNumber.equals("reverse")) {
                            rotationRef.setValue("Clockwise");
                            turnRef.setValue(playerTurnList.get(0));
                            selectedCardNumber="";
                        }else{
                            turnRef.setValue(playerTurnList.get(1));
                        }
                    } else {
                        if (selectedCardNumber.equals("reverse")) {
                            rotationRef.setValue("CounterClockwise");
                            turnRef.setValue(playerTurnList.get(0));
                            selectedCardNumber="";
                        }else{
                            turnRef.setValue(playerTurnList.get(1));
                        }

                    }
                }
            } else {
                turnRef.setValue(playerTurnList.get(1));
            }
        }

        //If player has 1 card and didnt pressed UNO give 2 gards
        if(cardLinearLayout.getChildCount()==1 && !isUnoClicked){
            giveCard(2);
        }

        //Post to DB if card is +2 or +4
        if(selectedCardNumber.equals("draw4") || selectedCardNumber.equals("draw2")){
            isCardDrawRef = database.getReference("Rooms/" + roomName + "/IsCardDraw/");
            isCardDrawRef.setValue("true");

        }

        //Check if previous player passed his turn
        if(previousPlayerPassed){
            passTurnRef.setValue("null");
            previousPlayerPassed=false;
        }

    }

    //when player tries to play and its not his turn, the animation lets him know.
    public void forbiddenActionAnimation(){
        restrictedActionImageView.setVisibility(View.VISIBLE);
        ScaleAnimation mAnimation = new ScaleAnimation(1.0f,1.15f,1.0f,1.15f,Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.45f);
        mAnimation.setDuration(250);
        restrictedActionImageView.startAnimation(mAnimation);
        restrictedActionImageView.setVisibility(View.INVISIBLE);
    }




    //Gets the players names from DB and sets them in a clockwise order
    public void addPlayersEventListener(){
        //Gets the list of players in the room and assigns roles and details
        playerRef = database.getReference("Rooms/" + roomName + "/Players/");
        playerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                playerList.clear();

                Iterable<DataSnapshot> players = dataSnapshot.getChildren();
                for (DataSnapshot snapshot : players) {
                    playerList.add(snapshot.getKey());
                }

                //find the position of player's name in array list
                for (int i = 0; i < playerList.size(); i++) {
                    if (playerList.get(i).equals(playerName)) {
                        namePos = i;
                        break;
                    }
                }

                //find who plays after the player
                ListIterator i = playerList.listIterator(namePos);
                if (i.hasNext()) i.next();
                while (i.hasNext()) {
                    playerTurnList.add(i.next());
                }

                //complete the loop adding the rest of the players
                Iterator k = playerList.iterator();
                while (k.hasNext()) {
                    String name = (String) k.next();
                    if (name.equals(playerName)) break;
                    else playerTurnList.add(name);
                }
                playerTurnList.add(0, playerName);


                playerPlayerModule.setVisibility(View.VISIBLE);
                playerPlayerModule.setPlayerName(playerTurnList.get(0).toString());
                playerPlayerModule.setPlayerAvatar(R.drawable.avatar1_64);
                if(playerList.size() >= 2) {
                    oponentPlayerModule1.setVisibility(View.VISIBLE);
                    oponentPlayerModule1.setPlayerName(playerTurnList.get(1).toString());
                    oponentPlayerModule1.setPlayerAvatar(R.drawable.avatar2_64);
                }
                if(playerList.size() >= 3) {
                    oponentPlayerModule2.setVisibility(View.VISIBLE);
                    oponentPlayerModule2.setPlayerName(playerTurnList.get(2).toString());
                    oponentPlayerModule2.setPlayerAvatar(R.drawable.avatar3_64);
                }
                if(playerList.size() >= 4) {
                    oponentPlayerModule3.setVisibility(View.VISIBLE);
                    oponentPlayerModule3.setPlayerName(playerTurnList.get(3).toString());
                    oponentPlayerModule3.setPlayerAvatar(R.drawable.avatar4_64);
                }

                if (role.equals("host")) {
                    Random random = new Random();
                    int randomNumber = random.nextInt(playerList.size());
                    turnRef = database.getReference("Rooms/" + roomName + "/PlayerTurn/");
                    turnRef.setValue(playerTurnList.get(randomNumber).toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //error - nothing
            }
        });
    }

    //Dialog when a player Drew a card and can play
    public void simpleDialog(){
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        parentConstraintLayout.setAlpha(1f);
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        parentConstraintLayout.setAlpha(1f);
                        nextTurn();
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        parentConstraintLayout.setAlpha(0.5f);
        builder.setMessage("Do you want to play this card?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }

    //Finish the activity
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context arg0, Intent intent) {
            String action = intent.getAction();
            if (action.equals("finish_activity")) {
                unregisterReceiver(broadcastReceiver);
                finish();

            }
        }
    };

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
}

