package cn.veasion.im.client;

import cn.veasion.im.bean.MsgData;
import cn.veasion.im.protocol.MessageCodec;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * ImClient
 *
 * @author Veasion
 * @description
 * @date 2020/3/24
 */
public class ImClient {

    public static final Logger logger = LoggerFactory.getLogger(ImClient.class);

    private Channel channel;
    private Bootstrap bootstrap;
    private EventLoopGroup group;
    private InetSocketAddress socketAddress;

    public ImClient(String host, int port) {
        socketAddress = InetSocketAddress.createUnresolved(host, port);
    }

    private ImClient createBootstrap() {
        group = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .remoteAddress(socketAddress)
                .option(ChannelOption.TCP_NODELAY, Boolean.TRUE)
                .option(ChannelOption.SO_KEEPALIVE, Boolean.TRUE)
                .option(ChannelOption.SO_REUSEADDR, Boolean.TRUE)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        pipeline.addLast(new IdleStateHandler(0, 0, 10));
                        pipeline.addLast(new MessageCodec());
                        pipeline.addLast(new ClientChannelHandler(ImClient.this));
                    }
                });
        return this;
    }

    public ImClient connect() throws Exception {
        if (bootstrap == null) {
            createBootstrap();
        }
        if (channel == null || !channel.isActive()) {
            ChannelFuture future = bootstrap.connect();
            future.addListener((ChannelFutureListener) (channelFuture) -> {
                if (!channelFuture.isSuccess()) {
                    logger.error("连接失败！正在重连...", channelFuture.cause());
                    try {
                        ImClient.this.connect();
                    } catch (Exception e) {
                        logger.error("重连失败！", e);
                    }
                }
            });
            channel = future.sync().channel();
        }
        return this;
    }

    public void sendMsg(MsgData msg) {
        channel.writeAndFlush(msg);
    }

    public void close() throws InterruptedException {
        if (channel != null) {
            channel.closeFuture().sync();
        }
        if (group != null) {
            group.shutdownGracefully().sync();
        }
    }

}
