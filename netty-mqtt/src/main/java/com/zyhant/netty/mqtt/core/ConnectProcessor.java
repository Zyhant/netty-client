package com.zyhant.netty.mqtt.core;

import com.zyhant.netty.mqtt.domain.MqttOptions;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;

/**
 * 连接处理
 * @author zyhant
 * @date 2024/7/29 23:21
 */
public class ConnectProcessor {

    private static final Logger log = LoggerFactory.getLogger(ConnectProcessor.class);

    public static void handle(Channel channel, MqttOptions options) {
        String userName;
        byte[] password;
        boolean isWillRetain;
        int willQos;
        boolean isWillFlag;
        boolean isCleanSession;
        int keepAliveTime;
        String clientId;
        String willTopic;
        byte[] willMessage;
        try {
            Field fieldUserName = options.getClass().getDeclaredField("userName");
            fieldUserName.setAccessible(true);
            userName = (String) fieldUserName.get(options);
            fieldUserName.setAccessible(false);
            Field fieldPassword = options.getClass().getDeclaredField("password");
            fieldPassword.setAccessible(true);
            password = (byte[]) fieldPassword.get(options);
            fieldPassword.setAccessible(false);
            Field fieldWillRetain = options.getClass().getDeclaredField("isWillRetain");
            fieldWillRetain.setAccessible(true);
            isWillRetain = (boolean) fieldWillRetain.get(options);
            fieldWillRetain.setAccessible(false);
            Field fieldWillQos = options.getClass().getDeclaredField("willQos");
            fieldWillQos.setAccessible(true);
            willQos = (int) fieldWillQos.get(options);
            fieldWillQos.setAccessible(false);
            Field fieldWillFlag = options.getClass().getDeclaredField("isWillFlag");
            fieldWillFlag.setAccessible(true);
            isWillFlag = (boolean) fieldWillFlag.get(options);
            fieldWillFlag.setAccessible(false);
            Field fieldCleanSession = options.getClass().getDeclaredField("isCleanSession");
            fieldCleanSession.setAccessible(true);
            isCleanSession = (boolean) fieldCleanSession.get(options);
            fieldCleanSession.setAccessible(false);
            Field fieldKeepAliveTime = options.getClass().getDeclaredField("keepAliveTime");
            fieldKeepAliveTime.setAccessible(true);
            keepAliveTime = (int) fieldKeepAliveTime.get(options);
            fieldKeepAliveTime.setAccessible(false);
            Field fieldClientId = options.getClass().getDeclaredField("clientId");
            fieldClientId.setAccessible(true);
            clientId = (String) fieldClientId.get(options);
            fieldClientId.setAccessible(false);
            Field fieldWillTopic = options.getClass().getDeclaredField("willTopic");
            fieldWillTopic.setAccessible(true);
            willTopic = (String) fieldWillTopic.get(options);
            fieldWillTopic.setAccessible(false);
            Field fieldWillMessage = options.getClass().getDeclaredField("willMessage");
            fieldWillMessage.setAccessible(true);
            willMessage = (byte[]) fieldWillMessage.get(options);
            fieldWillMessage.setAccessible(false);
        } catch (Exception e) {
            log.error("MqttClient错误", e);
            return;
        }
        boolean isHasUserName = false;
        boolean isHasPassword = false;
        if (userName != null) {
            isHasUserName = true;
        }
        if (password != null && password.length > 0) {
            isHasPassword = true;
        }
        MqttFixedHeader fixedHeader = new MqttFixedHeader(
                MqttMessageType.CONNECT,
                false,
                MqttQoS.AT_MOST_ONCE,
                false,
                10);
        MqttVersion version = MqttVersion.MQTT_3_1_1;
        MqttConnectVariableHeader variableHeader = new MqttConnectVariableHeader(
                version.protocolName(),
                version.protocolLevel(),
                isHasUserName,
                isHasPassword,
                isWillRetain,
                willQos,
                isWillFlag,
                isCleanSession,
                keepAliveTime);
        MqttConnectPayload payload = new MqttConnectPayload(
                clientId,
                willTopic,
                willMessage,
                userName,
                password);
        MqttConnectMessage msg =  new MqttConnectMessage(fixedHeader, variableHeader, payload);
        log.info("MqttClient数据,客户端:" + clientId + ",发起登录:\n账号:" + userName + "\n密码:" + new String(password, StandardCharsets.UTF_8));
        channel.writeAndFlush(msg);
    }

    public static MqttConnectReturnCode processAck(MqttConnAckMessage message) {
        return message.variableHeader().connectReturnCode();
    }

}
