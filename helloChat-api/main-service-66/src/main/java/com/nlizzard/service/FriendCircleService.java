package com.nlizzard.service;

import com.nlizzard.pojo.FriendCircleLiked;
import com.nlizzard.pojo.bo.FriendCircleBO;
import com.nlizzard.utils.PagedGridResult;

import java.util.List;

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

    /**
     * 查询朋友圈的点赞列表
     * @param friendCircleId 朋友圈ID
     * @return 点赞列表
     */
    List<FriendCircleLiked> queryLikedFriends(String friendCircleId);

    /**
     * 判断当前用户是否点赞过朋友圈
     * @param friendCircleId 朋友圈ID
     * @param userId 用户ID
     * @return 是否点赞过
     */
    boolean isLike(String friendCircleId, String userId);

    /**
     * 删除朋友圈
     * @param friendCircleId 朋友圈ID
     * @param userId 用户ID
     */
    void delete(String friendCircleId, String userId);
}
