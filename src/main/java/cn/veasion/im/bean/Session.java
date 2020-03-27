package cn.veasion.im.bean;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Session
 *
 * @author Veasion
 * @description
 * @date 2020/3/26
 */
public class Session {

    public static final AttributeKey<Session> KEY = AttributeKey.valueOf("Session");

    private String userId; // 用户id
    private boolean login; // 是否登录

    private String roomNo; // 房间号
    private Channel channel; // 连接渠道

    private Map<String, Object> attr = new ConcurrentHashMap<>(); // 属性

    public Session(Channel channel, String userId) {
        this.channel = channel;
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isLogin() {
        return login;
    }

    public void setLogin(boolean login) {
        this.login = login;
    }

    public String getClientIp() {
        String address = getRemoteAddress();
        int index = address.indexOf(":");
        if (index != -1) {
            return address.substring(1, index);
        } else {
            return address;
        }
    }

    public String getRemoteAddress() {
        return this.channel.remoteAddress().toString();
    }

    public String getRoomNo() {
        return roomNo;
    }

    public void setRoomNo(String roomNo) {
        this.roomNo = roomNo;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public Object getAttr(String key) {
        return attr.get(key);
    }

    public Object getAttr(String key, Object defaultValue) {
        return attr.getOrDefault(key, defaultValue);
    }

    public void setAttr(String key, Object value) {
        attr.put(key, value);
    }

}
