package com.nlizzard.service;

import com.nlizzard.pojo.bo.FriendCircleBO;

public interface FriendCircleService{

    /**
     * 发布朋友圈
     * @param friendCircleBO 朋友圈信息
     */
    void publish(FriendCircleBO friendCircleBO);
}
