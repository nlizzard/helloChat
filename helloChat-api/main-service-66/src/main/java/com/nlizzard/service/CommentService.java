package com.nlizzard.service;

import com.nlizzard.pojo.vo.CommentVO;

import java.util.List;

public interface CommentService{

    /**
     * 查询朋友圈的评论列表
     * @param friendCircleId 朋友圈ID
     * @return 评论列表
     */
    List<CommentVO> queryAll(String friendCircleId);
}
