package cn.veasion.im.protocol;

import cn.veasion.im.bean.MessageTypeEnum;
import cn.veasion.im.bean.MsgData;
import cn.veasion.im.util.Constant;
import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.List;

/**
 * MessageCodec
 *
 * @author Veasion
 * @description
 * @date 2020/3/25
 */
public class MessageCodec extends DelimiterBasedFrameDecoder implements ChannelOutboundHandler {

    private static Logger logger = LoggerFactory.getLogger(MessageCodec.class);

    private static Charset charset = CharsetUtil.UTF_8;
    private static final MessageToMessageEncoder<MsgData> encoder;

    static {
        encoder = new MessageToMessageEncoder<MsgData>() {
            @Override
            protected void encode(ChannelHandlerContext ctx, MsgData msgData, List out) {
                String json = JSONObject.toJSONString(msgData);
                if (logger.isDebugEnabled()) {
                    logger.debug("编码：" + json);
                }
                ByteBuf byteBuf = ByteBufUtil.encodeString(ctx.alloc(), CharBuffer.wrap(json), charset, Constant.DELIMITER.capacity());
                byteBuf.writeBytes(Constant.DELIMITER.array());
                out.add(byteBuf);
            }
        };
    }

    public MessageCodec() {
        super(Constant.MAX_PACK_LENGTH, Constant.DELIMITER);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf buffer) throws Exception {
        Object obj = super.decode(ctx, buffer);
        if (obj != null) {
            try {
                String json = ((ByteBuf) obj).toString(charset);
                if (logger.isDebugEnabled()) {
                    logger.debug("解码：" + json);
                }
                MsgData msgData = JSONObject.parseObject(json, MsgData.class);
                if (MessageTypeEnum.SYSTEM_ACTIVE != msgData.getMessageType()) {
                    return msgData;
                }
            } catch (Exception e) {
                String address = ctx.channel().remoteAddress().toString();
                logger.error(address + "解析数据异常！", e);
                ctx.channel().writeAndFlush(MsgData.error("数据解析异常：" + e.getMessage()));
            }
        }
        return null;
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
