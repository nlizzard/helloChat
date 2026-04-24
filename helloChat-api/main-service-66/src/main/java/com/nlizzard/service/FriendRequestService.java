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
}
