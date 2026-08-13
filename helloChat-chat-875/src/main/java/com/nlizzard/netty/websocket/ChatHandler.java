package com.nlizzard.netty.websocket;


import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.nlizzard.enums.MsgTypeEnum;
import com.nlizzard.grace.result.GraceJSONResult;
import com.nlizzard.netty.config.RuntimeConfig;
import com.nlizzard.netty.mq.MessagePublisher;
import com.nlizzard.netty.utils.OkHttpUtil;
import com.nlizzard.netty.utils.RedisClientUtils;
import com.nlizzard.netty.utils.ZookeeperUtils;
import com.nlizzard.pojo.netty.ChatMsg;
import com.nlizzard.pojo.netty.DataContent;
import com.nlizzard.pojo.netty.NettyServerNode;
import com.nlizzard.utils.JsonUtils;
import com.nlizzard.utils.LocalDateUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;
import redis.clients.jedis.RedisClient;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

// SimpleChannelInboundHandler: 对于请求来说，相当于入站(入境)
// TextWebSocketFrame: 用于为websocket专门处理的文本数据对象，Frame是数据(消息)的载体
public class ChatHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    // 用于记录和管理所有客户端的channel组
    public static ChannelGroup clients =
            new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx,
                                TextWebSocketFrame msg) throws Exception {
        // 获得客户端传输过来的消息
        String content = msg.text();

        // 1. 获取客户端发来的消息并且解析
        DataContent dataContent = JsonUtils.jsonToPojo(content, DataContent.class);
        ChatMsg chatMsg = dataContent.getChatMsg();
        String receiverId = chatMsg.getReceiverId();
        String senderId = chatMsg.getSenderId();
        Integer msgType = chatMsg.getMsgType();

        // 获取channel
        Channel currentChannel = ctx.channel();
        String currentChannelId = currentChannel.id().asLongText();
        dataContent.setCurrentChannelId(currentChannelId);

        // 2. 判断消息类型，根据不同的类型来处理不同的业务
        if(Objects.equals(msgType,MsgTypeEnum.KEEPALIVE.type)){
            // 心跳保活消息，直接返回
            return ;
        }
        if (Objects.equals(msgType, MsgTypeEnum.CONNECT_INIT.type)) {
            // 当websocket初次open的时候，初始化channel，把channel和用户userid关联起来
            UserChannelSession.putMultiChannels(senderId, currentChannel);
            UserChannelSession.putUserChannelIdRelation(currentChannelId, senderId);

            NettyServerNode serverNode = dataContent.getServerNode();
            // 初次连接后，该节点下的在线人数累加
            ZookeeperUtils.incrementOnlineCounts(serverNode);

            // 获得ip+端口，在redis中设置关系，以便在前端设备断线后减少在线人数
            RedisClient jedis = RedisClientUtils.getJedisClient();
            // 用户id为key，ip+端口为value，存储到redis中
            jedis.set(senderId, JsonUtils.objectToJson(serverNode));
        }
        // 2.1 当消息类型为文本消息、图片消息、视频消息、语音消息的时候，进行消息的转发
        if (Objects.equals(msgType, MsgTypeEnum.WORDS.type)
                || Objects.equals(msgType, MsgTypeEnum.IMAGE.type)
                || Objects.equals(msgType, MsgTypeEnum.VIDEO.type)
                || Objects.equals(msgType, MsgTypeEnum.VOICE.type)) {
            // 设置消息的发送时间,以服务器的时间为准
            chatMsg.setChatTime(LocalDateTime.now());

            // 判断是否黑名单 start
            // 如果双方只要有一方是黑名单，则终止发送
            GraceJSONResult result = OkHttpUtil.get(RuntimeConfig.gatewayBaseUrl() + "/friendship/isBlack?friendId1st=" + receiverId
                    + "&friendId2nd=" + senderId);
            boolean isBlack = false;
            if (result != null) {
                // data 为 Object，网关返回错误体时可能为 null 或非 Boolean，
                // 直接 (boolean) 拆箱会 NPE / ClassCastException。Boolean.TRUE.equals 对 null 和非 Boolean 均安全。
                isBlack = Boolean.TRUE.equals(result.getData());
            }
            if (isBlack) {
                return;
            }
            // 判断是否黑名单 end

            // 生成消息的唯一id
            String id = IdWorker.getIdStr();
            chatMsg.setMsgId(id);
            // 发布消息到消息队列，保存信息到数据库表
            MessagePublisher.sendMsgToSave(chatMsg);

            // 语音消息增加未读标记
            if (Objects.equals(msgType, MsgTypeEnum.VOICE.type)) {
                chatMsg.setIsRead(false);
            }
            dataContent.setChatMsg(chatMsg);
            // 格式化消息发送时间
            String chatTimeFormat = LocalDateUtils
                    .format(chatMsg.getChatTime(),
                            LocalDateUtils.DATETIME_PATTERN_2);
            dataContent.setChatTime(chatTimeFormat);
            // 发送消息到广播队列
            MessagePublisher.sendMsgToOtherNettyServer(JsonUtils.objectToJson(dataContent));
        }
    }

    /**
     * 客户端连接到服务端之后(打开链接)
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx){
        Channel currentChannel = ctx.channel();
        String currentChannelId = currentChannel.id().asLongText();
        System.out.println("客户端建立连接，channel对应的长id为：" + currentChannelId);

        // 获得客户端的channel，并且存入到ChannelGroup中进行管理(作为一个客户端群组)
        clients.add(currentChannel);
    }

    /**
     * 关闭连接，移除channel
     */
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        Channel currentChannel = ctx.channel();
        String currentChannelId = currentChannel.id().asLongText();
        System.out.println("客户端关闭连接，channel对应的长id为：" + currentChannelId);

        // 发生异常之后关闭连接(关闭channel)
        ctx.channel().close();
        // 随后从ChannelGroup中移除对应的channel
        clients.remove(currentChannel);

        // 连接关闭后，移除多余的会话，并且更新zookeeper中服务器节点在线人数
        updateChannelSessionAndOnlineCounts(currentChannelId);
    }

    /**
     * 发生异常并且捕获，移除channel
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Channel currentChannel = ctx.channel();
        String currentChannelId = currentChannel.id().asLongText();
        System.out.println("发生异常捕获，channel对应的长id为：" + currentChannelId);
        cause.printStackTrace();

        // 发生异常之后关闭连接(关闭channel)
        ctx.channel().close();
        // 随后从ChannelGroup中移除对应的channel
        clients.remove(currentChannel);

        // 连接关闭后，移除多余的会话，并且更新zookeeper中服务器节点在线人数
        updateChannelSessionAndOnlineCounts(currentChannelId);
    }

    /**
     * 更新channelSession和更新zookeeper中服务器节点在线人数
     * @param currentChannelId 当前channel的id
     */
    private void updateChannelSessionAndOnlineCounts(String currentChannelId)throws Exception{
        // 移除多余的会话
        String userId = UserChannelSession.getUserIdByChannelId(currentChannelId);
        if (userId == null || userId.isBlank()) {
            return;
        }

        try {
            UserChannelSession.removeUselessChannels(userId, currentChannelId);

            // 连接关闭后，获得ip+端口，在redis中删除对应的关系，以便在前端设备断线后减少在线人数
            RedisClient jedis = RedisClientUtils.getJedisClient();
            Optional<NettyServerNode> nettyServerNode =
                    findServerNodeForClosedChannel(currentChannelId, jedis::get);
            if (nettyServerNode.isEmpty()) {
                return;
            }

            // 连接关闭后，该节点下的在线人数递减
            ZookeeperUtils.decrementOnlineCounts(nettyServerNode.get());
        } finally {
            UserChannelSession.removeUserChannelIdRelation(currentChannelId);
        }
    }

    static Optional<NettyServerNode> findServerNodeForClosedChannel(String currentChannelId,
                                                                    Function<String, String> serverNodeLoader) {
        String userId = UserChannelSession.getUserIdByChannelId(currentChannelId);
        if (userId == null || userId.isBlank() || serverNodeLoader == null) {
            return Optional.empty();
        }

        String serverNode = serverNodeLoader.apply(userId);
        if (serverNode == null || serverNode.isBlank()) {
            return Optional.empty();
        }

        try {
            return Optional.ofNullable(JsonUtils.jsonToPojo(serverNode, NettyServerNode.class));
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }
}
