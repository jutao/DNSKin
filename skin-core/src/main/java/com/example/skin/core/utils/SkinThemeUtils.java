package com.example.skin.core.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.os.Build;

import com.example.skin.core.R;

/**
 * Created by MSI on 2018/3/27.
 */

public class SkinThemeUtils {

    private static int[] TYPEFACE_ATTR = {
            R.attr.skinTypeface
    };
    private static int[] APPCOMPAT_COLOR_PRIMARY_DARK_ATTRS = {
            android.support.v7.appcompat.R.attr.colorPrimaryDark
    };
    private static int[] STATUSBAR_COLOR_ATTRS = {android.R.attr.statusBarColor, android.R.attr
            .navigationBarColor};

    public static int[] getResId(Context context,int[] attrs){
        int[] resIds=new int[attrs.length];
        //通过上下文拿到属性组
        TypedArray typedArray = context.obtainStyledAttributes(attrs);
        for (int i = 0; i < typedArray.length(); i++) {
            resIds[i] = typedArray.getResourceId(i, 0);
        }
        typedArray.recycle();
        return resIds;
    }

    public static void updateStatusBar(Activity activity) {
        if (Build.VERSION.SDK_INT <Build.VERSION_CODES.LOLLIPOP) {
            return;
        }
        int[] resIds=getResId(activity,STATUSBAR_COLOR_ATTRS);
        //如果没有配置属性则获得0
        if(resIds[0]==0){
            int statusBarColorId=getResId(activity,APPCOMPAT_COLOR_PRIMARY_DARK_ATTRS)[0];
            if(statusBarColorId!=0){
                activity.getWindow().setStatusBarColor(SkinResources.getInstance().getColor(statusBarColorId));
            }
        }else {
            activity.getWindow().setStatusBarColor(SkinResources.getInstance().getColor(resIds[0]));
        }
        if(resIds[1]!=0){
            activity.getWindow().setNavigationBarColor(SkinResources.getInstance().getColor(resIds[1]));
        }
    }

    /**
     * 获取字体
     * @param activity
     */
    public static Typeface getSkinTypeface(Activity activity) {
        int skinTypeId = getResId(activity, TYPEFACE_ATTR)[0];
        return SkinResources.getInstance().getTypeface(skinTypeId);
    }
}
