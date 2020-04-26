package com.gzk.netty.utils;

import android.os.Environment;

public class Constant {
    public static volatile int PORT = 3351;
    public static String KEY_IP = "key_ip";
    public static String KEY_PORT = "key_port";
    public static String KEY_USER = "key_user";
    public static String KEY_FILE_SEND = "FileSendNow";
    public static String KEY_FILE_LENGTH = "FileLength";
    public static String FILE_SAVE_PATH_ROOT = Environment.getExternalStorageDirectory() + "/A";
    public static String FILE_SAVE_PATH_DIR = FILE_SAVE_PATH_ROOT+"/files";
    public final static String START_SEND = "startSend";
}
