package com.zyhant.netty.mqtt.core;

import com.zyhant.netty.mqtt.domain.MqttOptions;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 心跳处理
 * @author zyhant
 * @date 2024/7/30 04:09
 */
public class PingProcessor {

    private static final Logger log = LoggerFactory.getLogger(PingProcessor.class);

    public static void handle(Channel channel, MqttOptions options) {
        int keepAliveTime;
        String clientId;
        try {
            Field fieldKeepAliveTime = options.getClass().getDeclaredField("keepAliveTime");
            fieldKeepAliveTime.setAccessible(true);
            keepAliveTime = (int) fieldKeepAliveTime.get(options);
            fieldKeepAliveTime.setAccessible(false);
            Field fieldClientId = options.getClass().getDeclaredField("clientId");
            fieldClientId.setAccessible(true);
            clientId = (String) fieldClientId.get(options);
            fieldClientId.setAccessible(false);
        } catch (Exception e) {
            log.error("MqttClient错误", e);
            return;
        }
        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(() -> {
            MqttMessage msg = MqttMessageFactory.newMessage(
                    new MqttFixedHeader(MqttMessageType.PINGREQ, false, MqttQoS.AT_MOST_ONCE, false, 0),
                    null,
                    null);
            log.info("MqttClient数据,客户端:" + clientId + ",发起心跳");
            channel.writeAndFlush(msg);
        },0, keepAliveTime, TimeUnit.SECONDS);
    }

}
