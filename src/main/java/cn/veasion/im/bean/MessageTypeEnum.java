package cn.veasion.im.bean;

/**
 * MessageTypeEnum
 *
 * @author Veasion
 * @description
 * @date 2020/3/24
 */
public enum MessageTypeEnum {

    SYSTEM_ACTIVE,

    SYSTEM_ERROR,

    TEST_MESSAGE,

    DEMO_MESSAGE;

    public static MessageTypeEnum of(String messageType) {
        if (messageType != null) {
            for (MessageTypeEnum value : values()) {
                if (messageType.equalsIgnoreCase(value.name())) {
                    return value;
                }
            }
        }
        return null;
    }

}
