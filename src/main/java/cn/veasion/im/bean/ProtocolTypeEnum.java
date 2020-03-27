package cn.veasion.im.bean;

import io.netty.util.AttributeKey;

/**
 * ProtocolTypeEnum
 *
 * @author Veasion
 * @description
 * @date 2020/3/27
 */
public enum ProtocolTypeEnum {

    HTTP,

    WEB_SOCKET,

    MESSAGE;

    public static final AttributeKey<ProtocolTypeEnum> KEY = AttributeKey.valueOf("ProtocolType");

    public static ProtocolTypeEnum of(Object protocolType) {
        if (protocolType instanceof ProtocolTypeEnum) {
            return (ProtocolTypeEnum) protocolType;
        }
        if (protocolType != null) {
            String protocol = protocolType.toString();
            for (ProtocolTypeEnum value : values()) {
                if (protocol.equalsIgnoreCase(value.name())) {
                    return value;
                }
            }
        }
        return null;
    }

}
