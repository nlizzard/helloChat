package com.nlizzard.netty.websocket;


import com.nlizzard.enums.MsgTypeEnum;
import com.nlizzard.pojo.netty.ChatMsg;
import com.nlizzard.pojo.netty.DataContent;
import com.nlizzard.utils.JsonUtils;
import com.nlizzard.utils.LocalDateUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

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
        System.out.println("接受到的数据：" + content);

        // 1. 获取客户端发来的消息并且解析
        DataContent dataContent = JsonUtils.jsonToPojo(content, DataContent.class);
        ChatMsg chatMsg = dataContent.getChatMsg();

        String msgText = chatMsg.getMsg();
        String receiverId = chatMsg.getReceiverId();
        String senderId = chatMsg.getSenderId();
        Integer msgType = chatMsg.getMsgType();
        System.out.println("客户端消息：" + msgText);
        System.out.println("客户端消息发送人ID：" + receiverId);

        // 获取channel
        Channel currentChannel = ctx.channel();
        String currentChannelId = currentChannel.id().asLongText();
        String currentChannelIdShort = currentChannel.id().asShortText();

        System.out.println("客户端currentChannelId：" + currentChannelId);
        System.out.println("客户端currentChannelIdShort：" + currentChannelIdShort);

        // 2. 判断消息类型，根据不同的类型来处理不同的业务
        if (Objects.equals(msgType, MsgTypeEnum.CONNECT_INIT.type)) {
            // 当websocket初次open的时候，初始化channel，把channel和用户userid关联起来
            UserChannelSession.putMultiChannels(senderId, currentChannel);
            UserChannelSession.putUserChannelIdRelation(currentChannelId, senderId);
        }
        // 2.1 当消息类型为文本消息、图片消息、视频消息、语音消息的时候，进行消息的转发
        if (Objects.equals(msgType, MsgTypeEnum.WORDS.type)
                || Objects.equals(msgType, MsgTypeEnum.IMAGE.type)
                || Objects.equals(msgType, MsgTypeEnum.VIDEO.type)
                || Objects.equals(msgType, MsgTypeEnum.VOICE.type)) {
            // 设置消息的发送时间,以服务器的时间为准
            chatMsg.setChatTime(LocalDateTime.now());

            // 发送消息
            List<Channel> receiverChannels = UserChannelSession.getMultiChannels(receiverId);
            if (receiverChannels == null || receiverChannels.isEmpty()) {
                // receiverChannels为空，表示用户离线/断线状态，消息不需要发送，TODO: 存储到数据库
                chatMsg.setIsReceiverOnLine(false);
            } else {
                chatMsg.setIsReceiverOnLine(true);
                // 当receiverChannels不为空的时候，接收方同账户多端设备接受消息
                for (Channel c : receiverChannels) {
                    Channel findChannel = clients.find(c.id());

                    if (findChannel == null) continue;

                    if (Objects.equals(msgType, MsgTypeEnum.VOICE.type)) {
                        chatMsg.setIsRead(false);
                    }
                    dataContent.setChatMsg(chatMsg);
                    String chatTimeFormat = LocalDateUtils
                            .format(chatMsg.getChatTime(),
                                    LocalDateUtils.DATETIME_PATTERN_2);
                    dataContent.setChatTime(chatTimeFormat);
                    // 发送消息给在线的用户
                    findChannel.writeAndFlush(
                            new TextWebSocketFrame(
                                    JsonUtils.objectToJson(dataContent)));

                }
            }
        }
        // 同步消息到发送方其他设备端
        List<Channel> myOtherChannels = UserChannelSession
                .getMyOtherChannels(senderId, currentChannelId);
        // 没有其他设备端在线，不需要同步消息
        if(myOtherChannels == null || myOtherChannels.isEmpty()) return;

        // 执行消息同步
        for (Channel c : myOtherChannels) {
            Channel findChannel = clients.find(c.id());
            if (findChannel != null) {
                dataContent.setChatMsg(chatMsg);
                String chatTimeFormat = LocalDateUtils
                        .format(chatMsg.getChatTime(),
                                LocalDateUtils.DATETIME_PATTERN_2);
                dataContent.setChatTime(chatTimeFormat);
                // 同步消息给在线的其他设备端
                findChannel.writeAndFlush(
                        new TextWebSocketFrame(
                                JsonUtils.objectToJson(dataContent)));
            }
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
    public void handlerRemoved(ChannelHandlerContext ctx){
        Channel currentChannel = ctx.channel();
        String currentChannelId = currentChannel.id().asLongText();
        System.out.println("客户端关闭连接，channel对应的长id为：" + currentChannelId);

        // 移除多余的会话
        String userId = UserChannelSession.getUserIdByChannelId(currentChannelId);
        UserChannelSession.removeUselessChannels(userId, currentChannelId);

        clients.remove(currentChannel);
    }

    /**
     * 发生异常并且捕获，移除channel
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause){
        Channel currentChannel = ctx.channel();
        String currentChannelId = currentChannel.id().asLongText();
        System.out.println("发生异常捕获，channel对应的长id为：" + currentChannelId);

        // 发生异常之后关闭连接(关闭channel)
        ctx.channel().close();
        // 随后从ChannelGroup中移除对应的channel
        clients.remove(currentChannel);

        // 移除多余的会话
        String userId = UserChannelSession.getUserIdByChannelId(currentChannelId);
        UserChannelSession.removeUselessChannels(userId, currentChannelId);
    }
}
