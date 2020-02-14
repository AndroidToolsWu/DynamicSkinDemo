package com.example.dynamicskindemo.skin;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.dynamicskindemo.R;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


public class SkinFactory implements LayoutInflater.Factory2 {

    //参数签名
    static final Class<?>[] mConstructorSignature = new Class[]{Context.class, AttributeSet.class};
    //系统view都在这几个包当中
    static final String[] prefixs = new String[]{
            "android.widget.",
            "android.view.",
            "android.webkit."
    };
    //用于存储view的构造
    private static final HashMap<String, Constructor<? extends View>> sConstructorMap = new HashMap<>();
    private static final String TAG = "SkinFactory";
    //缓存所有可变肤view
    private static List<SkinView> SKINVIEW_CACHE = new ArrayList<>();
    private static List<SkinChangeListener> LISTENER_LIST = new ArrayList<>();

    /**
     * 设置单个view的皮肤
     *
     * @param viewSkin
     */
    public static void applySkin(View viewSkin) {
        for (SkinView skinView : SKINVIEW_CACHE) {
            if (skinView.view == viewSkin) {
                skinView.changeSkin();
                break;
            }
        }
    }

    /**
     * 在recyclerview的adapter.viewholder中设置换肤，解决view复用导致的换肤bug
     *
     * @param view
     */
    public static void applyRecyclerViewSkin(View view) {
        applySkin(view);
        if (view instanceof ViewGroup) {
            ViewGroup parent = (ViewGroup) view;
            for (int i = 0; i < parent.getChildCount(); i++) {
                applyRecyclerViewSkin(parent.getChildAt(i));
            }
        }
    }

    /**
     * 全局换肤的入口
     */
    public static void applyAllSkinViews() {
        for (SkinView skinView : SKINVIEW_CACHE) {
            skinView.changeSkin();
        }
    }

    /**
     * 添加更换皮肤的监听器
     *
     * @param listener
     */
    public static void addSkinChangeListener(SkinChangeListener listener) {
        LISTENER_LIST.add(listener);
    }

    public static void removeSkinChangeListener(SkinChangeListener listener) {
        LISTENER_LIST.remove(listener);
    }

    /**
     * 通知所有监听器执行更换皮肤操作
     */
    public static void notifySkinListeners() {
        for (SkinChangeListener listener : LISTENER_LIST) {
            listener.onSkinChange();
        }
    }

    /**
     * 通过此方法代替系统创建view方法
     */
    @Nullable
    @Override
    public View onCreateView(@Nullable View parent, @NonNull String name, @NonNull Context context, @NonNull AttributeSet attributeSet) {
        View view = createViewFromTag(name, context, attributeSet);
        if (view == null) {
            createView(name, context, attributeSet);
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
     * 收集的方式是：通过自定义属性support_skin，从创建出来的很多View中，找到支持换肤的那些，保存到map中
     */
    private void collectSkinView(Context context, AttributeSet attrs, View view) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.Skinable);
        boolean isSupport = typedArray.getBoolean(R.styleable.Skinable_support_skin, false);
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
            SKINVIEW_CACHE.add(skinView);       //将可换肤的view，放到CACHE_SKINVIEW中
        }
        typedArray.recycle();
    }

    /**
     * 如果不是自定义view，调用此方法
     */
    private View createViewFromTag(String name, Context context, AttributeSet attrs) {
        View view = null;
        if (-1 != name.indexOf('.')) {
            return null;
        }
        for (String prefix : prefixs) {
            view = createView(prefix + name, context, attrs);
            if (view != null) {
                break;
            }
        }
        return view;
    }

    /**
     * 反射创建View,如果是自定义view，可以直接通过此方法创建
     */
    private View createView(String name, Context context, AttributeSet attrs) {
        Constructor<? extends View> constructor = sConstructorMap.get(name);
        if (constructor == null) {
            try {
                Class<? extends View> aClass = context.getClassLoader().loadClass(name).asSubclass(View.class);
                constructor = aClass.getConstructor(mConstructorSignature);
                constructor.setAccessible(true);
                sConstructorMap.put(name, constructor);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (null != constructor) {
            try {
                return constructor.newInstance(context, attrs);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}
