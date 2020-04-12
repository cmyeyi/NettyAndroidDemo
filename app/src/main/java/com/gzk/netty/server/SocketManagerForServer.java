package com.gzk.netty.server;

import android.os.Handler;
import android.os.Message;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

import static com.gzk.netty.utils.Constant.PORT;

public class SocketManagerForServer {
    private ServerSocket server;
    private Handler handler = null;
    public SocketManagerForServer(Handler handler){
        this.handler = handler;
        try {
            server = new ServerSocket(PORT);
            sendMessage(1, PORT);
            Thread receiveFileThread = new Thread(new Runnable(){
                @Override
                public void run() {
                    while(true){
                        waitForClientRequest();
                    }
                }
            });
            receiveFileThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    void sendMessage(int what, Object obj){
        if (handler != null){
            Message.obtain(handler, what, obj).sendToTarget();
        }
    }

    void waitForClientRequest(){
        try{

            Socket name = server.accept();
            InputStream nameStream = name.getInputStream();
            InputStreamReader streamReader = new InputStreamReader(nameStream);
            BufferedReader br = new BufferedReader(streamReader);
            String key = br.readLine();
            sendMessage(0, "收到，客户端请求,key:"+key+"\n");
            sendMessage(4, name.getInetAddress().getHostAddress());

            br.close();
            streamReader.close();
            nameStream.close();
            name.close();
        }catch(Exception e){
            sendMessage(0, "接收错误:\n" + e.getMessage());
        }
    }

    public void sendFile(String fileName, String path, String ipAddress, int port){
        try {
            Socket name = new Socket(ipAddress, port);
            OutputStream outputName = name.getOutputStream();
            OutputStreamWriter outputWriter = new OutputStreamWriter(outputName);
            BufferedWriter bwName = new BufferedWriter(outputWriter);
            bwName.write(fileName);
            bwName.close();
            outputWriter.close();
            outputName.close();
            name.close();
            sendMessage(0, "正在发送" + fileName);

            Socket data = new Socket(ipAddress, port);
            OutputStream outputData = data.getOutputStream();
            FileInputStream fileInput = new FileInputStream(path);
            int size = -1;
            byte[] buffer = new byte[1024];
            while((size = fileInput.read(buffer, 0, 1024)) != -1){
                outputData.write(buffer, 0, size);
            }
            outputData.close();
            fileInput.close();
            data.close();
            sendMessage(0, fileName + "  发送完成");
            sendMessage(0, "所有文件发送完成");
        } catch (Exception e) {
            sendMessage(0, "发送错误:\n" + e.getMessage());
        }
    }
}