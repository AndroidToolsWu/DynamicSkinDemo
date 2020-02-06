package com.example.dynamicskindemo.skin;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;

public class SkinFactory implements LayoutInflater.Factory2 {


    private static final String TAG = "SkinFactory";
    private AppCompatDelegate mDelegate;

    //缓存所有可变肤view
    private static List<SkinView> CACHE_SKINVIEW = new ArrayList<>();
    private static List<SkinChangeListener> LISTENER_LIST = new ArrayList<>();


    public void setDelegate(AppCompatDelegate delegate) {
        mDelegate = delegate;
    }


    @Nullable
    @Override   //拦截系统创建view的过程
    public View onCreateView(@Nullable View parent, @NonNull String name, @NonNull Context context, @NonNull AttributeSet attributeSet) {
        View view = mDelegate.createView(parent, name, context, attributeSet);
        if (view == null) {
            if (-1 == name.indexOf('.')) {
                view = createViewByPrefix(context, name, prefixs, attributeSet);
            } else {
                view = createViewByPrefix(context, name, null, attributeSet);
            }
        }
        //收集可换肤view
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
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.Skinable);
        boolean isSupport = typedArray.getBoolean(R.styleable.Skinable_isSupport, false);
        if (isSupport) {        //找到支持换肤的View
            final int len = attrs.getAttributeCount();
            HashMap<String, String> attrMap = new HashMap<>();
            for (int i = 0; i < len; i++) {
                String attrName = attrs.getAttributeName(i);
                String attrValue = attrs.getAttributeValue(i);
                attrMap.put(attrName, attrValue);       //将所有可换肤属性存起来
            }
            SkinView skinView = new SkinView();
            skinView.view = view;
            skinView.attrsMap = attrMap;
            CACHE_SKINVIEW.add(skinView);       //将可换肤的view，放到CACHE_SKINVIEW中
            Log.d(TAG, "collectSkinView: 收集view : " + skinView.view.toString());
        }
        typedArray.recycle();
    }

    //添加更换皮肤的监听器
    public static void addSkinChangeListener(SkinChangeListener listener){
        LISTENER_LIST.add(listener);
    }

    public static void removeSkinChangeListener(SkinChangeListener listener){
        LISTENER_LIST.remove(listener);
    }

    //通知所有监听器执行更换皮肤操作
    private static void notifySkinListeners(){
        for (SkinChangeListener listener : LISTENER_LIST){
            listener.onSkinChange();
        }
    }


    /**
     * 公开给外界换肤的入口
     */
    public static void changeSkin() {
        for (SkinView skinView : CACHE_SKINVIEW) {
            skinView.changeSkin();
        }
        notifySkinListeners();
    }

    static class SkinView {
        View view;
        HashMap<String, String> attrsMap;

        /**
         * 真正的换肤操作
         */
        private void changeSkin() {
            for (Map.Entry<String,String> entry : attrsMap.entrySet()){
                //属性开头可能是？、@、#
                String attrStr = filterValue(entry.getValue());
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
                        ((TextView) view).setTextColor(SkinEngine.getInstance().getColor(Integer.parseInt(attrStr)));
                        break;
                    default:
                        break;
                }
            }

        }

        //过滤资源类型
        private String filterValue(String value){
            if (value.startsWith("#")){
                return value;
            }else if (value.startsWith("@") || value.startsWith("?")){
                return value.substring(1);
            }else {
                return value;
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
