package com.gzk.netty.server;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gzk.netty.R;
import com.gzk.netty.callback.OnTransferListener;
import com.gzk.netty.utils.ZXingUtil;
import com.gzk.netty.utils.Constant;
import com.gzk.netty.utils.IPUtils;
import com.gzk.netty.view.NumberProgressBar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ServerActivity extends AppCompatActivity implements View.OnClickListener {
    public final static String TAG = ServerActivity.class.getSimpleName();
    private String filePath;
    private TextView addressView;
    private SocketManagerForServer socketManagerForServer;
    private ImageView qrCodeView;
    private NumberProgressBar progress;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            SimpleDateFormat format;
            switch (msg.what) {
                case 0:
                    format = new SimpleDateFormat("hh:mm:ss");
                    addressView.append("\n[" + format.format(new Date()) + "]" + msg.obj.toString());
                    break;
                case 1:
                    addressView.setText("本机IP：" + getSelfIpAddress() + " 监听端口:" + msg.obj.toString());
                    break;
                case 2:
                    Toast.makeText(getApplicationContext(), msg.obj.toString(), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    OnTransferListener onTransferListener = new OnTransferListener() {
        @Override
        public void onConnectSuccess(String ip) {
            String remoteIP = ip;
            send(remoteIP, filePath);
        }

        @Override
        public void onProgressChanged(int progressValue) {
            refreshProcess(progressValue);
        }

        @Override
        public void onTransferFinished() {
            if (handler != null){
                Message.obtain(handler, 0, "发送完毕").sendToTarget();
            }
            finish();
        }

        @Override
        public void onError() {
            Toast.makeText(ServerActivity.this, "数据接收失败", Toast.LENGTH_SHORT).show();
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);
        getFilePath();
        initProgress();
        qrCodeView = findViewById(R.id.id_qrcode_view);
        addressView = findViewById(R.id.ip_address);
        addressView.setText("ip=" + getSelfIpAddress());
        createQRCode();
        socketManagerForServer = new SocketManagerForServer(onTransferListener);
    }

    private void getFilePath() {
        Intent intent = getIntent();

        if (Intent.ACTION_SEND == intent.getAction()) {
            Bundle bundle = intent.getExtras();
            Uri uri = (Uri) bundle.get(Intent.EXTRA_STREAM);
            filePath = uri.getPath();
            Log.e("#######", "ACTION_SEND，" + uri.getPath() + "  " + intent.getAction());
        } else if (Intent.ACTION_VIEW == intent.getAction()) {
            Uri uri = intent.getData();
            filePath = uri.getPath();
            Log.e("#######", "ACTION_VIEW，" + uri.getPath() + "  " + intent.getAction());
        } else {
            filePath = intent.getStringExtra("path");
            Log.e("#######", "else filepath：" + filePath);
        }
    }

    private void initProgress() {
        int color = Color.BLUE;
        progress = findViewById(R.id.progress);
        progress.setProgressTextColor(color);
        progress.setReachedBarColor(color);
        progress.setUnreachedBarColor(Color.GRAY);
        progress.setProgressTextSize(40f);
        progress.setMax(100);
    }

    public void refreshProcess(final int progressValue) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (progress.getVisibility() != View.VISIBLE) {
                    progress.setVisibility(View.VISIBLE);
                }
                progress.setProgress(progressValue);
            }
        });
    }

    private void createQRCode() {
        initPort();
        JSONObject value=new JSONObject();
        try {
            value.put(Constant.KEY_IP,getSelfIpAddress());
            value.put(Constant.KEY_PORT, Constant.PORT);
            value.put(Constant.KEY_USER,"user123");
            String data = value.toString();
            Log.d("######","###data:"+data);
            Bitmap bitmap = ZXingUtil.createQRCode(data, 150, 150);
            qrCodeView.setImageBitmap(bitmap);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String getSelfIpAddress() {
        IPUtils iPInfo = new IPUtils(this);
        return iPInfo.getWIFILocalIpAddress();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            default:
                break;
        }
    }

    private void send(final String remoteIp, String filePath) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            final File file = new File(filePath);
            if (file.exists()) {
                Log.d("#####", "name:" + file.getName());
                Message.obtain(handler, 0, "正在发送至" + remoteIp + ":" + Constant.PORT).sendToTarget();
                Thread sendThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        socketManagerForServer.sendFile(file, remoteIp, Constant.PORT);
                    }
                });
                sendThread.start();
            } else {
                Log.d("#####", "file no exists:");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private int initPort() {
        Constant.PORT = (int)(Math.random()*9000+1000);
        return Constant.PORT;
    }

}
