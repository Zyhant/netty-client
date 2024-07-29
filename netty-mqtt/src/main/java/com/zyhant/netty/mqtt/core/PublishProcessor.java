package com.zyhant.netty.mqtt.core;

import com.zyhant.netty.mqtt.domain.MqttOptions;
import com.zyhant.netty.mqtt.utils.MessageIdFactory;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;

/**
 * 发布消息处理
 * @author zyhant
 * @date 2024/7/30 04:55
 */
public class PublishProcessor {

    private static final Logger log = LoggerFactory.getLogger(PublishProcessor.class);

    public static void handle(Channel channel, MqttOptions options, String topic, String content) {
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
            MqttPublishMessage msg = (MqttPublishMessage) MqttMessageFactory.newMessage(
                    new MqttFixedHeader(MqttMessageType.PUBLISH, false, MqttQoS.AT_LEAST_ONCE, false, 0),
                    new MqttPublishVariableHeader(topic, id),
                    Unpooled.buffer().writeBytes(content.getBytes(StandardCharsets.UTF_8)));
            log.info("MqttClient数据,客户端:" + clientId + ",发布消息:\n主题:\n" + topic + "\n内容:\n" + content);
            channel.writeAndFlush(msg);
        } catch (Exception e) {
            MessageIdFactory.release(id);
            log.error("MqttClient错误", e);
        } finally {
            MessageIdFactory.release(id);
        }
    }
}
