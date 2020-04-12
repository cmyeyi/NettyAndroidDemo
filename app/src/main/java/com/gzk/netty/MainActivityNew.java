package com.gzk.netty;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.gzk.netty.client.ClientActivity;
import com.gzk.netty.server.ServerActivity;
import com.gzk.netty.utils.Constant;
import com.gzk.netty.utils.IPUtils;

public class MainActivityNew extends AppCompatActivity implements View.OnClickListener {
    public final static String TAG = MainActivityNew.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_new);
        findViewById(R.id.tv_old).setOnClickListener(this);
        findViewById(R.id.tv_new).setOnClickListener(this);
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
        startActivity(new Intent(this, ServerActivity.class));
    }


    private void toNew() {
        startActivity(new Intent(this, ClientActivity.class));
    }



}
