package cn.veasion.im.server;

import cn.veasion.im.bean.ProtocolTypeEnum;
import cn.veasion.im.protocol.MessageCodec;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.cors.CorsConfig;
import io.netty.handler.codec.http.cors.CorsConfigBuilder;
import io.netty.handler.codec.http.cors.CorsHandler;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.Attribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * ServerChannelHandler
 *
 * @author Veasion
 * @description
 * @date 2020/3/26
 */
@ChannelHandler.Sharable
public class ServerChannelHandler extends ChannelInboundHandlerAdapter {

    private static Logger logger = LoggerFactory.getLogger(ServerChannelHandler.class);

    private static final String WEBSOCKET_PATH = "/websocket";
    private static final String WEB_SOCKET_PROTOCOL = "GET ".concat(WEBSOCKET_PATH).concat(" ");
    private static final String[] HTTP_PROTOCOL = new String[]{"GET /", "POST /", "PUT /", "DELETE /", "OPTIONS /", "HEAD /", "PATCH /", "TRACE /", "CONNECT /"};

    private static CorsConfig corsConfig = CorsConfigBuilder.forAnyOrigin().allowNullOrigin().allowCredentials().build();

    private final ByteToMessageDecoder decoder;

    public ServerChannelHandler(final ImServer server) {
        decoder = new ByteToMessageDecoder() {
            @Override
            protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> list) {
                distribute(server, ctx, byteBuf, list);
            }
        };
    }

    private static void distribute(ImServer server, ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> list) {
        // 协议分发
        String protocol = getStartToString(byteBuf, WEB_SOCKET_PROTOCOL.length());
        Attribute<ProtocolTypeEnum> attr = ctx.channel().attr(ProtocolTypeEnum.KEY);
        ChannelPipeline pipeline = ctx.pipeline();
        if (isFullHttp(protocol)) {
            // http / websocket
            pipeline.addLast(new HttpServerCodec());
            pipeline.addLast(new HttpObjectAggregator(65536));
            pipeline.addLast(new ChunkedWriteHandler());
            pipeline.addLast(new CorsHandler(corsConfig));
            if (protocol.startsWith(WEB_SOCKET_PROTOCOL)) {
                // websocket
                attr.set(ProtocolTypeEnum.WEB_SOCKET);
                pipeline.addLast(new WebSocketServerProtocolHandler(WEBSOCKET_PATH, null, true));
                pipeline.addLast(new WebSocketChannelHandler(server));
            } else {
                // http
                attr.set(ProtocolTypeEnum.HTTP);
                pipeline.addLast(new HttpChannelHandler());
            }
        } else {
            // message
            attr.set(ProtocolTypeEnum.MESSAGE);
            pipeline.addLast(new MessageCodec());
        }

        logger.info("remote: {}, 协议：{}", ctx.channel().remoteAddress(), attr.get());

        pipeline.addLast(new MessageChannelHandler(server));

        pipeline.remove(ServerChannelHandler.class);
        pipeline.fireChannelRegistered();
        pipeline.fireChannelActive();
    }

    private static boolean isFullHttp(String protocol) {
        if (protocol != null && protocol.length() > 0) {
            if (protocol.charAt(0) == '{') {
                return false;
            }
            for (String method : HTTP_PROTOCOL) {
                if (protocol.startsWith(method)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static String getStartToString(ByteBuf in, int maxLength) {
        int length = in.readableBytes();
        if (length > maxLength) {
            length = maxLength;
        }
        byte[] bytes = new byte[length];
        in.markReaderIndex();
        in.readBytes(bytes);
        in.resetReaderIndex();
        return new String(bytes);
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        decoder.channelRead(ctx, msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        decoder.channelReadComplete(ctx);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        decoder.userEventTriggered(ctx, evt);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        decoder.channelInactive(ctx);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        decoder.handlerRemoved(ctx);
    }

}
