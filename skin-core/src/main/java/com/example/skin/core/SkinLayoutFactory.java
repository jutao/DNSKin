package com.example.skin.core;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by MSI on 2018/3/25.
 */

public class SkinLayoutFactory implements LayoutInflater.Factory2, Observer {

    /**
     * 属性处理类
     */
    private final SkinAttribute skinAttribute;

    protected SkinLayoutFactory(){
        skinAttribute = new SkinAttribute();
    }


    private static final String[] mClassPrefixList={
      "android.widget.",
      "android.view.",
      "android.webkit.",
    };

    private static final Class<?>[] mConstructorsSignature=new Class[]{Context.class,AttributeSet.class};

    private static final HashMap<String, Constructor<? extends View>> sConstructorMap =
            new HashMap<>();

    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        View view = createViewFromTag(name, context, attrs);
        skinAttribute.load(view,attrs);
        return view;
    }

    private View createViewFromTag(String name, Context context, AttributeSet attrs) {
        //自定义控件才包含 '.'
        if (-1 != name.indexOf('.')) {
            return createView(name,context,attrs);
        }
        View view = null;
        for (int i = 0; i < mClassPrefixList.length; i++) {
            //拼全Android包的地址
             view = createView(mClassPrefixList[i] + name, context, attrs);
            if(view!=null){
                break;
            }
        }
        return view;
    }

    private View createView(String name, Context context, AttributeSet attrs) {
        Constructor<? extends View> constructor = sConstructorMap.get(name);
        if(null == constructor){
            try {
                Class<? extends View> aClass = context.getClassLoader().loadClass(name).asSubclass(View.class);
                constructor = aClass.getConstructor(mConstructorsSignature);
                sConstructorMap.put(name, constructor);
            } catch (Exception e) {
            }
        }
        if(null!=constructor){
            try {
                return constructor.newInstance(context,attrs);
            } catch (Exception e) {
            }
        }
        return null;
    }

    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        return null;
    }

    @Override
    public void update(Observable o, Object arg) {
        skinAttribute.applySkin();
    }
}
