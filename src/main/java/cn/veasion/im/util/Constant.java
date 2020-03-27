package cn.veasion.im.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * Constant
 *
 * @author Veasion
 * @description
 * @date 2020/3/24
 */
public class Constant {

    public static final int PORT = 11630;
    public static final String REMOTE_HOST = "127.0.0.1";

    public static final int MAX_PACK_LENGTH = 2 * 1024 * 1024;
    public static final ByteBuf DELIMITER = Unpooled.copiedBuffer("\r\n".getBytes());

}
