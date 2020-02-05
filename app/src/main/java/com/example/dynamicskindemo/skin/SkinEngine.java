package com.example.dynamicskindemo.skin;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;

import java.io.File;
import java.lang.reflect.Method;

import androidx.core.content.ContextCompat;

public class SkinEngine {

    //饿汉模式
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
        //使用application的目的是，如果万一传进来的是activity对象
        //那么它被静态对象instance所持有，这个Activity就无法释放了
    }

    /*
        加载外部资源包
     */
    public void load(final String path) { //path是外部传入的apk文件名
        File file = new File(path);
        if (!file.exists()) {
            return;
        }
        //取得PackageManager引用
        PackageManager mPackageManager = mContext.getPackageManager();
        //PackageInfo中包含着应用程序的总体信息
        PackageInfo mInfo = mPackageManager.getPackageArchiveInfo(path, PackageManager.GET_ACTIVITIES);
        mPackageName = mInfo.packageName; //将包名储存起来
        AssetManager assetManager; //资源管理器
        //通过反射获取AssetManager用来加载外部apk资源
        try {
            //因为addAssetPath为私有方法，如果使用就需要通过反射的方式
            assetManager = AssetManager.class.newInstance();
            Method addAssetPath = assetManager.getClass().getMethod("addAssetPath", String.class);
            addAssetPath.setAccessible(true);
            addAssetPath.invoke(assetManager, path); //反射执行方法，设置加载路径
            mResources = new Resources(assetManager   //资源管理器
                    , mContext.getResources().getDisplayMetrics() //屏幕参数
                    , mContext.getResources().getConfiguration()); //资源配置
            //最终创建从一个可以加载外部资源文件的Resource对象

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
    public int getColor(int resId) {
        if (mResources == null) {
            return resId;
        }
        String resName = mResources.getResourceEntryName(resId);
        int outResId = mResources.getIdentifier(resName, "color", mPackageName);
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
    public Drawable getDrawable(int resId) { //获取图片
        if (mResources == null) {
            return ContextCompat.getDrawable(mContext, resId);
        }
        String resName = mResources.getResourceEntryName(resId);
        int outResId = mResources.getIdentifier(resName, "drawable", mPackageName);
        if (outResId == 0) {
            return ContextCompat.getDrawable(mContext, resId);
        }
        return mResources.getDrawable(outResId);
    }


}
