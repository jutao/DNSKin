package com.example.msi.dnskin;

import android.app.Application;

import com.example.skin.core.SkinManager;

/**
 * Created by MSI on 2018/3/25.
 */

public class MyApplication extends Application{

    @Override
    public void onCreate() {
        super.onCreate();
        SkinManager.init(this);
    }
}
