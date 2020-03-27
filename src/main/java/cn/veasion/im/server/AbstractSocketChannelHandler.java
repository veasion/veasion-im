package cn.veasion.im.server;

import cn.veasion.im.bean.MsgData;
import cn.veasion.im.bean.ProtocolTypeEnum;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AbstractSocketChannelHandler
 *
 * @author Veasion
 * @description
 * @date 2020/3/26
 */
public abstract class AbstractSocketChannelHandler<T> extends SimpleChannelInboundHandler<T> {

    protected static Logger logger = LoggerFactory.getLogger(AbstractSocketChannelHandler.class);

    protected ImServer server;

    public AbstractSocketChannelHandler(ImServer server) {
        this.server = server;
    }

    protected boolean isHttp(ChannelHandlerContext ctx) {
        return ProtocolTypeEnum.HTTP.equals(ctx.channel().attr(ProtocolTypeEnum.KEY).get());
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        if (!isHttp(ctx)) {
            server.join(ctx);
        }
        super.channelRegistered(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(MsgData.pong());
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (!isHttp(ctx)) {
            server.remove(ctx);
        }
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        String address = ctx.channel().remoteAddress().toString();
        logger.error(address + "发生异常！", cause);
        ctx.writeAndFlush(MsgData.error("发生异常：" + cause.getMessage()));
        // ctx.close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            if (e.state() == IdleState.ALL_IDLE) {
                // 断开连接
                ctx.disconnect();
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
