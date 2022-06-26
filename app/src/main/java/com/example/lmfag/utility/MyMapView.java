package com.example.lmfag.utility;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewParent;
import android.widget.ScrollView;

import org.osmdroid.views.MapView;

public class MyMapView extends MapView {

    public MyMapView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyMapView(Context context) {
        super(context);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        /**
         * Request all parents to relinquish the touch events
         */
        getParent().requestDisallowInterceptTouchEvent(true);
        return super.dispatchTouchEvent(ev);
    }

    private ScrollView getParentScrollView(ViewParent view) {
        if (view instanceof ScrollView)
            return (ScrollView) view;

        return getParentScrollView(view.getParent());
    }
}