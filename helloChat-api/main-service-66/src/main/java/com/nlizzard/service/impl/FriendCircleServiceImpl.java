package com.nlizzard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.nlizzard.base.BaseInfoProperties;
import com.nlizzard.mapper.CommentMapper;
import com.nlizzard.mapper.FriendCircleLikedMapper;
import com.nlizzard.mapper.FriendCircleMapper;
import com.nlizzard.pojo.Comment;
import com.nlizzard.pojo.FriendCircle;
import com.nlizzard.pojo.FriendCircleLiked;
import com.nlizzard.pojo.Users;
import com.nlizzard.pojo.bo.FriendCircleBO;
import com.nlizzard.pojo.vo.FriendCircleVO;
import com.nlizzard.service.CommentService;
import com.nlizzard.service.FriendCircleService;
import com.nlizzard.service.UsersService;
import com.nlizzard.utils.PagedGridResult;
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
public class FriendCircleServiceImpl extends BaseInfoProperties implements FriendCircleService {

    private final FriendCircleMapper friendCircleMapper;

    private final UsersService usersService;

    private final FriendCircleLikedMapper friendCircleLikedMapper;

    private final CommentMapper commentMapper;

    // 发布朋友圈
    @Transactional
    @Override
    public void publish(FriendCircleBO friendCircleBO) {

        FriendCircle pendingFriendCircle = new FriendCircle();

        BeanUtils.copyProperties(friendCircleBO, pendingFriendCircle);

        friendCircleMapper.insert(pendingFriendCircle);
    }

    // 分页查询朋友圈图文列表
    @Override
    public PagedGridResult queryList(String userId,
                                     Integer page,
                                     Integer pageSize) {

        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);

        // 设置分页参数
        Page<FriendCircleVO> pageInfo = new Page<>(page, pageSize);
        friendCircleMapper.queryFriendCircleList(pageInfo, map);

        return setterPagedGridPlus(pageInfo);
    }

    // 点赞/取消点赞朋友圈
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void toggleLike(String friendCircleId, String userId, String tag) {

        // 1.先查是否存在点赞记录
        LambdaQueryWrapper<FriendCircleLiked> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FriendCircleLiked::getFriendCircleId, friendCircleId)
                    .eq(FriendCircleLiked::getLikedUserId, userId);

        FriendCircleLiked friendCircleLiked = friendCircleLikedMapper.selectOne(wrapper);


        // 2.1 取消点赞
        if("unlike".equals(tag)){

            // 从数据库中删除点赞关系
            if(friendCircleLiked != null) friendCircleLikedMapper.delete(wrapper);

            // 可做业务统计
            // 取消点赞过后，朋友圈的对应点赞数累减1
            //redis.decrement(REDIS_FRIEND_CIRCLE_LIKED_COUNTS + ":" + friendCircleId, 1);
            // 删除标记的那个用户点赞过的朋友圈
            //redis.del(REDIS_DOES_USER_LIKE_FRIEND_CIRCLE + ":" + friendCircleId + ":" + userId);

            return ;
        }

        // 2.2 点赞，更新点赞记录表，点赞人昵称，朋友圈图文所属人需要从users，friendCircle表中查询
        // 如果已经点赞过了，就不再重复点赞了
        if(friendCircleLiked != null) return ;

        // 根据朋友圈的主键ID查询归属人(发布人)
        FriendCircle friendCircle = this.selectFriendCircle(friendCircleId);
        // 朋友圈不存在（已删除或伪造 id）时不点赞，避免 friendCircle.getUserId() NPE
        if (friendCircle == null) {
            return;
        }

        // 根据用户主键ID查询点赞人
        Users users = usersService.getById(userId);
        // 点赞人不存在时不点赞，避免 users.getNickname() NPE
        if (users == null) {
            return;
        }

        FriendCircleLiked circleLiked = new FriendCircleLiked();
        circleLiked.setFriendCircleId(friendCircleId);
        circleLiked.setBelongUserId(friendCircle.getUserId());
        circleLiked.setLikedUserId(userId);
        circleLiked.setLikedUserName(users.getNickname());
        circleLiked.setCreatedTime(LocalDateTime.now());

        friendCircleLikedMapper.insert(circleLiked);

        // 可做业务统计
        // 点赞过后，朋友圈的对应点赞数累加1
        //redis.increment(REDIS_FRIEND_CIRCLE_LIKED_COUNTS + ":" + friendCircleId, 1);
        // 标记哪个用户点赞过该朋友圈
        //redis.setnx(REDIS_DOES_USER_LIKE_FRIEND_CIRCLE + ":" + friendCircleId + ":" + userId, userId);

    }

    // 根据朋友圈ID查询朋友圈信息
    private FriendCircle selectFriendCircle(String friendCircleId) {

        return friendCircleMapper.selectById(friendCircleId);
    }

    // 查询朋友圈的点赞列表
    @Override
    public List<FriendCircleLiked> queryLikedFriends(String friendCircleId) {
        LambdaQueryWrapper<FriendCircleLiked> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FriendCircleLiked::getFriendCircleId, friendCircleId);

        return friendCircleLikedMapper.selectList(queryWrapper);
    }

    // 判断当前用户是否点赞过朋友圈
    @Override
    public boolean isLike(String friendCircleId, String userId) {
        LambdaQueryWrapper<FriendCircleLiked> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FriendCircleLiked::getFriendCircleId, friendCircleId)
                    .eq(FriendCircleLiked::getLikedUserId, userId);

        FriendCircleLiked friendCircleLiked = friendCircleLikedMapper.selectOne(queryWrapper);

        return friendCircleLiked != null;
    }

    // 删除朋友圈
    @Transactional
    @Override
    public void delete(String friendCircleId, String userId) {

        // 先校验是否本人的朋友圈，避免越权删除他人朋友圈下的评论与点赞
        LambdaQueryWrapper<FriendCircle> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(FriendCircle::getId, friendCircleId)
                     .eq(FriendCircle::getUserId, userId);
        long ownCount = friendCircleMapper.selectCount(deleteWrapper);
        if (ownCount <= 0) {
            // 非本人朋友圈，拒绝删除（避免清空他人朋友圈的评论与点赞）
            return;
        }
        // 删除朋友圈记录
        friendCircleMapper.delete(deleteWrapper);

        // 删除相关评论记录（孤儿数据）
        LambdaQueryWrapper<Comment> commentWrapper = new LambdaQueryWrapper<>();
        commentWrapper.eq(Comment::getFriendCircleId, friendCircleId);
        commentMapper.delete(commentWrapper);

        // 删除相关点赞记录（孤儿数据）
        LambdaQueryWrapper<FriendCircleLiked> likedWrapper = new LambdaQueryWrapper<>();
        likedWrapper.eq(FriendCircleLiked::getFriendCircleId, friendCircleId);
        friendCircleLikedMapper.delete(likedWrapper);

    }
}
