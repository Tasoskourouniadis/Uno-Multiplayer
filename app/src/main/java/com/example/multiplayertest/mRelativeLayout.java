package com.example.multiplayertest;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

public class mRelativeLayout extends RelativeLayout {
    private boolean mWithholdTouchEventsFromChildren;

    public mRelativeLayout(Context context) {
        super(context);
    }
    public mRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public mRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);}


    //Custom Relative layout that prevents touch on children when needed
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mWithholdTouchEventsFromChildren || super.onInterceptTouchEvent(ev);
    }

    public void setWithholdTouchEventsFromChildren(boolean withholdTouchEventsFromChildren) {
        mWithholdTouchEventsFromChildren = withholdTouchEventsFromChildren;
    }

}
