package com.gzk.netty.client;

import android.os.Bundle;
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

import java.text.SimpleDateFormat;
import java.util.Date;

public class ClientActivity extends AppCompatActivity implements View.OnClickListener {
    public final static String TAG = ClientActivity.class.getSimpleName();
//    public static final String IP = "192.168.31.251";
    private TextView addressView;
    private TextView progressView;
    private SocketManagerForClient socketManagerForClient;
    private Handler handler;
    private String remoteIP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getRemoteIp();
        setContentView(R.layout.activity_client);
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
                        addressView.append("\n本机IP：" + getSelfIpAddress() + " 监听端口:" + msg.obj.toString());
                        break;
                    case 2:
                        Toast.makeText(getApplicationContext(), msg.obj.toString(), Toast.LENGTH_SHORT).show();
                        break;
                    case 3:
                        addressView.setText("进度：" + msg.obj.toString());
                        break;
                    case 10:
                        finish();
                        break;
                }
            }
        };
        socketManagerForClient = new SocketManagerForClient(handler);
    }

    private void getRemoteIp() {
        remoteIP = getIntent().getStringExtra("address");
        Log.d("#####", "接收端，扫码获取connectAddress：" + remoteIP);
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
            default:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        connect();
    }

    private Thread serverConnect;
    private void connect() {
        serverConnect = new Thread(new Runnable() {
            @Override
            public void run() {
                socketManagerForClient.connectServer(remoteIP, Constant.PORT);
            }
        });
        serverConnect.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        serverConnect.interrupt();
        serverConnect = null;
    }
}
