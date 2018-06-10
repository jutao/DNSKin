package com.example.skin.core;

import android.app.Activity;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.skin.core.utils.SkinResources;
import com.example.skin.core.utils.SkinThemeUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by MSI on 2018/3/25.
 */

public class SkinAttribute {
    private static final List<String> mAttributes = new ArrayList<>();

    static {
        mAttributes.add("background");
        mAttributes.add("src");
        mAttributes.add("textColor");
        mAttributes.add("drawableLeft");
        mAttributes.add("drawableBottom");
        mAttributes.add("drawableRight");
        mAttributes.add("drawableTop");
    }


    List<SkinView> skinViews = new ArrayList<>();

    public SkinAttribute() {
    }

    public void load(View view, AttributeSet attrs) {
        List<SkinPair> skinPairs = new ArrayList<>();
        for (int i = 0; i < attrs.getAttributeCount(); i++) {
            String attributeName = attrs.getAttributeName(i);
            if (mAttributes.contains(attributeName)) {
                String attributeValue = attrs.getAttributeValue(i);
                //写死不进行换肤
                if (attributeValue.startsWith("#")) {
                    continue;
                }
                int resId;
                if (attributeValue.startsWith("？")) {
                    int attrId = Integer.parseInt(attributeValue.substring(1));
                    //获得主题 style 中的 对应 attr 的资源 id 的值
                    resId = SkinThemeUtils.getResId(view.getContext(), new int[]{attrId})[0];
                } else {
                    resId = Integer.parseInt(attributeValue.substring(1));
                }
                if (resId != 0) {
                    SkinPair skinPair = new SkinPair(attributeName, resId);
                    skinPairs.add(skinPair);
                }
            }
        }
        //将 view 与之对应的属性放入一个集合中
        if (!skinPairs.isEmpty()||view instanceof TextView||view instanceof SkinViewSupport) {
            SkinView skinView = new SkinView(view, skinPairs);
            //换新加载的页面的皮肤
            skinView.applySkin();
            skinViews.add(skinView);
        }

    }

    /**
     * 换皮肤
     */
    public void applySkin() {
        for (SkinView skinView : skinViews) {
            //此处是换当前页面的皮肤
            skinView.applySkin();
        }
    }

    class SkinView {
        View view;
        List<SkinPair> skinPairs;

        public SkinView(View view, List<SkinPair> skinPairs) {
            this.view = view;
            this.skinPairs = skinPairs;
        }

        public void applySkin() {
            applySkinTypeface(SkinThemeUtils.getSkinTypeface((Activity) view.getContext()));
            applySkinViewSupport();
            for (SkinPair skinPair : skinPairs) {
                Drawable left = null, top = null, right = null, bottom = null;
                switch (skinPair.attributeName) {
                    case "background":
                        Object background = SkinResources.getInstance().getBackground(skinPair.resId);
                        //Color
                        if (background instanceof Integer) {
                            view.setBackgroundColor((Integer) background);
                        } else {
                            ViewCompat.setBackground(view, (Drawable) background);
                        }
                        break;
                    case "src":
                        Object src = SkinResources.getInstance().getBackground(skinPair.resId);
                        if (src instanceof Integer) {
                            ((ImageView)view).setImageDrawable(new ColorDrawable((Integer) src));
                        } else {
                            ((ImageView)view).setImageDrawable((Drawable) src);
                        }
                        break;
                    case "textColor":
                        ((TextView) view).setTextColor(SkinResources.getInstance().getColorStateList(skinPair.resId));
                        break;
                    case "drawableLeft":
                        left = SkinResources.getInstance().getDrawable(skinPair.resId);
                        break;
                    case "drawableBottom":
                        bottom = SkinResources.getInstance().getDrawable(skinPair.resId);
                        break;
                    case "drawableRight":
                        right = SkinResources.getInstance().getDrawable(skinPair.resId);
                        break;
                    case "drawableTop":
                        top = SkinResources.getInstance().getDrawable(skinPair.resId);
                        break;
                    default:
                        break;
                }
                if (null != left || null != right || null != top || null != bottom) {
                    ((TextView) view).setCompoundDrawablesWithIntrinsicBounds(left, top, right,
                            bottom);
                }
            }
            SkinThemeUtils.updateStatusBar((Activity) view.getContext());
        }

        private void applySkinViewSupport() {
            if(view instanceof SkinViewSupport){
                SkinViewSupport skinViewSupport= (SkinViewSupport) view;
                skinViewSupport.applySkin();
            }
        }

        private void applySkinTypeface(Typeface typeface) {
            if(view instanceof TextView){
                ((TextView)view).setTypeface(typeface);
            }
        }
    }

    class SkinPair {
        String attributeName;
        int resId;

        public SkinPair(String attributeName, int resId) {
            this.attributeName = attributeName;
            this.resId = resId;
        }
    }
}
