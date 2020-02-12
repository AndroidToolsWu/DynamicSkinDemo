package com.example.dynamicskindemo.skin;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

class SkinView {
    View view;
    HashMap<String, String> attrsMap;

    /**
     * 真正的换肤操作
     */
    void changeSkin() {
        for (Map.Entry<String, String> entry : attrsMap.entrySet()) {
            //属性开头可能是？、@、#
            String attrStr = filterValue(entry.getValue());
            switch (entry.getKey()) {
                case "background":
                    Object bgValue = SkinEngine.getInstance().getBackground(Integer.parseInt(attrStr));
                    if (bgValue instanceof Drawable) {
                        view.setBackground((Drawable) bgValue);
                    } else if (bgValue instanceof Integer) {
                        view.setBackgroundColor((Integer) bgValue);
                    }
                    break;
                case "src":
                    Object srcValue = SkinEngine.getInstance().getSrc(Integer.parseInt(attrStr));
                    if (view instanceof ImageView) {
                        if (srcValue instanceof Drawable) {
                            ((ImageView) view).setImageDrawable((Drawable) srcValue);
                        } else if (srcValue instanceof Integer) {
                            ((ImageView) view).setColorFilter((Integer) srcValue);
                        }
                    }
                    break;
                case "textColor":
                    ((TextView) view).setTextColor(SkinEngine.getInstance().getColor(Integer.parseInt(attrStr)));
                    break;
                case "fontFamily":
                    ((TextView) view).setTypeface(SkinEngine.getInstance().getTypeFace(Integer.parseInt(attrStr)));
                    break;
                default:
                    break;
            }
        }

    }

    //过滤资源类型
    private String filterValue(String value) {
        if (value.startsWith("#")) {
            return value;
        } else if (value.startsWith("@") || value.startsWith("?")) {
            return value.substring(1);
        } else {
            return value;
        }
    }


}
