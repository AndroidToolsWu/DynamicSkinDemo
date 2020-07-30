package com.example.dynamicskindemo.skin;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.provider.Settings;
import android.text.TextUtils;

import com.example.dynamicskindemo.app.Application;

import java.io.File;
import java.lang.reflect.Method;

import androidx.core.content.ContextCompat;

@SuppressLint("StaticFieldLeak")
public class SkinEngine {

    private static SkinEngine instance;
    private static Application mAppApplication;
    private static Context mContext;
    private Resources mResources;
    private String mPackageName;


    private SkinEngine() {
        //绑定全局监听换肤
        mAppApplication.registerActivityLifecycleCallbacks(new SkinActivityLifecycleCallback());
    }

    public static SkinEngine getInstance() {
        if (instance == null) throw new NullPointerException("SkinEngine instance is null");
        return instance;
    }

    public static void init(Application application) {
        mAppApplication = application;
        mContext = application.getApplicationContext();
        if (null == instance) {
            instance = new SkinEngine();
        }
    }

    /**
     * 加载外部资源包
     *
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
     *
     * @param resId
     * @return
     */
    int getColor(int resId) {
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
     * 提供外部资源包里的字体
     *
     * @param resId
     * @return
     */
    @SuppressLint("NewApi")
    Typeface getTypeFace(int resId) {
        if (mResources == null) {
            return null;
        }
        int outResId = getIdentifier(resId);
        if (outResId == 0) {
            return null;
        }
        return mResources.getFont(outResId);
    }

    /**
     * 提供外部资源包的图片
     *
     * @param resId
     * @return
     */
    Drawable getDrawable(int resId) {
        if (mResources == null) {
            return ContextCompat.getDrawable(mContext, resId);
        }
        int outResId = getIdentifier(resId);
        if (outResId == 0) {
            return ContextCompat.getDrawable(mContext, resId);
        }
        return mResources.getDrawable(outResId);
    }

    Object getBackground(int resId) {
        if (mResources == null) {
            return null;
        }
        String typeName = mResources.getResourceTypeName(resId);
        if (TextUtils.equals(typeName, "drawable")) {
            return getDrawable(resId);
        } else if (TextUtils.equals(typeName, "color")) {
            return getColor(resId);
        }
        return null;
    }

    Object getSrc(int resId) {
        return getBackground(resId);
    }

    private int getIdentifier(int resId) {
        //例如resId为ic_launcher图片id，那么要通过这种方式获得皮肤包里的资源
        String resName = mResources.getResourceEntryName(resId);  //名称，例如ic_launcher
        String resType = mResources.getResourceTypeName(resId);   //类型，例如drawable
        //生成后R.drawable.ic_launcher
        return mResources.getIdentifier(resName, resType, mPackageName);
    }


}
