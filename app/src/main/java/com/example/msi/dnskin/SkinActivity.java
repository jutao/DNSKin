package com.example.msi.dnskin;

import android.Manifest;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.example.msi.dnskin.permission.RxPermission;
import com.example.skin.core.SkinManager;

import io.reactivex.functions.Consumer;

/**
 * @author Lance
 */

public class SkinActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_skin);
    }

    @SuppressLint("CheckResult")
    public void change(final View view) {
        final RxPermission rxPermissions = new RxPermission(this);
        rxPermissions.setLogging(true);
        rxPermissions.request(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                     .subscribe(

                             new Consumer<Boolean>() {
                                 @Override
                                 public void accept(Boolean granted) throws Exception {
                                     if (granted) {
                                         //换肤
                                         SkinManager.getInstance().loadSkin("/sdcard/app-skin-debug.skin");
                                     } else {
                                         Log.e("permission","权限被拒绝");
                                     }
                                 }
                             });

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
