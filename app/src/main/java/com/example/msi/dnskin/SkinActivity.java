package com.example.msi.dnskin;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.example.skin.core.SkinManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * @author Lance
 * @date 2018/3/12
 */

public class SkinActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_skin);
    }

    public void change(View view) {

        //换肤
        SkinManager.getInstance().loadSkin("/sdcard/app-skin-debug.skin");
//        try {
//            byte[] buffer = new byte[1024];
//            InputStream is = getAssets().open("app_skin-debug.apk");
//            FileOutputStream fos = new FileOutputStream(new File("/sdcard/app-skin-debug.skin"));
//            int byteCount=0;
//            while((byteCount=is.read(buffer))!=-1) {//循环从输入流读取 buffer字节
//                fos.write(buffer, 0, byteCount);//将读取的输入流写入到输出流
//            }
//            fos.flush();//刷新缓冲区
//            is.close();
//            fos.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    public void restore(View view) {
        SkinManager.getInstance().loadSkin(null);
    }
}
