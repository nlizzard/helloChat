package com.nlizzard.service;

import com.nlizzard.pojo.Friendship;

public interface FriendshipService {

    /**
     * 获取好友关系
     * @param myId 我的用户ID
     * @param friendId 好友的用户ID
     * @return 好友关系对象，如果没有关系则返回null
     */
    Friendship getFriendship(String myId, String friendId);
}
