package com.zyhant.netty.common;

import com.zyhant.netty.common.domain.ConnectOptions;
import com.zyhant.netty.common.listener.StatusListener;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.net.ConnectException;

/**
 * Netty客户端
 * @author zyhant
 * @date 2024/7/22 03:02
 */
public abstract class NettyClient<T extends ConnectOptions> {

    private static final Logger log = LoggerFactory.getLogger(NettyClient.class);
    protected T options;
    protected Channel channel;
    protected String host;
    protected int port;
    private int maxReconnectTimesOnLost = 0;
    private EventLoopGroup parentGroup;
    private boolean isClosed = false;
    private StatusListener listener;

    /**
     * 当maxTimes大于0时，如果发生掉线，则自动尝试重连，重连成功则回调onConnected方法，
     * 重连次数用完则回调onConnectLost方法。
     *
     * @param maxTimes 重试最大次数
     */
    public void setReconnect(int maxTimes) {
        this.maxReconnectTimesOnLost = maxTimes;
    }

    synchronized public void connect(T options) {
        if (this.options != null) {
            return;
        }
        this.options = options;
        try {
            Field fieldHost = options.getClass().getDeclaredField("host");
            fieldHost.setAccessible(true);
            this.host = (String) fieldHost.get(options);
            fieldHost.setAccessible(false);
            Field fieldPort = options.getClass().getDeclaredField("port");
            fieldPort.setAccessible(true);
            this.port = (int) fieldPort.get(options);
            fieldPort.setAccessible(false);
        } catch (Exception e) {
            log.error("NettyClient错误", e);
            return;
        }
        try {
            this.doConnect();
            this.setClosed(false);
        } catch (Exception e) {
            this.onConnectFailed(e);
            throw e;
        }
    }

    public void close() {
        this.setClosed(true);

        if (this.channel != null) {
            try {
                this.disconnect();
                log.info("NettyClient断开连接");
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                this.channel.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            this.channel = null;
        }
    }

    protected void setListener(StatusListener listener) {
        this.listener = listener;
    }

    protected void onConnected() {
        if (this.listener != null) {
            this.listener.onSuccess();
        }
    }

    private void doConnect() {
        try {
            this.parentGroup = new NioEventLoopGroup();
            Bootstrap bootstrap = new Bootstrap()
                    .group(this.parentGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel channel) throws Exception {
                            initPipeline(channel.pipeline());
                        }
                    });
            ChannelFuture ch = bootstrap.connect(this.host, this.port).sync();
            this.channel = ch.channel();
            log.info("NettyClient连接成功:\n地址:" + this.host + "\n端口:" + this.port);
        } catch (Exception e) {
            log.error("NettyClient错误", e);
            if (this.parentGroup != null) {
                this.parentGroup.shutdownGracefully();
            }
            return;
        }
        if (this.channel == null) {
            return;
        }
        this.connectSuccess();
        try {
            this.channel.closeFuture().sync();
        } catch (Exception e) {
            log.error("NettyClient错误", e);
        } finally {
            if (this.parentGroup != null) {
                this.parentGroup.shutdownGracefully();
            }
            if (!this.isClosed()) {
                // 非主动断开，可能源于服务器原因
                Exception e = new ConnectException("NettyClient意外关闭");
                log.error("NettyClient错误", e);
                this.onConnectLost(e);
            } else {
                log.info("NettyClient停止成功:\n地址:" + this.host + "\n端口:" + this.port);
            }
        }
    }

    private void doReconnect(Throwable throwable) {
        boolean bSuccess = false;
        for (int i = 0; i < this.maxReconnectTimesOnLost; i++) {
            log.info("NettyClient重连:" + this.host + ",次数：" + i);
            this.onReconnectStart(i);
            try {
                this.doConnect();
                log.info("NettyClient重连:" + this.host + "成功");
                bSuccess = true;
                break;
            } catch (Exception e) {
                log.info("NettyClient重连:" + this.host + "失败");
            }
        }
        if (!bSuccess) {
            this.onReconnectFailed(throwable);
            log.info("NettyClient重连:" + this.host + ",重试次数已达上限");
        }
    }

    private boolean isClosed() {
        return this.isClosed;
    }

    private void setClosed(boolean closed) {
        this.isClosed = closed;
    }

    private void onConnectFailed(Throwable throwable) {
        this.close();
        if (this.listener != null) {
            this.listener.onError(throwable);
        }
    }

    private void onConnectLost(Throwable throwable) {
        this.close();

        if (this.maxReconnectTimesOnLost > 0) {
            this.doReconnect(throwable);
        } else {
            if (this.listener != null) {
                this.listener.onLost(throwable);
            }
        }
    }

    private void onReconnectStart(int num) {
        if (this.listener != null) {
            this.listener.onReconnect(num);
        }
    }

    private void onReconnectFailed(Throwable throwable) {
        this.close();
        if (this.listener != null) {
            this.listener.onLost(throwable);
        }
    }

    protected abstract void initPipeline(ChannelPipeline pipeline);

    protected abstract void connectSuccess();

    protected abstract void disconnect();

}
