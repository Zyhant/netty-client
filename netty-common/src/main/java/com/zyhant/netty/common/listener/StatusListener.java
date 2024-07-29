package com.zyhant.netty.common.listener;

/**
 * 连接状态回调
 * @author zyhant
 * @date 2024/7/25 21:27
 */
public interface StatusListener {

    void onSuccess();

    void onError(Throwable throwable);

    void onLost(Throwable throwable);

    void onReconnect(int number);

}
