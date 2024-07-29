package com.zyhant.netty.mqtt.domain;

import com.zyhant.netty.common.domain.ConnectOptions;

/**
 * 启动配置
 * @author zyhant
 * @date 2024/7/21 22:56
 */
public class MqttOptions extends ConnectOptions {

    private boolean isWillRetain = false;

    private int willQos = 0;

    private boolean isWillFlag = false;

    private boolean isCleanSession = false;

    private int keepAliveTime = 60;

    private String clientId = "";

    private String willTopic = "";

    private byte[] willMessage;

    private String userName = "";

    private byte[] password;

    /**
     * 设置是否保留遗嘱消息
     * @param willRetain
     */
    public void setWillRetain(boolean willRetain) {
        isWillRetain = willRetain;
    }

    /**
     * 设置遗嘱QoS
     * @param willQos
     */
    public void setWillQos(int willQos) {
        this.willQos = willQos;
    }

    /**
     * 设置遗嘱标志
     * @param willFlag
     */
    public void setWillFlag(boolean willFlag) {
        isWillFlag = willFlag;
    }

    /**
     * 设置清除会话
     * @param cleanSession
     */
    public void setCleanSession(boolean cleanSession) {
        isCleanSession = cleanSession;
    }

    /**
     * 设置心跳时长
     * @param keepAliveTime
     */
    public void setKeepAliveTime(int keepAliveTime) {
        this.keepAliveTime = keepAliveTime;
    }

    /**
     * 设置客户端ID
     * @param clientId
     */
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    /**
     * 设置遗嘱主题
     * @param willTopic
     */
    public void setWillTopic(String willTopic) {
        this.willTopic = willTopic;
    }

    /**
     * 设置遗嘱消息
     * @param willMessage
     */
    public void setWillMessage(byte[] willMessage) {
        this.willMessage = willMessage;
    }

    /**
     * 设置用户名
     * @param userName
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * 设置密码
     * @param password
     */
    public void setPassword(byte[] password) {
        this.password = password;
    }
}
