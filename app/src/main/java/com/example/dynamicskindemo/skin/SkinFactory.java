package com.example.dynamicskindemo.skin;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.dynamicskindemo.R;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class SkinFactory implements LayoutInflater.Factory2 {

    private static SkinFactory mInstance = new SkinFactory();
    //参数签名
    private static final Class<?>[] CONSTRUCTOR_SIGNATURE = new Class[]{Context.class, AttributeSet.class};
    //系统view都在这几个包当中
    private static final String[] PREFIX = new String[]{
            "android.widget.",
            "android.view.",
            "android.webkit.",
            "android.app."
    };
    //用于存储view的构造
    private static final HashMap<String, Constructor<? extends View>> CONSTRUCTOR_MAP = new HashMap<>();
    //缓存所有可变肤view
    private static final List<SkinView> SKIN_VIEW_CACHE = new ArrayList<>();
    private static final Set<SkinChangeListener> LISTENER_LIST = new HashSet<>();

    public static SkinFactory getInstance() {
        return mInstance;
    }

    /**
     * 通过此方法代替系统创建view方法
     */
    @Nullable
    @Override
    public View onCreateView(@Nullable View parent, @NonNull String name, @NonNull Context context, @NonNull AttributeSet attributeSet) {
        View view = createViewFromTag(name, context, attributeSet);
        if (view == null) {
            view = createView(name, context, attributeSet);
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
        //找到支持换肤的View
        if (isSupport) {
            final int len = attrs.getAttributeCount();
            HashMap<String, String> attrMap = new HashMap<>();
            for (int i = 0; i < len; i++) {
                String attrName = attrs.getAttributeName(i);
                String attrValue = attrs.getAttributeValue(i);
                //将所有可换肤属性存起来
                attrMap.put(attrName, attrValue);
            }
            SkinView skinView = new SkinView();
            skinView.setView(view);
            skinView.setAttributeMap(attrMap);
            //将可换肤的view，放到CACHE_SKIN_VIEW中
            SKIN_VIEW_CACHE.add(skinView);
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
        for (String prefix : PREFIX) {
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
        Constructor<? extends View> constructor = CONSTRUCTOR_MAP.get(name);
        if (constructor == null) {
            try {
                Class<? extends View> aClass = context.getClassLoader().loadClass(name).asSubclass(View.class);
                constructor = aClass.getConstructor(CONSTRUCTOR_SIGNATURE);
                constructor.setAccessible(true);
                CONSTRUCTOR_MAP.put(name, constructor);
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

    /**
     * 设置单个view的皮肤
     */
    public void applySkin(View viewSkin) {
        for (SkinView skinView : SKIN_VIEW_CACHE) {
            if (skinView.getView() == viewSkin) {
                skinView.changeViewSkin();
                break;
            }
        }
    }

    /**
     * 在recyclerview的adapter.viewHolder中设置换肤，解决view复用导致的换肤bug
     */
    public void applyRecyclerViewSkin(View view) {
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
    public void applyAllSkinViews() {
        for (SkinView skinView : SKIN_VIEW_CACHE) {
            skinView.changeViewSkin();
        }
    }

    /**
     * 添加更换皮肤的监听器
     */
    public void addSkinChangeListener(SkinChangeListener listener) {
        LISTENER_LIST.add(listener);
    }

    public void removeSkinChangeListener(SkinChangeListener listener) {
        LISTENER_LIST.remove(listener);
    }

    /**
     * 通知所有监听器执行更换皮肤操作
     */
    public void notifySkinListeners() {
        for (SkinChangeListener listener : LISTENER_LIST) {
            listener.onSkinChange();
        }
    }
}
