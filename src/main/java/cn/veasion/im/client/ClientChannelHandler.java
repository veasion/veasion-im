package cn.veasion.im.client;

import cn.veasion.im.bean.MessageTypeEnum;
import cn.veasion.im.bean.MsgData;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * ClientChannelHandler
 *
 * @author Veasion
 * @description
 * @date 2020/3/24
 */
public class ClientChannelHandler extends SimpleChannelInboundHandler<MsgData> {

    private static final Logger logger = LoggerFactory.getLogger(ClientChannelHandler.class);

    private ImClient client;

    public ClientChannelHandler(ImClient client) {
        this.client = client;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(MsgData.ping());
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.info("连接断开，正在重连...");
        ctx.channel().eventLoop().schedule(() -> {
            for (int i = 0; i < 3; i++) {
                try {
                    client.connect();
                    break;
                } catch (Exception e) {
                    logger.error("重连失败！+{}", i + 1, e);
                }
            }
        }, 1L, TimeUnit.SECONDS);
        super.channelInactive(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MsgData msgData) {
        System.out.println("服务端：" + msgData.getMessage());
        if (MessageTypeEnum.SYSTEM_ACTIVE != msgData.getMessageType()) {
            ctx.fireChannelRead(msgData);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            if (e.state() == IdleState.ALL_IDLE) {
                ctx.writeAndFlush(MsgData.ping());
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

}
