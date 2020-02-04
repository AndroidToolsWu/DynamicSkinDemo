package com.example.dynamicskindemo.skin;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.example.dynamicskindemo.R;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;

public class SkinFactory implements LayoutInflater.Factory2 {


    //预定义一个委托类，它负责按照系统的原有逻辑来创建view
    private AppCompatDelegate mDelegate;
    //我自定义的List，缓存所有可变肤view
    private List<SkinView> mListCacheSkinView = new ArrayList<>();

    /*
        给外部提供一个set方法
     */
    public void setDelegate(AppCompatDelegate delegate) {
        mDelegate = delegate;
    }

    @Nullable
    @Override
    public View onCreateView(@Nullable View parent, @NonNull String name, @NonNull Context context, @NonNull AttributeSet attributeSet) {
        //关键点1：执行系统代码里的创建View的过程，我们只想加入自己的操作，并不是要全盘接管
        //系统创建出来的有时候会为空
        View view = mDelegate.createView(parent, name, context, attributeSet);
        if (view == null) {
            if (-1 == name.indexOf('.')) { //不包含，说明不带包名，我们帮他添上
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
        }
        // fixme typedArray.recycle();
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
        public void changeSkin() {
            if (!TextUtils.isEmpty(attrsMap.get("background"))) { //属性名，例如 background 、textColor 、text
                int bgId = Integer.parseInt(attrsMap.get("background").substring(1)); //属性类别，比如drawable 、 color
                //这个值，在app的一次运行中，不会发生变化
                String attrType = view.getResources().getResourceTypeName(bgId);
                if (TextUtils.equals(attrType, "drawable")) { //区分drawable和color
                    view.setBackground(SkinEngine.getInstance().getDrawable(bgId));
                } else if (TextUtils.equals(attrType, "color")) {
                    view.setBackgroundColor(SkinEngine.getInstance().getColor(bgId));
                }
            }

            if (view instanceof TextView) {
                if (!TextUtils.isEmpty(attrsMap.get("textColor"))) {
                    int textColorId = Integer.parseInt(attrsMap.get("textColor").substring(1));
                    ((TextView) view).setTextColor(SkinEngine.getInstance().getColor(textColorId));
                }
            }


        }

    }

    // 下面的逻辑摘抄自源码,AppCompatViewInflater类源码里搜索：view = createViewFromTag(context, name, attrs);
    static final Class<?>[] mConstructorSignature = new Class[]{Context.class, AttributeSet.class};
    //安卓里面控件的包名，就这么3种,这个变量是为了下面代码里，反射创建类的class而预备的
    static final String[] prefixs = new String[]{
            "android.widget.",
            "android.view.",
            "android.webkit."
    };
    //用映射，将View的反射构造函数都存起来
    private static final HashMap<String, Constructor<? extends View>> sConstructorMap = new HashMap<>();
    //View的构造函数的2个实参对象
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
                                prefix != null ? (prefix + name) : name).asSubclass(View.class);    //控件
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
                constructor = clazz.getConstructor(mConstructorSignature);  //拿到 构造方法，
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            constructor.setAccessible(true);
            sConstructorMap.put(name, constructor); //然后缓存起来，下次再用，就直接从内存中去取
        }
        Object[] args = mConstructorArgs;
        args[1] = attrs;
        try {
            //通过反射创建View对象
            final View view = constructor.newInstance(args);    //执行构造函数，拿到View对象
            return view;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
