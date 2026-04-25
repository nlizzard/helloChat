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
}
