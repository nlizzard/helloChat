package com.nlizzard.netty.websocket;

import com.nlizzard.pojo.netty.DataContent;
import com.nlizzard.utils.JsonUtils;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 用于userID和Channel的会话管理
 *
 * 线程安全说明：本类被 Netty 多个 IO 线程并发调用（同一用户多端上线、大量用户并发建连），
 * 因此内部集合使用 ConcurrentHashMap；同一 userId 的 List<Channel> 使用 CopyOnWriteArrayList，
 * 并通过 compute 对“读-改-写”做原子互斥，避免并发下丢通道或 ConcurrentModificationException。
 */
public class UserChannelSession {

    // 用于多端同时接受消息，允许同一个账号在多个设备同时在线，比如iPad、iPhone、Mac等设备同时收到消息
    // key: userId, value: 多个用户的channel（CopyOnWriteArrayList：遍历安全，避免并发修改异常）
    private static final ConcurrentHashMap<String, List<Channel>> multiSession = new ConcurrentHashMap<>();

    // 用于记录用户id和客户端channel长id的关联关系  channelId : userId
    private static final ConcurrentHashMap<String, String> userChannelIdRelation = new ConcurrentHashMap<>();

    /**
     * 当websocket初次open的时候，初始化channel，把channel和用户userid关联起来
     */
    public static void putUserChannelIdRelation(String channelId, String userId) {
        userChannelIdRelation.put(channelId, userId);
    }

    /**
     * 根据channelId获得userId
     */
    public static String getUserIdByChannelId(String channelId) {
        return userChannelIdRelation.get(channelId);
    }

    /**
     * 移除channelId和userId的关联关系
     */
    public static void removeUserChannelIdRelation(String channelId) {
        userChannelIdRelation.remove(channelId);
    }

    /**
     * 当websocket初次open的时候，初始化channel，把channel列表和用户userid关联起来
     *
     * 使用 compute 原子地执行“读取旧列表 -> 追加 channel -> 写回”，
     * 保证同一 userId 在多线程并发建连时不会互相覆盖丢失通道。
     */
    public static void putMultiChannels(String userId, Channel channel) {
        multiSession.compute(userId, (key, channels) -> {
            if (channels == null || channels.isEmpty()) {
                // 新建为 CopyOnWriteArrayList，保证后续遍历安全
                List<Channel> list = new CopyOnWriteArrayList<>();
                list.add(channel);
                return list;
            }
            // 兼容历史数据：若旧值不是 CopyOnWriteArrayList（理论上不会发生），这里统一升级
            if (!(channels instanceof CopyOnWriteArrayList)) {
                List<Channel> list = new CopyOnWriteArrayList<>(channels);
                list.add(channel);
                return list;
            }
            channels.add(channel);
            return channels;
        });
    }

    /**
     * 根据userId获得channel列表
     */
    public static List<Channel> getMultiChannels(String userId) {
        return multiSession.get(userId);
    }

    /**
     * 移除不再使用的会话
     *
     * 使用 compute 原子地遍历并移除指定 channelId，避免遍历中途修改列表抛
     * ConcurrentModificationException；若移除后列表为空则移除整个 key，避免空列表残留。
     */
    public static void removeUselessChannels(String userId, String channelId) {
        multiSession.compute(userId, (key, channels) -> {
            if (channels == null || channels.isEmpty()) {
                return null;
            }
            channels.removeIf(c -> c.id().asLongText().equals(channelId));
            return channels.isEmpty() ? null : channels;
        });
    }

    /**
     * 获得除了当前channelId之外的其他channel列表
     */
    public static List<Channel> getMyOtherChannels(String userId, String channelId) {
        List<Channel> channels = getMultiChannels(userId);
        if (channels == null || channels.isEmpty()) {
            return null;
        }

        List<Channel> myOtherChannels = new ArrayList<>();
        for (Channel tempChannel : channels) {
            if (!tempChannel.id().asLongText().equals(channelId)) {
                myOtherChannels.add(tempChannel);
            }
        }
        return myOtherChannels;
    }

    /**
     * 输出当前的多端会话情况(用于测试)
     */
    public static void outputMulti() {

        System.out.println("++++++++++++++++++");

        for (ConcurrentHashMap.Entry<String, List<Channel>> entry : multiSession.entrySet()) {
            System.out.println("----------");

            System.out.println("UserId: " + entry.getKey());
            List<Channel> temp = entry.getValue();
            for (Channel c : temp) {
                System.out.println("\t\t ChannelId: " + c.id().asLongText());
            }

            System.out.println("----------");
        }
        System.out.println("++++++++++++++++++");
    }

    /**
     * 发送消息到消息接收方的channel列表
     */
    public static void sendToTarget(List<Channel> receiverChannels, DataContent dataContent) {
        sendMessageToChannelList(receiverChannels,dataContent);
    }
    /**
     * 发送消息到消息发送方的其他设备的channel列表
     */
    public static void sendToMyOthers(List<Channel> myOtherChannels, DataContent dataContent) {
        sendMessageToChannelList(myOtherChannels, dataContent);
    }

    private static void sendMessageToChannelList(List<Channel> channelList, DataContent dataContent){
        ChannelGroup clients = ChatHandler.clients;

        if (channelList == null || channelList.isEmpty()) {
            return;
        }

        for (Channel c : channelList) {
            Channel findChannel = clients.find(c.id());
            if (findChannel != null) {
                findChannel.writeAndFlush(
                        new TextWebSocketFrame(
                                JsonUtils.objectToJson(dataContent)));
            }
        }
    }
}
