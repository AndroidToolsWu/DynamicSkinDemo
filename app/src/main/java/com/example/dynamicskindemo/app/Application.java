package com.example.dynamicskindemo.app;

import android.content.Context;

import com.example.dynamicskindemo.skin.SkinEngine;

public class Application extends android.app.Application {

    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        //初始化换肤引擎
        SkinEngine.init(this);
        mContext = getApplicationContext();
    }

    public static Context getContext(){
        return mContext;
    }
}
