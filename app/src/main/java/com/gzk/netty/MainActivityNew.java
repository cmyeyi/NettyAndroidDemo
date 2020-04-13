package com.gzk.netty;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.gzk.netty.file.ChoseFileActivity;
import com.gzk.netty.utils.Constant;

import java.io.File;

public class MainActivityNew extends AppCompatActivity implements View.OnClickListener {
    public final static String TAG = MainActivityNew.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_new);
        findViewById(R.id.tv_old).setOnClickListener(this);
        findViewById(R.id.tv_new).setOnClickListener(this);
        mkDir();
    }

    /**
     * 创建文件夹
     */
    private void mkDir() {
        File dir = new File(Constant.FILE_SAVE_PATH_ROOT);
        if (!dir.exists())
            dir.mkdir();
        File dir2 = new File(Constant.FILE_SAVE_PATH_DIR);
        if (!dir2.exists())
            dir2.mkdir();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_old:
                toOld();
                break;
            case R.id.tv_new:
                toNew();
                break;
        }
    }

    private void toOld() {
        if (checkPermission()) {
            Intent intent = new Intent(this, ChoseFileActivity.class);
            startActivityForResult(intent, 1);
        }
//        startActivity(new Intent(this, ServerActivity.class));
    }


    private void toNew() {
        if(checkPermission()) {
            Intent intent = new Intent(MainActivityNew.this, CaptureActivity.class);
            startActivity(intent);
        }

    }

    public boolean checkPermission() {
        if (!hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                !hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ||
                !hasPermission(Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(MainActivityNew.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 100);
            return false;
        }
        return true;
    }

    public boolean hasPermission(String permission) {
        return ActivityCompat.checkSelfPermission(getBaseContext(), permission) == PackageManager.PERMISSION_GRANTED;
    }


}
