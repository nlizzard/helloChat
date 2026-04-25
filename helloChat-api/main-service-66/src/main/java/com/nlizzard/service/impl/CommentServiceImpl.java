package com.nlizzard.service.impl;

import com.nlizzard.base.BaseInfoProperties;
import com.nlizzard.mapper.CommentMapper;
import com.nlizzard.pojo.Comment;
import com.nlizzard.pojo.Users;
import com.nlizzard.pojo.bo.CommentBO;
import com.nlizzard.pojo.vo.CommentVO;
import com.nlizzard.service.CommentService;
import com.nlizzard.service.UsersService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl extends BaseInfoProperties implements CommentService {

    private final CommentMapper commentMapper;

    private final UsersService usersService;

    // 查询朋友圈的评论列表
    @Override
    public List<CommentVO> queryAll(String friendCircleId) {
        Map<String, Object> map = new HashMap<>();
        map.put("friendCircleId", friendCircleId);

        return commentMapper.queryFriendCircleComments(map);
    }

    // 发表朋友圈评论
    @Transactional
    @Override
    public CommentVO createComment(CommentBO commentBO) {


        Comment pendingComment = new Comment();

        BeanUtils.copyProperties(commentBO, pendingComment);
        pendingComment.setCreatedTime(LocalDateTime.now());
        // 新增留言
        commentMapper.insert(pendingComment);

        // 留言后的最新评论数据需要返回给前端（提供前端做的扩展数据）
        CommentVO commentVO = new CommentVO();
        BeanUtils.copyProperties(pendingComment, commentVO);

        Users commentUser = usersService.getById(commentBO.getCommentUserId());
        commentVO.setCommentUserNickname(commentUser.getNickname());
        commentVO.setCommentUserFace(commentUser.getFace());
        commentVO.setCommentId(pendingComment.getId());

        return commentVO;
    }
}
