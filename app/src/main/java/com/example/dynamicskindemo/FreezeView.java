package com.example.dynamicskindemo;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class FreezeView extends View {

    public FreezeView(Context context) {
        this(context,null);
    }

    public FreezeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FreezeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}
