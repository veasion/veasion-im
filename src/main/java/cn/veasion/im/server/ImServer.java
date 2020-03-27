package cn.veasion.im.server;

import cn.veasion.im.bean.MsgData;
import cn.veasion.im.bean.Session;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ImServer
 *
 * @author Veasion
 * @description
 * @date 2020/3/25
 */
public class ImServer {

    private static final Logger logger = LoggerFactory.getLogger(ImServer.class);

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private ServerBootstrap bootstrap;

    private final Map<String, Session> clientMap = new ConcurrentHashMap<>();
    private final Map<String, Session> userMap = new ConcurrentHashMap<>();
    private final Map<String, Set<Session>> roomMap = new ConcurrentHashMap<>();

    private ImServer createBootstrap(boolean checkHeartbeat) {
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .option(ChannelOption.TCP_NODELAY, Boolean.TRUE)
                .option(ChannelOption.SO_REUSEADDR, Boolean.TRUE)
                .childOption(ChannelOption.SO_KEEPALIVE, Boolean.TRUE)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        if (checkHeartbeat) {
                            pipeline.addLast(new IdleStateHandler(0, 0, 15));
                        }
                        pipeline.addLast(new ServerChannelHandler(ImServer.this));
                    }
                });
        return this;
    }

    public void bind(int port, boolean checkHeartbeat) throws Exception {
        if (bootstrap == null) {
            createBootstrap(checkHeartbeat);
        }
        bootstrap.bind(port).sync();
    }

    /**
     * 发送消息给用户
     *
     * @param userId
     * @param msg
     */
    public ChannelFuture sendToUser(String userId, MsgData msg) {
        Session session = userMap.get(userId);
        if (session != null) {
            if (session.getChannel().isActive()) {
                return session.getChannel().writeAndFlush(msg);
            }
        }
        return null;
    }

    /**
     * 广播消息给部客户端
     *
     * @param msg
     */
    public int sendToAll(MsgData msg) {
        int count = 0;
        Collection<Session> users = clientMap.values();
        for (Session session : users) {
            if (session.getChannel().isActive()) {
                count++;
                session.getChannel().writeAndFlush(msg);
            }
        }
        return count;
    }

    /**
     * 发送消息到房间
     *
     * @param msg
     */
    public int sendToRoom(String roomNo, MsgData msg) {
        int count = 0;
        Set<Session> users = roomMap.get(roomNo);
        if (users != null) {
            for (Session session : users) {
                if (session.getChannel().isActive()) {
                    count++;
                    session.getChannel().writeAndFlush(msg);
                }
            }
        }
        return count;
    }

    /**
     * 客户端连接成功，用户加入
     *
     * @param ctx
     * @return
     */
    public Session join(ChannelHandlerContext ctx) {
        String key = key(ctx);
        Session session = clientMap.get(key);
        if (session == null) {
            session = ctx.channel().attr(Session.KEY).get();
            if (session == null) {
                session = new Session(ctx.channel(), null);
                logger.info("{} join.", session.getClientIp());
            }
        }
        session.setChannel(ctx.channel());
        clientMap.put(key, session);
        ctx.channel().attr(Session.KEY).set(session);
        return session;
    }

    /**
     * 加入房间
     *
     * @param ctx
     * @param roomNo 房间号
     * @return
     */
    public Session join(ChannelHandlerContext ctx, String roomNo) {
        Session session = join(ctx);
        session.setRoomNo(roomNo);
        Set<Session> users = roomMap.get(roomNo);
        if (users == null) {
            synchronized (roomMap) {
                if (users == null) {
                    users = ConcurrentHashMap.newKeySet();
                    roomMap.put(roomNo, users);
                } else {
                    users = roomMap.get(roomNo);
                }
            }
        }
        users.add(session);
        return session;
    }

    /**
     * 用户登录
     *
     * @param ctx
     * @param userId 用户id
     */
    public void login(ChannelHandlerContext ctx, String userId) {
        Session session = join(ctx);
        session.setUserId(userId);
        session.setLogin(true);
        userMap.put(userId, session);
        logger.info("{} {} login.", session.getClientIp(), userId);
    }

    /**
     * 客户端连接断开，用户退出
     *
     * @param ctx
     * @return
     */
    public Session remove(ChannelHandlerContext ctx) {
        Session session = clientMap.remove(key(ctx));
        if (session == null) {
            return null;
        }
        if (session.getUserId() != null) {
            userMap.remove(session.getUserId());
        }
        if (session.getRoomNo() != null) {
            Set<Session> users = roomMap.get(session.getRoomNo());
            if (users != null) {
                users.remove(session);
                if (users.isEmpty()) {
                    synchronized (roomMap) {
                        users = roomMap.get(session.getRoomNo());
                        if (users != null && users.isEmpty()) {
                            roomMap.remove(session.getRoomNo());
                        }
                    }
                }
            }
        }
        ctx.channel().attr(Session.KEY).set(null);
        logger.info("{} exit.", session.getClientIp());
        return session;
    }

    public Session get(ChannelHandlerContext ctx) {
        Session session = clientMap.get(key(ctx));
        if (session == null) {
            session = ctx.channel().attr(Session.KEY).get();
        }
        return session;
    }

    public Session getByUserId(String userId) {
        return userMap.get(userId);
    }

    public Set<Session> getByRoomNo(String roomNo) {
        return roomMap.get(roomNo);
    }

    private String key(ChannelHandlerContext ctx) {
        return ctx.channel().remoteAddress().toString();
    }

    public Map<String, Session> getClientMap() {
        return clientMap;
    }

    public Map<String, Session> getUserMap() {
        return userMap;
    }

    public Map<String, Set<Session>> getRoomMap() {
        return roomMap;
    }

    public void close() {
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
    }

}
