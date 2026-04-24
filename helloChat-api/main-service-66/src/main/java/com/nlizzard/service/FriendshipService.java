package com.nlizzard.service;

import com.nlizzard.enums.YesOrNo;
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

    /**
     * 修改我的好友的备注名
     * @param myId 我的ID
     * @param friendId 好友ID
     * @param friendRemark 好友备注名
     */
    void updateFriendRemark(String myId,String friendId,String friendRemark);

    /**
     * 拉黑或者恢复好友
     * @param myId 我的ID
     * @param friendId 好友ID
     * @param yesOrNo 表示是否拉黑，YesOrNo.YES表示拉黑，YesOrNo.NO表示恢复好友关系
     */
    void updateBlackList(String myId,
                                String friendId,
                                YesOrNo yesOrNo);

    /**
     * 判断两个朋友之前的关系是否拉黑
     * @param friendId1st 朋友1的ID
     * @param friendId2nd 朋友2的ID
     */
    boolean isBlackEachOther(String friendId1st, String friendId2nd);
}
