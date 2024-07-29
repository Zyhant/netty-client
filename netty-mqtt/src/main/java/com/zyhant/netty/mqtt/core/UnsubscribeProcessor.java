package com.zyhant.netty.mqtt.core;

import com.zyhant.netty.mqtt.domain.MqttOptions;
import com.zyhant.netty.mqtt.utils.MessageIdFactory;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Arrays;

/**
 * 取消订阅处理
 * @author zyhant
 * @date 2024/7/30 04:48
 */
public class UnsubscribeProcessor {

    private static final Logger log = LoggerFactory.getLogger(UnsubscribeProcessor.class);

    public static void handle(Channel channel, MqttOptions options, String[] topics) {
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
            MqttFixedHeader mqttFixedHeader = new MqttFixedHeader(MqttMessageType.UNSUBSCRIBE, false, MqttQoS.AT_MOST_ONCE,
                    false, 0x02);
            MqttMessageIdVariableHeader variableHeader = MqttMessageIdVariableHeader.from(id);
            MqttUnsubscribePayload mqttUnsubscribeMessage = new MqttUnsubscribePayload(Arrays.asList(topics));
            MqttUnsubscribeMessage msg = new MqttUnsubscribeMessage(mqttFixedHeader, variableHeader, mqttUnsubscribeMessage);
            for (String topic : topics) {
                log.info("MqttClient数据,客户端:" + clientId + ",取消订阅主题:" + topic);
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
