package com.example.dynamicskindemo.skin;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import java.io.File;
import java.lang.reflect.Method;

import androidx.core.content.ContextCompat;

@SuppressLint("StaticFieldLeak")
public class SkinEngine {

    private final static SkinEngine instance = new SkinEngine();
    private Resources mResources;
    private Context mContext;
    private String mPackageName;


    private SkinEngine() {
    }

    public static SkinEngine getInstance() {
        return instance;
    }

    public void init(Context context) {
        mContext = context.getApplicationContext();
    }

    /**
     * 加载外部资源包
     * @param path 外部传入的apk文件名
     */
    public void load(final String path) {
        File file = new File(path);
        if (!file.exists()) {
            return;
        }
        PackageManager packageManager = mContext.getPackageManager();
        PackageInfo info = packageManager.getPackageArchiveInfo(path, PackageManager.GET_ACTIVITIES);
        if (info != null) mPackageName = info.packageName;
        AssetManager assetManager;
        try {
            assetManager = AssetManager.class.newInstance();
            Method addAssetPath = assetManager.getClass().getMethod("addAssetPath", String.class);
            addAssetPath.setAccessible(true);
            addAssetPath.invoke(assetManager, path);
            mResources = new Resources(assetManager
                    , mContext.getResources().getDisplayMetrics()
                    , mContext.getResources().getConfiguration());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 提供外部资源包里的颜色
     * @param resId
     * @return
     */
    public int getColor(int resId) {
        if (mResources == null) {
            return resId;
        }
        int outResId = getIdentifier(resId);
        if (outResId == 0) {
            return resId;
        }
        return mResources.getColor(outResId);
    }

    /**
     * 提供外部资源包的图片
     *
     * @param resId
     * @return
     */
    public Drawable getDrawable(int resId) {
        if (mResources == null) {
            return ContextCompat.getDrawable(mContext, resId);
        }
        int outResId = getIdentifier(resId);
        if (outResId == 0) {
            return ContextCompat.getDrawable(mContext, resId);
        }
        return mResources.getDrawable(outResId);
    }

    public Object getBackground(int resId){
        String typeName = mResources.getResourceTypeName(resId);
        if (TextUtils.equals(typeName,"drawable")){
            return getDrawable(resId);
        }else if (TextUtils.equals(typeName,"color")){
            return getColor(resId);
        }
        return null;
    }

    private int getIdentifier(int resId) {
        String resName = mResources.getResourceEntryName(resId);  //名称，例如ic_launcher
        String resType = mResources.getResourceTypeName(resId);   //类型，例如drawable
        int skinId = mResources.getIdentifier(resName, resType, mPackageName);
        return skinId;
    }


}
