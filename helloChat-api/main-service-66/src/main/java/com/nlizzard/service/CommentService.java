package com.nlizzard.service;

import com.nlizzard.pojo.bo.CommentBO;
import com.nlizzard.pojo.vo.CommentVO;

import java.util.List;

public interface CommentService{

    /**
     * 查询朋友圈的评论列表
     * @param friendCircleId 朋友圈ID
     * @return 评论列表
     */
    List<CommentVO> queryAll(String friendCircleId);

    /**
     * 发表朋友圈评论
     * @param commentBO 评论对象
     */
    CommentVO createComment(CommentBO commentBO);

    /**
     * 删除朋友圈的评论
     * @param commentUserId 发表评论的用户ID
     * @param commentId 评论ID
     * @param friendCircleId 朋友圈ID
     */
    void deleteComment(String commentUserId,String commentId,String friendCircleId);
}
