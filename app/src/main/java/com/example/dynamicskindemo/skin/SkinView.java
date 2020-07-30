package com.example.dynamicskindemo.skin;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

class SkinView {

    public static final String ATTR_BACKGROUND = "background";
    public static final String ATTR_SRC = "src";
    public static final String ATTR_TEXT_COLOR = "textColor";
    public static final String ATTR_FONT_FAMILY = "fontFamily";

    private View mView;
    private final HashMap<String, String> mAttributeMap = new HashMap<>();

    /**
     * 真正的换肤操作
     */
    void changeViewSkin() {
        for (Map.Entry<String, String> entry : mAttributeMap.entrySet()) {
            // 属性开头可能是？、@、#
            String attributeResource = filterValue(entry.getValue());
            switch (entry.getKey()) {
                case ATTR_BACKGROUND:
                    changeBackgroundSkin(attributeResource);
                    break;
                case ATTR_SRC:
                    changeSrcSkin(attributeResource);
                    break;
                case ATTR_TEXT_COLOR:
                    changeTextColorSkin(attributeResource);
                    break;
                case ATTR_FONT_FAMILY:
                    changeFontFamilySkin(attributeResource);
                    break;
            }
        }
    }

    private void changeBackgroundSkin(String attributeResource) {
        Object bgValue = SkinEngine.getInstance().getBackground(Integer.parseInt(attributeResource));
        if (bgValue instanceof Drawable) {
            mView.setBackground((Drawable) bgValue);
        } else if (bgValue instanceof Integer) {
            mView.setBackgroundColor((Integer) bgValue);
        }
    }

    private void changeSrcSkin(String attributeResource) {
        Object srcValue = SkinEngine.getInstance().getSrc(Integer.parseInt(attributeResource));
        if (mView instanceof ImageView) {
            if (srcValue instanceof Drawable) {
                ((ImageView) mView).setImageDrawable((Drawable) srcValue);
            } else if (srcValue instanceof Integer) {
                ((ImageView) mView).setColorFilter((Integer) srcValue);
            }
        }
    }

    private void changeTextColorSkin(String attributeResource) {
        ((TextView) mView).setTextColor(SkinEngine.getInstance().getColor(Integer.parseInt(attributeResource)));
    }

    private void changeFontFamilySkin(String attributeResource) {
        ((TextView) mView).setTypeface(SkinEngine.getInstance().getTypeFace(Integer.parseInt(attributeResource)));
    }

    /**
     * 过滤资源类型
     */
    private String filterValue(String value) {
        if (value.startsWith("#")) {
            return value;
        } else if (value.startsWith("@") || value.startsWith("?")) {
            return value.substring(1);
        } else {
            return value;
        }
    }

    public View getView() {
        return mView;
    }

    public void setView(View view) {
        mView = view;
    }

    public HashMap<String, String> getAttributeMap() {
        return mAttributeMap;
    }

    public void setAttributeMap(HashMap<String, String> attributeMap) {
        mAttributeMap.clear();
        mAttributeMap.putAll(attributeMap);
    }
}
