package com.zyhant.netty.mqtt;

import com.zyhant.netty.common.NettyClient;
import com.zyhant.netty.mqtt.core.*;
import com.zyhant.netty.mqtt.domain.MqttOptions;
import com.zyhant.netty.mqtt.listener.MqttListener;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.mqtt.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

/**
 * @author zyhant
 * @date 2024/7/23 20:28
 */
public abstract class MqttClient extends NettyClient<MqttOptions> {

    private static final Logger log = LoggerFactory.getLogger(MqttClient.class);
    private MqttListener mqttListener;

    public void setMqttListener(MqttListener mqttListener) {
        this.mqttListener = mqttListener;
        super.setListener(mqttListener);
    }

    public void subscribe(int qos, String... topics) {
        if (channel == null) {
            return;
        }
        SubscribeProcessor.handle(super.channel, super.options, qos, topics);
    }

    public void unsubscribe(String... topics) {
        if (channel == null) {
            return;
        }
        UnsubscribeProcessor.handle(super.channel, super.options, topics);
    }

    public void publish(String topic, String content) {
        if (channel == null) {
            return;
        }
        PublishProcessor.handle(super.channel, super.options, topic, content);
    }

    @Override
    protected void initPipeline(ChannelPipeline pipeline) {
        pipeline.addLast("decoder", new MqttDecoder());//解码
        pipeline.addLast("encoder", MqttEncoder.INSTANCE);//编码
        pipeline.addLast("handler", new MqttHandler());
    }

    @Override
    protected void connectSuccess() {
        ConnectProcessor.handle(super.channel, super.options);
    }

    @Override
    protected void disconnect() {
        MqttFixedHeader mqttFixedHeader = new MqttFixedHeader(MqttMessageType.DISCONNECT, false, MqttQoS.AT_MOST_ONCE,
                false, 0x02);
        MqttMessage msg = new MqttMessage(mqttFixedHeader);
        log.info("MqttClient断开连接");
        super.channel.writeAndFlush(msg);
    }

    private class MqttHandler extends SimpleChannelInboundHandler<Object> {

        @Override
        protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object msg) throws Exception {
            if (!(msg instanceof MqttMessage)) {
                return;
            }
            MqttMessage message = (MqttMessage) msg;
            if (message.fixedHeader() == null) {
                log.error("MqttClient错误,Header为空");
                return;
            }
            MqttFixedHeader mqttFixedHeader = message.fixedHeader();
            switch (mqttFixedHeader.messageType()) {
                case CONNACK:
                    if (msg instanceof MqttConnAckMessage) {
                        String error;
                        switch (ConnectProcessor.processAck((MqttConnAckMessage) msg)) {
                            case CONNECTION_ACCEPTED:
                                // 连接成功
                                PingProcessor.handle(channelHandlerContext.channel(), options);
                                return;
                            case CONNECTION_REFUSED_BAD_USER_NAME_OR_PASSWORD:
                                error = "用户名密码错误";
                                break;
                            case CONNECTION_REFUSED_IDENTIFIER_REJECTED:
                                error = "clientId不允许链接";
                                break;
                            case CONNECTION_REFUSED_SERVER_UNAVAILABLE:
                                error = "服务不可用";
                                break;
                            case CONNECTION_REFUSED_UNACCEPTABLE_PROTOCOL_VERSION:
                                error = "mqtt 版本不可用";
                                break;
                            case CONNECTION_REFUSED_NOT_AUTHORIZED:
                                error = "未授权登录";
                                break;
                            default:
                                error = "未知问题";
                                break;
                        }
                        Exception e = new Exception(error);
                        mqttListener.onError(e);
                    }
                    break;
                case PUBLISH:
                    if (msg instanceof MqttPublishMessage) {
                        MqttPublishMessage publishMessage = (MqttPublishMessage) msg;
                        MqttPublishVariableHeader mqttPublishVariableHeader = publishMessage.variableHeader();
                        String topicName = mqttPublishVariableHeader.topicName();
                        ByteBuf payload = publishMessage.payload();
                        String content = payload.toString(StandardCharsets.UTF_8);
                        mqttListener.onMessage(topicName, content);

                        if (mqttFixedHeader.qosLevel() == MqttQoS.AT_LEAST_ONCE
                                || mqttFixedHeader.qosLevel() == MqttQoS.EXACTLY_ONCE) {
                            MqttPubAckMessage mqttPubAckMessage = (MqttPubAckMessage) MqttMessageFactory.newMessage(
                                    new MqttFixedHeader(MqttMessageType.PUBACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
                                    MqttMessageIdVariableHeader.from(mqttPublishVariableHeader.packetId()),
                                    null);
                            channelHandlerContext.channel().writeAndFlush(mqttPubAckMessage);
                        }
                    }
                    break;
            }
        }
    }
}
