package com.gzk.netty.client;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

import static com.gzk.netty.utils.Constant.PORT;

public class SocketManagerForClient {
    private ServerSocket server;
    private Handler handler = null;
    private Thread receiveFileThread;
    private boolean stop;

    public SocketManagerForClient(Handler handler) {
        this.handler = handler;
    }

    private void createServerSocket() {
        stop = false;
        try {
            server = new ServerSocket();
            server.setReuseAddress(true);
            server.bind(new InetSocketAddress(PORT));
            sendMessage(1, PORT);
            receiveFileThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (!stop) {
                        receiveFile();
                    }
                }
            });
            receiveFileThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void receiveFile() {
        try {
            Socket name = server.accept();
            InputStream nameStream = name.getInputStream();
            InputStreamReader streamReader = new InputStreamReader(nameStream);
            BufferedReader br = new BufferedReader(streamReader);
            String fileName = br.readLine();
            br.close();
            streamReader.close();
            nameStream.close();
            name.close();
            sendMessage(0, "正在接收:" + fileName);

            Socket data = server.accept();
            InputStream dataStream = data.getInputStream();
            String savePath = Environment.getExternalStorageDirectory().getPath() + "/" + fileName;
            FileOutputStream file = new FileOutputStream(savePath, false);
            byte[] buffer = new byte[1024];
            int size = -1;
            long total = 0;
            while ((size = dataStream.read(buffer)) != -1) {
                file.write(buffer, 0, size);
                total += size;
                sendMessage(3, "total:" + total);
            }
            file.close();
            dataStream.close();
            data.close();
            stop = true;
            sendMessage(0, fileName + "接收完成");
            sendMessage(10, "finish");
        } catch (Exception e) {
            sendMessage(0, "接收错误:\n" + e.getMessage());
        }
    }


    public void connectServer(String ipAddress, int port) {
        String content = "requestServer";
        try {

            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(ipAddress, port), 1000);

            OutputStream os = socket.getOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(os);
            BufferedWriter bw = new BufferedWriter(osw);
            os.write(content.getBytes("utf-8"));
            bw.close();
            osw.close();
            os.close();
            socket.close();

            sendMessage(0, "请求服务... ...");
        } catch (SocketTimeoutException ste) {
            sendMessage(0, "发送错误:" + ste.getMessage());
        } catch (Exception e) {
            sendMessage(0, "发送错误:" + e.getMessage());
        }
        createServerSocket();
    }

    void sendMessage(int what, Object obj) {
        if (handler != null) {
            Message.obtain(handler, what, obj).sendToTarget();
        }
    }

    private void clean() {

    }
}