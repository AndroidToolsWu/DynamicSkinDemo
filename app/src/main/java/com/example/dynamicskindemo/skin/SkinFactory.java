package com.example.dynamicskindemo.skin;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.example.dynamicskindemo.R;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.ViewCompat;

public class SkinFactory implements LayoutInflater.Factory2 {


    private static final String TAG = "SkinFactory";
    //预定义一个委托类，它负责按照系统的原有逻辑来创建view
    private AppCompatDelegate mDelegate;
    //我自定义的List，缓存所有可变肤view
    private List<SkinView> mListCacheSkinView = new ArrayList<>();


    public void setDelegate(AppCompatDelegate delegate) {
        mDelegate = delegate;
    }

    @Nullable
    @Override
    public View onCreateView(@Nullable View parent, @NonNull String name, @NonNull Context context, @NonNull AttributeSet attributeSet) {
        //执行系统代码里的创建View的过程，我们只想加入自己的操作，并不是要全盘接管
        //系统创建出来的有时候会为空
        View view = mDelegate.createView(parent, name, context, attributeSet);
        if (view == null) {
            if (-1 == name.indexOf('.')) {
                view = createViewByPrefix(context, name, prefixs, attributeSet);
            } else {
                view = createViewByPrefix(context, name, null, attributeSet);
            }
        }
        collectSkinView(context, attributeSet, view);
        return view;

    }

    /**
     * Factory2 是继承Factory的，所以，我们只要重写Factory2的onCreateView逻辑，就不必理会Factory的重写方法了
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull String s, @NonNull Context context, @NonNull AttributeSet attributeSet) {
        return null;
    }

    /**
     * 收集需要换肤的控件
     * 收集的方式是：通过自定义属性isSupport，从创建出来的很多View中，找到支持换肤的那些，保存到map中
     */
    private void collectSkinView(Context context, AttributeSet attrs, View view) {
        //获得我们自定义的属性
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.Skinable);
        boolean isSupport = typedArray.getBoolean(R.styleable.Skinable_isSupport, false);
        if (isSupport) { //找到支持换肤的View
            final int len = attrs.getAttributeCount();
            HashMap<String, String> attrMap = new HashMap<>();
            for (int i = 0; i < len; i++) {
                String attrName = attrs.getAttributeName(i);
                String attrValue = attrs.getAttributeValue(i);
                attrMap.put(attrName, attrValue); //将所有可换肤属性存起来
            }

            SkinView skinView = new SkinView();
            skinView.view = view;
            skinView.attrsMap = attrMap;
            mListCacheSkinView.add(skinView);  //将可换肤的view，放到mListCacheSkinView中
            //Log.d(TAG, "collectSkinView: 收集view : " + skinView.view.toString());
        }
        typedArray.recycle();
    }

    /*
        公开给外界换肤的入口
     */
    public void changeSkin() {
        for (SkinView skinView : mListCacheSkinView) {
            skinView.changeSkin();
        }
    }

    static class SkinView {
        View view;
        HashMap<String, String> attrsMap;

        /**
         * 真正的换肤操作
         */
        private void changeSkin() {
            for (Map.Entry<String,String> entry : attrsMap.entrySet()){
                String attrStr = entry.getValue().substring(1);
                switch (entry.getKey()){
                    case "background":
                        Object bgValue = SkinEngine.getInstance().getBackground(Integer.parseInt(attrStr));
                        if (bgValue instanceof Drawable){
                            view.setBackground((Drawable) bgValue);
                        }else if (bgValue instanceof Integer){
                            view.setBackgroundColor((Integer) bgValue);
                        }
                        break;
                    case "textColor":
                        ((TextView) view).setTextColor(Integer.parseInt(attrStr));
                        break;
                    default:
                        break;
                }
            }

        }
    }

    static final Class<?>[] mConstructorSignature = new Class[]{Context.class, AttributeSet.class};
    static final String[] prefixs = new String[]{
            "android.widget.",
            "android.view.",
            "android.webkit."
    };
    private static final HashMap<String, Constructor<? extends View>> sConstructorMap = new HashMap<>();
    final Object[] mConstructorArgs = new Object[2];

    /**
     * 反射创建View
     *
     * @param context
     * @param name
     * @param prefixs
     * @param attrs
     * @return
     */
    private final View createViewByPrefix(Context context, String name, String[] prefixs, AttributeSet attrs) {

        Constructor<? extends View> constructor = sConstructorMap.get(name);
        Class<? extends View> clazz = null;

        if (constructor == null) {
            try {
                if (prefixs != null && prefixs.length > 0) {
                    for (String prefix : prefixs) {
                        clazz = context.getClassLoader().loadClass(
                                prefix != null ? (prefix + name) : name).asSubclass(View.class);
                        if (clazz != null) break;
                    }
                } else {
                    if (clazz == null) {
                        clazz = context.getClassLoader().loadClass(name).asSubclass(View.class);
                    }
                }
                if (clazz == null) {
                    return null;
                }
                constructor = clazz.getConstructor(mConstructorSignature);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            constructor.setAccessible(true);
            sConstructorMap.put(name, constructor);
        }
        Object[] args = mConstructorArgs;
        args[1] = attrs;
        try {
            final View view = constructor.newInstance(args);
            return view;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
