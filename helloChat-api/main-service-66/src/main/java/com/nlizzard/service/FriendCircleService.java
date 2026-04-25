package com.nlizzard.service;

import com.nlizzard.pojo.bo.FriendCircleBO;
import com.nlizzard.utils.PagedGridResult;

public interface FriendCircleService{

    /**
     * 发布朋友圈
     * @param friendCircleBO 朋友圈信息
     */
    void publish(FriendCircleBO friendCircleBO);

    /**
     * 分页查询朋友圈图文列表
     * @param userId 用户ID
     * @param page 页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    PagedGridResult queryList(String userId,Integer page,Integer pageSize);

    /**
     * 点赞/取消点赞朋友圈
     * @param friendCircleId 朋友圈ID
     * @param userId 用户ID
     * @param tag 点赞/取消点赞标识
     */
    void toggleLike(String friendCircleId,String userId,String tag);
}
