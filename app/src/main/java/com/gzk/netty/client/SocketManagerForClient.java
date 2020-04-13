package com.gzk.netty.client;

import android.util.Log;

import com.gzk.netty.callback.OnTransferListener;
import com.gzk.netty.utils.Constant;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

import static com.gzk.netty.utils.Constant.KEY_FILE_LENGTH;
import static com.gzk.netty.utils.Constant.KEY_FILE_SEND;
import static com.gzk.netty.utils.Constant.PORT;

public class SocketManagerForClient {
    private ServerSocket server;
    private Socket socket;
    private Thread receiveFileThread;
    private boolean stop;
    private OnTransferListener onTransferListener;
    private String fileName;
    private int length;

    public SocketManagerForClient(OnTransferListener t) {
        this.onTransferListener = t;
    }

    private void createServerSocket() {
        stop = false;
        try {
            server = new ServerSocket();
            server.setReuseAddress(true);
            server.bind(new InetSocketAddress(PORT));
            Log.w("########", "client create server:" + PORT);
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
            socket = server.accept();
            InputStream is = socket.getInputStream();

            File file = getFileName(is);
            length = getFileLength(is);

            FileOutputStream os = new FileOutputStream(file);
            copyFile(is, os);

            is.close();
            os.close();
            socket.close();
            stop = true;
            Log.d("########", fileName + "接收完成");
            onTransferFinished();
        } catch (IOException e) {
            onTransferError(e);
        } catch (Exception e) {
            onTransferError(e);
        }
    }

    public boolean copyFile(InputStream inputStream, OutputStream out) {
        byte buf[] = new byte[1024];
        int len;
        long total = 0;
        int progress;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                out.write(buf, 0, len);
                total += len;
                progress = (int) (total * 100 / length);
                onTransferProgress(progress);
            }
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public File getFileName(InputStream is) throws Exception {
        byte[] buf = new byte[1024];
        int len = 0;
        len = is.read(buf); // 获取文件名
        fileName = new String(buf, 0, len);
        File file = new File(Constant.FILE_SAVE_PATH_DIR + File.separator + fileName);
        writeOutInfo(socket, KEY_FILE_LENGTH);
        return file;
    }

    public void writeOutInfo(Socket socket, String infoStr) throws Exception {
        OutputStream sockOut = socket.getOutputStream();
        sockOut.write(infoStr.getBytes());
    }

    public int getFileLength(InputStream is) throws Exception {
        byte[] buf = new byte[1024];
        int len = 0;
        len = is.read(buf); // get file length
        String length = new String(buf, 0, len);
        writeOutInfo(socket, KEY_FILE_SEND);
        return Integer.parseInt(length);
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

    public void connectServer(String ipAddress, int port) {
        Log.e("#########", "connectServer,ipAddress=" + ipAddress + ",port" + port);
        String content = "requestServer";
        try {

            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(ipAddress, port), 5000);

            OutputStream os = socket.getOutputStream();
            os.write(content.getBytes("utf-8"));
            os.close();
            socket.close();

            Log.d("########", "请求服务... ...");
        } catch (SocketTimeoutException ste) {
            ste.getMessage();
            Log.e("########", "连接超时，" + ste.getMessage());
        } catch (Exception e) {
            Log.e("########", "client 发送错误，" + e.getMessage());
        }
        createServerSocket();
    }

}