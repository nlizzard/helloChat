package com.nlizzard.service;

import com.nlizzard.pojo.Friendship;
import com.nlizzard.pojo.vo.ContactsVO;

import java.util.List;

public interface FriendshipService {

    /**
     * 获取好友关系
     * @param myId 我的用户ID
     * @param friendId 好友的用户ID
     * @return 好友关系对象，如果没有关系则返回null
     */
    Friendship getFriendship(String myId, String friendId);

    /**
     * 查询我的好友列表(通讯录)
     * @param myId 我的ID
     * @param needBlack 是否需要查询黑名单中的好友，true表示需要，false表示不需要
     * @return 好友列表，包含好友的基本信息和备注信息等，如果没有好友则返回空列表
     */
    List<ContactsVO> queryMyFriends(String myId, boolean needBlack);
}
