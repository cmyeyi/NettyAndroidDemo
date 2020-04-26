package com.gzk.netty.bean;

import com.google.gson.Gson;

import java.io.Serializable;

public class FileInfo implements Serializable {

    /**
     * 文件传输结果：1 成功  -1 失败
     */
    public static final int FLAG_SUCCESS = 1;
    public static final int FLAG_FAILURE = -1;


    /**
     * 文件路径
     */
    private String filePath;

    /**
     * 文件大小
     */
    private long size;

    /***
     * 文件名
     */
    private String fileName;

    /**
     * 文件传送结果
     */
    private int result;

    /**
     * 传输进度
     */
    private int progress;

    public FileInfo(String filePath, long size) {
        this.filePath = filePath;
        this.size = size;
    }

    public FileInfo() {

    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public static String toJsonStr(FileInfo fileInfo) {
        return new Gson().toJson(fileInfo);
    }

    public static FileInfo toFileInfo(String jsonStr) {
        return new Gson().fromJson(jsonStr, FileInfo.class);
    }

    @Override
    public String toString() {
        return "FileInfo:{" +
                "filePath='" + filePath + '\'' +
                ", size=" + size +
                '}';
    }
}
