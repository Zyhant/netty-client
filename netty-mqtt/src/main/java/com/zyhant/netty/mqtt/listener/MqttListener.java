package com.zyhant.netty.mqtt.listener;

import com.zyhant.netty.common.listener.StatusListener;

/**
 * Mqtt回调
 * @author zyhant
 * @date 2024/7/25 22:30
 */
public interface MqttListener extends StatusListener {

    void onMessage(String topic, String content);

}
