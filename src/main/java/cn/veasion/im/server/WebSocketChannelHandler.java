package cn.veasion.im.server;

import cn.veasion.im.bean.MessageTypeEnum;
import cn.veasion.im.bean.MsgData;
import com.alibaba.fastjson.JSONObject;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.CharsetUtil;

import java.net.SocketAddress;
import java.util.List;

/**
 * WebSocketHandler
 *
 * @author Veasion
 * @description
 * @date 2020/3/26
 */
public class WebSocketChannelHandler extends AbstractSocketChannelHandler<WebSocketFrame> implements ChannelOutboundHandler {

    private static final MessageToMessageEncoder<MsgData> encoder;

    static {
        encoder = new MessageToMessageEncoder<MsgData>() {
            @Override
            protected void encode(ChannelHandlerContext ctx, MsgData msgData, List<Object> out) {
                String json = JSONObject.toJSONString(msgData);
                if (logger.isDebugEnabled()) {
                    logger.debug("websocket出站：" + json);
                }
                out.add(new TextWebSocketFrame(json));
            }
        };
    }

    public WebSocketChannelHandler(ImServer imServer) {
        super(imServer);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame webSocket) {
        String json = webSocket.content().toString(CharsetUtil.UTF_8);
        if (logger.isDebugEnabled()) {
            logger.debug("websocket入站：" + json);
        }
        MsgData msgData = JSONObject.parseObject(json, MsgData.class);
        if (MessageTypeEnum.SYSTEM_ACTIVE != msgData.getMessageType()) {
            ctx.fireChannelRead(msgData);
        }
    }

    @Override
    public void bind(ChannelHandlerContext ctx, SocketAddress socketAddress, ChannelPromise channelPromise) throws Exception {
        encoder.bind(ctx, socketAddress, channelPromise);
    }

    @Override
    public void connect(ChannelHandlerContext ctx, SocketAddress socketAddress, SocketAddress socketAddress1, ChannelPromise channelPromise) throws Exception {
        encoder.connect(ctx, socketAddress, socketAddress1, channelPromise);
    }

    @Override
    public void disconnect(ChannelHandlerContext ctx, ChannelPromise channelPromise) throws Exception {
        encoder.disconnect(ctx, channelPromise);
    }

    @Override
    public void close(ChannelHandlerContext ctx, ChannelPromise channelPromise) throws Exception {
        encoder.close(ctx, channelPromise);
    }

    @Override
    public void deregister(ChannelHandlerContext ctx, ChannelPromise channelPromise) throws Exception {
        encoder.deregister(ctx, channelPromise);
    }

    @Override
    public void read(ChannelHandlerContext ctx) throws Exception {
        encoder.read(ctx);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object o, ChannelPromise channelPromise) throws Exception {
        encoder.write(ctx, o, channelPromise);
    }

    @Override
    public void flush(ChannelHandlerContext ctx) throws Exception {
        encoder.flush(ctx);
    }
}
