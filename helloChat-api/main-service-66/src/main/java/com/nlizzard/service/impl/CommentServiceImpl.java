package com.nlizzard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nlizzard.base.BaseInfoProperties;
import com.nlizzard.exceptions.GraceException;
import com.nlizzard.grace.result.GraceJSONResult;
import com.nlizzard.grace.result.ResponseStatusEnum;
import com.nlizzard.mapper.CommentMapper;
import com.nlizzard.mapper.FriendCircleMapper;
import com.nlizzard.pojo.Comment;
import com.nlizzard.pojo.FriendCircle;
import com.nlizzard.pojo.Users;
import com.nlizzard.pojo.bo.CommentBO;
import com.nlizzard.pojo.vo.CommentVO;
import com.nlizzard.service.CommentService;
import com.nlizzard.service.UsersService;
import com.nlizzard.utils.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl extends BaseInfoProperties implements CommentService {

    private final CommentMapper commentMapper;

    private final UsersService usersService;

    private final FriendCircleMapper friendCircleMapper;

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
        // 评论人不存在（伪造/已删除用户）时不填充昵称头像，避免 commentUser.getNickname() NPE
        if (commentUser != null) {
            commentVO.setCommentUserNickname(commentUser.getNickname());
            commentVO.setCommentUserFace(commentUser.getFace());
        }
        commentVO.setCommentId(pendingComment.getId());

        return commentVO;
    }

    // 删除朋友圈的评论(1:删除自己的评论，2:删除别人对自己的朋友圈的评论)
    @Transactional
    @Override
    public void deleteComment(String commentUserId,String commentId,String friendCircleId) {

        String myId = UserContext.getUserId();
        if(Objects.equals(myId,commentUserId)){
            // 删除自己的评论
            LambdaQueryWrapper<Comment> deleteWrapper = new LambdaQueryWrapper<>();
            deleteWrapper.eq(Comment::getId, commentId)
                    .eq(Comment::getCommentUserId, commentUserId)
                    .eq(Comment::getFriendCircleId, friendCircleId);

            commentMapper.delete(deleteWrapper);
            return ;
        }

        // 删除别人对自己的朋友圈的评论
        // 先判断是否是自己的朋友圈
        LambdaQueryWrapper<FriendCircle> friendCircleQueryWrapper = new LambdaQueryWrapper<>();
        friendCircleQueryWrapper.eq(FriendCircle::getId,friendCircleId)
                                .eq(FriendCircle::getUserId,myId);
        FriendCircle friendCircle = friendCircleMapper.selectOne(friendCircleQueryWrapper);

        // 如果查询不到朋友圈，说明不是自己的朋友圈，没有权限删除评论
        if(friendCircle == null){
           GraceException.display(ResponseStatusEnum.NO_AUTH);
        }

        // 删除评论
        LambdaQueryWrapper<Comment> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(Comment::getId, commentId)
                .eq(Comment::getCommentUserId, commentUserId)
                .eq(Comment::getFriendCircleId, friendCircleId);

        commentMapper.delete(deleteWrapper);
    }
}
