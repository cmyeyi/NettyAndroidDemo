package com.gzk.netty;

/**
 * @Description: <p>
 * <p>
 * @Project: NettyAndroidDemo
 * @Files: ${CLASS_NAME}
 * @Author: HQ
 * @Version: 0.0.1
 * @Date: 2020/4/13 11:43
 * @Copyright:
 */
public interface ConnectListener {
    void onConnectSuccess(String ip);
    void onDisconnect();
}
