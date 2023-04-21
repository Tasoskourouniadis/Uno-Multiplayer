package com.example.multiplayertest;

import static android.content.ContentValues.TAG;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class CardDetails {

    public ArrayList<ArrayList> createArray() {
        ArrayList<ArrayList> cardDetails = new ArrayList();
        ArrayList cardList = new ArrayList();
        ArrayList cardNumber = new ArrayList();
        ArrayList cardColor = new ArrayList();


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

        cardList.add(R.drawable.red_0);
        cardList.add(R.drawable.blue_0);
        cardList.add(R.drawable.yellow_0);
        cardList.add(R.drawable.green_0); //59

        for (int i = 0; i < 4; i++) {
            cardNumber.add("1");
            cardNumber.add("2");
            cardNumber.add("3");
            cardNumber.add("4");
            cardNumber.add("5");
            cardNumber.add("6");
            cardNumber.add("7");
            cardNumber.add("8");
            cardNumber.add("9");
        }
        for (int i = 0; i < 4; i++) {
            cardNumber.add("skip");
            cardNumber.add("reverse");
            cardNumber.add("draw2");
        }
        for (int i = 0; i < 4; i++) {
            cardNumber.add("changeColor");
        }
        for (int i = 0; i < 4; i++) {
            cardNumber.add("draw4");
        }
        for (int i = 0; i < 4; i++) {
            cardNumber.add("0");
        }


        for (int i = 0; i < 9; i++) {
            cardColor.add("red");
        }
        for (int i = 0; i < 9; i++) {
            cardColor.add("blue");
        }
        for (int i = 0; i < 9; i++) {
            cardColor.add("yellow");
        }
        for (int i = 0; i < 9; i++) {
            cardColor.add("green");
        }
        for (int i = 0; i < 3; i++) {
            cardColor.add("red");
        }
        for (int i = 0; i < 3; i++) {
            cardColor.add("blue");
        }
        for (int i = 0; i < 3; i++) {
            cardColor.add("yellow");
        }
        for (int i = 0; i < 3; i++) {
            cardColor.add("green");
        }
        for (int i = 0; i < 4; i++) {
            cardColor.add("change");
        }
        for (int i = 0; i < 4; i++) {
            cardColor.add("draw");
        }
        cardColor.add("red");
        cardColor.add("blue");
        cardColor.add("yellow");
        cardColor.add("green");


        cardDetails.add(cardList);
        cardDetails.add(cardNumber);
        cardDetails.add(cardColor);

        return cardDetails;
    }



    }

