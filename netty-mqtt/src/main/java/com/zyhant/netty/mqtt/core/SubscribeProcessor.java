package com.zyhant.netty.mqtt.core;

import com.zyhant.netty.mqtt.domain.MqttOptions;
import com.zyhant.netty.mqtt.utils.MessageIdFactory;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

/**
 * 订阅处理
 * @author zyhant
 * @date 2024/7/30 04:34
 */
public class SubscribeProcessor {

    private static final Logger log = LoggerFactory.getLogger(SubscribeProcessor.class);

    public static void handle(Channel channel, MqttOptions options, int qos, String[] topics) {
        String clientId;
        try {
            Field fieldClientId = options.getClass().getDeclaredField("clientId");
            fieldClientId.setAccessible(true);
            clientId = (String) fieldClientId.get(options);
            fieldClientId.setAccessible(false);
        } catch (Exception e) {
            log.error("MqttClient错误", e);
            return;
        }
        int id = 0;
        try {
            id = MessageIdFactory.get();
            List<MqttTopicSubscription> list = new LinkedList<>();
            for (String topic : topics) {
                list.add(new MqttTopicSubscription(topic, MqttQoS.valueOf(qos)));
            }
            MqttFixedHeader mqttFixedHeader = new MqttFixedHeader(MqttMessageType.SUBSCRIBE, false, MqttQoS.AT_LEAST_ONCE,
                    false, 0);
            MqttMessageIdVariableHeader mqttMessageIdVariableHeader = MqttMessageIdVariableHeader.from(id);
            MqttSubscribePayload mqttSubscribePayload = new MqttSubscribePayload(list);
            MqttSubscribeMessage msg = new MqttSubscribeMessage(mqttFixedHeader, mqttMessageIdVariableHeader, mqttSubscribePayload);
            for (String topic : topics) {
                log.info("MqttClient数据,客户端:" + clientId + ",订阅主题:" + topic);
            }
            channel.writeAndFlush(msg);
        } catch (Exception e) {
            MessageIdFactory.release(id);
            log.error("MqttClient错误", e);
        } finally {
            MessageIdFactory.release(id);
        }
    }

}
