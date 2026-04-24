package com.nlizzard.service;

import com.nlizzard.pojo.bo.NewFriendRequestBO;
import com.nlizzard.utils.PagedGridResult;

public interface FriendRequestService {

    /**
     * 添加好友请求
     * @param newFriendRequestBO 新的好友请求BO
     */
    void addNewRequest(NewFriendRequestBO newFriendRequestBO);

    /**
     * 查询好友请求列表
     * @param userId 用户ID
     * @param page 页码
     * @param pageSize 每页记录数
     * @return 好友请求列表
     */
    PagedGridResult queryNewFriendList(String userId, Integer page, Integer pageSize);

    /**
     * 通过好友请求
     * @param friendRequestId 好友请求ID，唯一标识一个好友请求记录
     * @param friendRemark 好友备注信息
     */
    void passNewFriend(String friendRequestId, String friendRemark);
}
