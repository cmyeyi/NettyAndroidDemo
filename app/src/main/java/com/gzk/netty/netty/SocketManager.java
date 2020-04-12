package com.gzk.netty.netty;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import static com.gzk.netty.netty.utils.Constant.PORT;

public class SocketManager {
    private ServerSocket server;
    private Handler handler = null;
    public SocketManager(Handler handler){
        this.handler = handler;
        try {
            server = new ServerSocket(PORT);
            sendMessage(1, PORT);
            Thread receiveFileThread = new Thread(new Runnable(){
                @Override
                public void run() {
                    while(true){
                        receiveFile();
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

    void receiveFile(){
        try{

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
            while ((size = dataStream.read(buffer)) != -1){
                file.write(buffer, 0 ,size);
                total += size;
                sendMessage(3, "total:" + total);
            }
            file.close();
            dataStream.close();
            data.close();
            sendMessage(0, fileName + "接收完成");
        }catch(Exception e){
            sendMessage(0, "接收错误:\n" + e.getMessage());
        }
    }

    public int getFileLength(InputStream is) throws Exception {
        byte[] buf = new byte[1024];
        int len = 0;
        len = is.read(buf); // get file length
        String length = new String(buf, 0, len);
        return Integer.parseInt(length);
    }

    public void sendFile(ArrayList<String> fileName, ArrayList<String> path, String ipAddress, int port){
        try {
            for (int i = 0; i < fileName.size(); i++){
                Socket name = new Socket(ipAddress, port);
                OutputStream outputName = name.getOutputStream();
                OutputStreamWriter outputWriter = new OutputStreamWriter(outputName);
                BufferedWriter bwName = new BufferedWriter(outputWriter);
                bwName.write(fileName.get(i));
                bwName.close();
                outputWriter.close();
                outputName.close();
                name.close();
                sendMessage(0, "正在发送" + fileName.get(i));

                Socket data = new Socket(ipAddress, port);
                OutputStream outputData = data.getOutputStream();
                FileInputStream fileInput = new FileInputStream(path.get(i));
                int size = -1;
                byte[] buffer = new byte[1024];
                while((size = fileInput.read(buffer, 0, 1024)) != -1){
                    outputData.write(buffer, 0, size);
                }
                outputData.close();
                fileInput.close();
                data.close();
                sendMessage(0, fileName.get(i) + "  发送完成");
            }
            sendMessage(0, "所有文件发送完成");
        } catch (Exception e) {
            sendMessage(0, "发送错误:\n" + e.getMessage());
        }
    }
}