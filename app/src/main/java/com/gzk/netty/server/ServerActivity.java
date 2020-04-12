package com.gzk.netty.server;

import android.graphics.Bitmap;
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
import com.gzk.netty.qrcode.utils.ZXingUtil;
import com.gzk.netty.utils.Constant;
import com.gzk.netty.utils.IPUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ServerActivity extends AppCompatActivity implements View.OnClickListener {
    public final static String TAG = ServerActivity.class.getSimpleName();
    private TextView addressView;
    private TextView progressView;
    private SocketManagerForServer socketManagerForServer;
    private Handler handler;
    private ImageView qrCodeView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);

        qrCodeView = findViewById(R.id.id_qrcode_view);
        progressView = findViewById(R.id.ip_progress);
        addressView = findViewById(R.id.ip_address);
        addressView.setText("ip=" + getSelfIpAddress());
        handler = new Handler() {
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
                    case 3:
                        progressView.setText("进度：" + msg.obj.toString());
                        break;
                    case 4:
                        String remoteIP = msg.obj.toString();
                        send(remoteIP);
                        break;
                    case 10:
                        finish();
                        break;
                }
            }
        };
        socketManagerForServer = new SocketManagerForServer(handler);
        createQRCode();
    }

    private void createQRCode() {
        Bitmap bitmap = ZXingUtil.createQRCode(getSelfIpAddress(), 150, 150);
        qrCodeView.setImageBitmap(bitmap);
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

    private Thread sendThread;
    private void send(final String remoteIp) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            final File file = new File("/sdcard/gd.zip");
            if (file.exists()) {
                Log.d("#####", "name:" + file.getName());
                Message.obtain(handler, 0, "正在发送至" + remoteIp + ":" + Constant.PORT).sendToTarget();
                sendThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        socketManagerForServer.sendFile(file.getName(), file.getPath(), remoteIp, Constant.PORT);
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
        sendThread.interrupt();
        sendThread= null;
    }
}
