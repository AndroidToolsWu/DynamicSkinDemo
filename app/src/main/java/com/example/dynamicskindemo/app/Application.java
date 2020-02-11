package com.example.dynamicskindemo.app;

import android.content.Context;

import com.example.dynamicskindemo.skin.SkinEngine;

public class Application extends android.app.Application {

    private static Context mContext;


    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        //初始化换肤引擎
        SkinEngine.getInstance().init(this);


    }

    public static Context getContext(){
        return mContext;
    }
}
