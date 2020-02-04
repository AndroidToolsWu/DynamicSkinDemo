package com.example.dynamicskindemo.app;

import com.example.dynamicskindemo.skin.SkinEngine;

public class Application extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();
        //初始化换肤引擎
        SkinEngine.getInstance().init(this);
    }
}
