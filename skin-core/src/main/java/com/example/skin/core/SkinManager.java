package com.example.skin.core;

import android.app.Activity;
import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import com.example.skin.core.utils.SkinPreference;
import com.example.skin.core.utils.SkinResources;

import java.lang.reflect.Method;
import java.util.Observable;

/**
 * Created by MSI on 2018/3/25.
 */

public class SkinManager extends Observable{
    private static SkinManager instance;
    private final Application application;
    private final SkinActivityLifecycle skinActivityLifecycle;

    public static SkinManager getInstance() {
        return instance;
    }

    private SkinManager(Application application) {
        this.application =application;
        SkinPreference.init(application);
        SkinResources.init(application);
        skinActivityLifecycle = new SkinActivityLifecycle();
        application.registerActivityLifecycleCallbacks(skinActivityLifecycle);
        loadSkin(SkinPreference.getInstance().getSkin());
    }

    /**
     * 加载皮肤并更新
     *
     * @param path 皮肤包路径
     */
    public void loadSkin(String path) {
        //还原默认皮肤包
        if (TextUtils.isEmpty(path)) {
            SkinPreference.getInstance().setSkin("");
            SkinResources.getInstance().reset();
        } else {
            try {
                AssetManager assetManager = AssetManager.class.newInstance();
                //添加资源进入资源管理器
                Method addAssetPath = assetManager.getClass().getMethod("addAssetPath", String.class);
                addAssetPath.setAccessible(true);
                addAssetPath.invoke(assetManager, path);

                Resources resources = application.getApplicationContext().getResources();
                Resources skinResources = new Resources(assetManager, resources.getDisplayMetrics(), resources.getConfiguration());
                PackageManager packageManager=application.getPackageManager();
                PackageInfo packageArchiveInfo = packageManager.getPackageArchiveInfo(path, PackageManager.GET_ACTIVITIES);
                SkinResources.getInstance().applySkin(skinResources,packageArchiveInfo.packageName);
                //保存当前使用的皮肤包
                SkinPreference.getInstance().setSkin(path);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //应用皮肤包
        setChanged();
        //通知观察者
        notifyObservers();
    }



    public static void init(Application application) {
        synchronized (SkinManager.class) {
            if (null == instance) {
                instance = new SkinManager(application);
            }
        }
    }
    public void updateSkin(Activity activity){
        skinActivityLifecycle.updateSkin(activity);
    }
}
