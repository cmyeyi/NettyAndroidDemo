package com.gzk.netty.a;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.gzk.netty.R;
import com.gzk.netty.utils.Constant;
import com.gzk.netty.utils.IPUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public final static String TAG = MainActivity.class.getSimpleName();
    public static final String IP = "192.168.31.10";
    private TextView addressView;
    private TextView progressView;
    private SocketManager socketManager;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.tv_send).setOnClickListener(this);
        findViewById(R.id.tv_connect).setOnClickListener(this);
        progressView = findViewById(R.id.ip_progress);
        addressView = findViewById(R.id.ip_address);
        addressView.setText("ip=" + getSelfIpAddress());
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                        SimpleDateFormat format = new SimpleDateFormat("hh:mm:ss");
                        addressView.append("\n[" + format.format(new Date()) + "]" + msg.obj.toString());
                        break;
                    case 1:
                        addressView.setText("本机IP：" + getSelfIpAddress() + " 监听端口:" + msg.obj.toString());
                        break;
                    case 2:
                        Toast.makeText(getApplicationContext(), msg.obj.toString(), Toast.LENGTH_SHORT).show();
                        break;
                    case 3:
                        progressView.setText("进度：" + msg.obj.toString());
                        break;
                }
            }
        };
        socketManager = new SocketManager(handler);
    }

    private String getSelfIpAddress() {
        IPUtils iPInfo = new IPUtils(this);
        return iPInfo.getWIFILocalIpAddress();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_connect:
                connect();
                break;
            case R.id.tv_send:
                send();
                break;
            default:
                break;
        }
    }

    private void connect() {

    }

    private static final int FILE_CODE = 0;

    private void send() {
        if (Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
            final File file = new File("/sdcard/gd.zip");
            if (file.exists()) {
                Log.d("#####", "name:" + file.getName());
                Message.obtain(handler, 0, "正在发送至" + IP + ":" + Constant.PORT).sendToTarget();
                Thread sendThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        socketManager.sendFile(file.getName(), file.getPath(), IP, Constant.PORT);
                    }
                });
                sendThread.start();
            } else {
                Log.d("#####", "file no exists:");
            }
        }

    }
}
