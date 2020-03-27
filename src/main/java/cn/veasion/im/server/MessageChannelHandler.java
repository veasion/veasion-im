package cn.veasion.im.server;

import cn.veasion.im.bean.MessageTypeEnum;
import cn.veasion.im.bean.MsgData;
import io.netty.channel.ChannelHandlerContext;

/**
 * MessageChannelHandler
 *
 * @author Veasion
 * @description
 * @date 2020/3/24
 */
public class MessageChannelHandler extends AbstractSocketChannelHandler<MsgData> {

    public MessageChannelHandler(ImServer server) {
        super(server);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MsgData msgData) {
        System.out.println("客户端：" + msgData.getMessage());
        if (MessageTypeEnum.DEMO_MESSAGE.equals(msgData.getMessageType())) {
            server.sendToAll(msgData);
        }
    }

}
