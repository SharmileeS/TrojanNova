package com.nova.trojan.trojannova.utils;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class CustomPreference extends Preference {


    public CustomPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        View view = super.onCreateView(parent);

        LinearLayout imageView = (LinearLayout) ((LinearLayout) view).getChildAt(0);
        ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
        layoutParams.resolveLayoutDirection(Gravity.RIGHT);
        imageView.setLayoutParams(layoutParams);
        return view;
    }
}