package com.gzk.netty.server;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.gzk.netty.callback.OnTransferListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import static com.gzk.netty.utils.Constant.KEY_FILE_LENGTH;
import static com.gzk.netty.utils.Constant.KEY_FILE_SEND;
import static com.gzk.netty.utils.Constant.PORT;

public class SocketManagerForServer {
    private ServerSocket server;
    private Socket socket;
    private Thread receiveFileThread;
    private boolean stop = false;
    private OnTransferListener onTransferListener;
    private long length;
    private String fileName;

    public SocketManagerForServer(OnTransferListener callback) {
        this.onTransferListener = callback;
        stop = false;
        try {
            server = new ServerSocket();
            server.setReuseAddress(true);
            server.bind(new InetSocketAddress(PORT));
            receiveFileThread = new Thread(new Runnable(){
                @Override
                public void run() {
                    while(!stop){
                        waitForClientRequest();
                    }
                }
            });
            receiveFileThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    void waitForClientRequest(){
        try{

            Socket socket = server.accept();
            InputStream is = socket.getInputStream();

            onConnectSuccess(socket);
            is.close();
            socket.close();
        }catch(Exception e){
            onTransferError(e);
        }
    }

    public String serverInfoBack(InputStream is) throws Exception {
        byte[] bufIs = new byte[1024];
        int lenIn = is.read(bufIs);
        String info = new String(bufIs, 0, lenIn);
        return info;
    }

    public boolean copyFile(InputStream inputStream, OutputStream out) {
        byte buf[] = new byte[1024];
        int len;
        long sendLength = 0;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                out.write(buf, 0, len);
                sendLength += len;
                onTransferProgress((int) (sendLength * 100 / length));
            }
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public void sendFile(File file, String ipAddress, int port){
        try {
            socket = new Socket(ipAddress, port);
            OutputStream os = socket.getOutputStream();
            InputStream is = socket.getInputStream();
            fileName = file.getName();
            os.write(fileName.getBytes());
            String serverInfo = serverInfoBack(is);
            if (serverInfo.equals(KEY_FILE_LENGTH)) {
                length = file.length();
                os.write(("" + length).getBytes());
            }

            String serverInfo2 = serverInfoBack(is);
            if (serverInfo2.equals(KEY_FILE_SEND)) {
                FileInputStream inputStream = new FileInputStream(file);
                copyFile(inputStream, os);
                inputStream.close();
            }

            is.close();
            os.close();
            socket.close();

            onTransferFinished();
            stop = true;
        } catch (Exception e) {
            onTransferError(e);
        }
    }

    private void onConnectSuccess(Socket socket) {
        Log.d("######","#server connect success#");
        if(onTransferListener != null) {
            onTransferListener.onConnectSuccess(socket.getInetAddress().getHostAddress());
        }
    }

    private void onTransferProgress(int progress) {
        Log.i("#####", "文件接收进度: " + progress);
        if (onTransferListener != null) {
            onTransferListener.onProgressChanged(progress);
        }
    }

    private void onTransferFinished() {
        Log.i("#####", "onTransferFinished");
        if (onTransferListener != null) {
            onTransferListener.onTransferFinished();
        }
    }

    private void onTransferError(Exception e) {
        Log.e("########", "onTransferError" + e.getMessage());
        if (onTransferListener != null) {
            onTransferListener.onError();
        }
    }
}