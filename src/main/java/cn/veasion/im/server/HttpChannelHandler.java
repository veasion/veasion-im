package cn.veasion.im.server;

import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * HttpChannelHandler
 *
 * @author Veasion
 * @description
 * @date 2020/3/26
 */
public class HttpChannelHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static final String STATIC_PREFIX = "/static/";
    private static final String INDEX_PATH = "/static/index.html";

    private static final String CONTENT_TYPE_HTML = "text/html; charset=UTF-8";
    private static final String CONTENT_TYPE_JSON = "application/json; charset=UTF-8";
    private static final String CONTENT_TYPE_JS = "application/javascript; charset=UTF-8";

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) {
        String uri = req.uri();
        HttpMethod method = req.method();
        String content = req.content().toString(CharsetUtil.UTF_8);

        if ("/".equals(uri)) {
            sendRedirect(ctx, INDEX_PATH);
        } else if (uri.startsWith(STATIC_PREFIX)) {
            sendStatic(ctx, uri);
        } else {
            DefaultHttpHeaders headers = (DefaultHttpHeaders) req.headers();
            Map<String, Object> map = new HashMap<>();
            map.put("uri", uri);
            map.put("content", content);
            map.put("method", method.name());
            map.put("headers", headers.entries());
            sendJson(ctx, map);
        }
    }

    private void sendRedirect(ChannelHandlerContext ctx, String newUri) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FOUND, Unpooled.EMPTY_BUFFER);
        response.headers().set(HttpHeaderNames.LOCATION, newUri);
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private void sendStatic(ChannelHandlerContext ctx, String uri) {
        int index = uri.lastIndexOf("?");
        if (index != -1) {
            uri = uri.substring(0, index);
        }
        ByteBuf byteBuf = loadFile(uri);
        if (byteBuf != null) {
            String contentType = CONTENT_TYPE_HTML;
            if (uri.endsWith(".js")) {
                contentType = CONTENT_TYPE_JS;
            }
            response(ctx, byteBuf, contentType);
        } else {
            // 404
            DefaultFullHttpResponse http404 = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND);
            ctx.writeAndFlush(http404).addListener(ChannelFutureListener.CLOSE);
        }
    }

    private void sendJson(ChannelHandlerContext ctx, Object result) {
        String json = JSONObject.toJSONString(result);
        ByteBuf byteBuf = Unpooled.copiedBuffer(json, CharsetUtil.UTF_8);
        response(ctx, byteBuf, CONTENT_TYPE_JSON);
    }

    private void response(ChannelHandlerContext ctx, ByteBuf result, String contentType) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, result);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, contentType);
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private static ByteBuf loadFile(String uri) {
        InputStream input = HttpChannelHandler.class.getResourceAsStream(uri);
        if (input != null) {
            try {
                int n;
                byte[] buffer = new byte[1024];
                ByteBuf byteBuf = Unpooled.buffer();
                while (-1 != (n = input.read(buffer))) {
                    byteBuf.writeBytes(buffer, 0, n);
                }
                input.close();
                return byteBuf;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}
