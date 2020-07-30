package com.example.dynamicskindemo.skin;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;


public class SkinActivityLifecycleCallback implements android.app.Application.ActivityLifecycleCallbacks {

    private String TAG = "SkinActivityLifecycleCallback";

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        loadSkin();
    }

    private void loadSkin() {
        File skinFile = new File(Environment.getExternalStorageDirectory(), "SkinDemo/skin.apk");
        //加载外部资源包
        SkinEngine.getInstance().load(skinFile.getAbsolutePath());
        Log.d(TAG, "changeSkin:" + skinFile.getAbsolutePath());
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        //在view创建完成后进行换肤
        SkinFactory.getInstance().applyAllSkinViews();
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {

    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {

    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {

    }
}
