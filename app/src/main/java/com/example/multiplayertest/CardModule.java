package com.example.multiplayertest;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class CardModule extends RelativeLayout {

    private static final String TAG = "CardModule";

    public LayoutInflater inflater;
    public View rootView;

    ImageView cardImageView;
    Integer imageResourceId;

    CardModule initContext;

    OnCardClickedEvent listener;



    //Initializing constructors
    public CardModule(Context context) {
        this(context, null);
    }

    public CardModule(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CardModule(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        rootView = inflater.inflate(R.layout.activity_card_module, null);
        this.addView(rootView);
        initContext=this;





        cardImageView = findViewById(R.id.cardImageView);
        cardImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                //When a card is clicked, animate it and pass Context to listener
                rootView.animate().translationYBy(-50);
                listener.onCardClicked(initContext);

            }
        });


    }



    //Custom Listener
    public void setListener(OnCardClickedEvent listener){
        this.listener = listener;
    }

    //Method for getting image resource
    public Integer getImageResource(){
        return imageResourceId;
    }

    //Method for setting up image resource
    public void setCardImageView(Integer id){
        imageResourceId = id;
        cardImageView.setImageResource(id);
    }


    //Setting margins between cards so they overlap
    public void setMargins (int left, int top, int right, int bottom) {
        if (cardImageView.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) cardImageView.getLayoutParams();
            p.setMargins(left, top, right, bottom);
            cardImageView.requestLayout();
        }
    }

    //Resets the animated card back to the original position
    public void resetAnimation(){
        rootView.animate().translationY(0);
    }

}



