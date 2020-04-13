package com.gzk.netty.client;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.gzk.netty.R;
import com.gzk.netty.callback.OnTransferListener;
import com.gzk.netty.utils.Constant;
import com.gzk.netty.utils.IPUtils;
import com.gzk.netty.view.NumberProgressBar;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.gzk.netty.utils.Constant.PORT;

public class ClientActivity extends AppCompatActivity {
    public final static String TAG = ClientActivity.class.getSimpleName();
    private TextView addressView;
    private SocketManagerForClient socketManagerForClient;
    private String remoteIP;
    private int port;
    private String userName;
    private NumberProgressBar progress;
    private Handler handler = new Handler() {
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
            }
        }
    };

    OnTransferListener onTransferListener = new OnTransferListener() {

        @Override
        public void onConnectSuccess(String ip) {

        }

        @Override
        public void onProgressChanged(int progressValue) {
            refreshProcess(progressValue);
        }

        @Override
        public void onTransferFinished() {
            finish();
        }

        @Override
        public void onError() {
            Toast.makeText(ClientActivity.this, "数据接收失败", Toast.LENGTH_SHORT).show();
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getExtra();
        setContentView(R.layout.activity_client);
        initProgress();
        addressView = findViewById(R.id.ip_address);
        addressView.setText("ip=" + getSelfIpAddress());
        socketManagerForClient = new SocketManagerForClient(onTransferListener);
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

    private void getExtra() {
        remoteIP = getIntent().getStringExtra(Constant.KEY_IP);
        Constant.PORT = getIntent().getIntExtra(Constant.KEY_PORT, PORT);
        userName = getIntent().getStringExtra(Constant.KEY_USER);
        Log.d("#####", "接收端，扫码结果，remoteIP：" + remoteIP);
        Log.d("#####", "接收端，扫码结果，port：" + Constant.PORT);
        Log.d("#####", "接收端，扫码结果，userName：" + userName);
    }

    private String getSelfIpAddress() {
        IPUtils iPInfo = new IPUtils(this);
        return iPInfo.getWIFILocalIpAddress();
    }

    @Override
    protected void onResume() {
        super.onResume();
        connect();
    }

    private void connect() {
        Thread serverConnect = new Thread(new Runnable() {
            @Override
            public void run() {
                socketManagerForClient.connectServer(remoteIP, PORT);
            }
        });
        serverConnect.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
