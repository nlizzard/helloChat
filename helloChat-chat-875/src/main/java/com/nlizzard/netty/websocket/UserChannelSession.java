package com.nlizzard.netty.websocket;

import io.netty.channel.Channel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用于userID和Channel的会话管理
 */
public class UserChannelSession {

    // 用于多端同时接受消息，允许同一个账号在多个设备同时在线，比如iPad、iPhone、Mac等设备同时收到消息
    // key: userId, value: 多个用户的channel
    private static final Map<String, List<Channel>> multiSession = new HashMap<>();

    // 用于记录用户id和客户端channel长id的关联关系
    private static final Map<String, String> userChannelIdRelation = new HashMap<>();

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
     * 当websocket初次open的时候，初始化channel，把channel列表和用户userid关联起来
     */
    public static void putMultiChannels(String userId, Channel channel) {

        List<Channel> channels = getMultiChannels(userId);
        if (channels == null || channels.isEmpty()) {
            channels = new ArrayList<>();
        }
        channels.add(channel);

        multiSession.put(userId, channels);
    }

    /**
     * 根据userId获得channel列表
     */
    public static List<Channel> getMultiChannels(String userId) {
        return multiSession.get(userId);
    }

    /**
     * 移除不再使用的会话
     */
    public static void removeUselessChannels(String userId, String channelId) {

        List<Channel> channels = getMultiChannels(userId);
        if (channels == null || channels.isEmpty()) {
            return;
        }

        for (int i = 0; i < channels.size(); i++) {
            Channel tempChannel = channels.get(i);
            if (tempChannel.id().asLongText().equals(channelId)) {
                channels.remove(i);
                multiSession.put(userId, channels);
                break;
            }
        }
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
        for (int i = 0; i < channels.size(); i++) {
            Channel tempChannel = channels.get(i);
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

        for (Map.Entry<String, List<Channel>> entry : multiSession.entrySet()) {
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
}
