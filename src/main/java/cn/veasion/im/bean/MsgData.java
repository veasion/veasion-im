package cn.veasion.im.bean;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.Serializable;

/**
 * MsgData
 *
 * @author Veasion
 * @description
 * @date 2020/3/24
 */
public class MsgData implements Serializable {

    private MessageTypeEnum messageType;
    private Object message;

    public static MsgData of(Object message, MessageTypeEnum messageType) {
        MsgData data = new MsgData();
        data.message = message;
        data.messageType = messageType;
        return data;
    }

    public static MsgData error(String message) {
        return of(message, MessageTypeEnum.SYSTEM_ERROR);
    }

    public static MsgData ping() {
        return of("ping", MessageTypeEnum.SYSTEM_ACTIVE);
    }

    public static MsgData pong() {
        return of("pong", MessageTypeEnum.SYSTEM_ACTIVE);
    }

    public <T> T convertMessage(Class<T> clazz) {
        if (message == null) {
            return null;
        }
        if (message.getClass() == clazz) {
            return (T) message;
        } else if (message instanceof JSON) {
            JSON json = (JSON) message;
            return json.toJavaObject(clazz);
        } else {
            String json = JSON.toJSONString(message);
            return JSONObject.parseObject(json, clazz);
        }
    }

    public MessageTypeEnum getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageTypeEnum messageType) {
        this.messageType = messageType;
    }

    public Object getMessage() {
        return message;
    }

    public void setMessage(Object message) {
        this.message = message;
    }
}
