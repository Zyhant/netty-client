package com.zyhant.netty.common.domain;

/**
 * 启动配置
 * @author zyhant
 * @date 2024/7/21 22:56
 */
public class ConnectOptions {

    private String host;

    private int port;

    /**
     * 设置服务器地址
     * @param host
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * 设置服务器端口
     * @param port
     */
    public void setPort(int port) {
        this.port = port;
    }
}
