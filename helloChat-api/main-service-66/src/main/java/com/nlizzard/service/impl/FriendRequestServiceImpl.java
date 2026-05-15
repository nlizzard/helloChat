package com.nlizzard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nlizzard.base.BaseInfoProperties;
import com.nlizzard.enums.FriendRequestVerifyStatus;
import com.nlizzard.enums.YesOrNo;
import com.nlizzard.exceptions.GraceException;
import com.nlizzard.grace.result.ResponseStatusEnum;
import com.nlizzard.mapper.FriendRequestMapper;
import com.nlizzard.mapper.FriendshipMapper;
import com.nlizzard.pojo.FriendRequest;
import com.nlizzard.pojo.Friendship;
import com.nlizzard.pojo.bo.NewFriendRequestBO;
import com.nlizzard.pojo.vo.NewFriendsVO;
import com.nlizzard.service.FriendRequestService;
import com.nlizzard.utils.PagedGridResult;
import com.nlizzard.utils.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class FriendRequestServiceImpl extends BaseInfoProperties implements FriendRequestService {

    private final FriendRequestMapper friendRequestMapper;

    private final FriendshipMapper friendshipMapper;

    // 添加好友请求
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addNewRequest(NewFriendRequestBO newFriendRequestBO) {
        // 1.删除原先存在的好友请求记录，保持好友请求记录的唯一性
        LambdaUpdateWrapper<FriendRequest> friendRequestUpdateWrapper = new LambdaUpdateWrapper<>();
        friendRequestUpdateWrapper.eq(FriendRequest::getMyId,newFriendRequestBO.getMyId())
                            .eq(FriendRequest::getFriendId,newFriendRequestBO.getFriendId());
        friendRequestMapper.delete(friendRequestUpdateWrapper);

        // 2.添加新的好友请求记录
        FriendRequest friendRequest = new FriendRequest();
        friendRequest.setVerifyStatus(FriendRequestVerifyStatus.WAIT.type);
        friendRequest.setRequestTime(LocalDateTime.now());
        BeanUtils.copyProperties(newFriendRequestBO,friendRequest);

        friendRequestMapper.insert(friendRequest);
    }

    // 查询好友请求列表
    @Override
    public PagedGridResult queryNewFriendList(String userId, Integer page, Integer pageSize) {
        Page<NewFriendsVO> pageInfo = new Page<>(page,pageSize);
        HashMap<String, Object> map = new HashMap<>();
        map.put("mySelfId",userId);
        friendRequestMapper.queryNewFriendList(pageInfo,map);

        return setterPagedGridPlus(pageInfo);
    }


    // 通过好友请求
    @Transactional
    @Override
    public void passNewFriend(String friendRequestId, String friendRemark) {
        // 1.查询好友请求记录，获取双方的用户ID
        FriendRequest friendRequest = getSingle(friendRequestId);

        // 如果好友请求记录不存在，或者好友请求记录的状态不是“等待”，则抛出异常
        if(friendRequest == null || !friendRequest.getVerifyStatus().equals(FriendRequestVerifyStatus.WAIT.type)){
            GraceException.display(ResponseStatusEnum.FAILED);
        }

        String mySelfId = friendRequest.getFriendId();  // 被申请方的用户id
        String myFriendId = friendRequest.getMyId();    // 申请方的用户id

        // 鉴权，只有被申请方才能通过好友请求，所以被申请方的用户id必须和当前登录用户id一致，否则抛出异常
        if(!mySelfId.equals(UserContext.getUserId())){
            GraceException.display(ResponseStatusEnum.NO_AUTH);
        }

        LocalDateTime nowTime = LocalDateTime.now();
        // 2.创建双方的好友关系
        Friendship friendshipSelf = new Friendship();
        friendshipSelf.setMyId(mySelfId);
        friendshipSelf.setFriendId(myFriendId);
        friendshipSelf.setFriendRemark(friendRemark);
        friendshipSelf.setIsBlack(YesOrNo.NO.type);
        friendshipSelf.setIsMsgIgnore(YesOrNo.NO.type);
        friendshipSelf.setCreatedTime(nowTime);
        friendshipSelf.setUpdatedTime(nowTime);

        Friendship friendshipOpposite = new Friendship();
        friendshipOpposite.setMyId(myFriendId);
        friendshipOpposite.setFriendId(mySelfId);
        friendshipOpposite.setFriendRemark(friendRequest.getFriendRemark());
        friendshipOpposite.setIsBlack(YesOrNo.NO.type);
        friendshipOpposite.setIsMsgIgnore(YesOrNo.NO.type);
        friendshipOpposite.setCreatedTime(nowTime);
        friendshipOpposite.setUpdatedTime(nowTime);

        friendshipMapper.insert(friendshipSelf);
        friendshipMapper.insert(friendshipOpposite);

        // A通过B的请求之后，需要把双方的好友请求记录都设置为“通过”
        friendRequest.setVerifyStatus(FriendRequestVerifyStatus.SUCCESS.type);
        friendRequestMapper.updateById(friendRequest);

        // 还有一种情况，A添加B，B没有通过，所以A发出的好友请求过期了；
        // 但是，过期后，B向A发起好友请求，所以B被A通过后，那么两边的请求都应该“通过”
        LambdaUpdateWrapper<FriendRequest> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(FriendRequest::getMyId, myFriendId)
                .eq(FriendRequest::getFriendId, mySelfId);

        FriendRequest requestOpposite = new FriendRequest();
        requestOpposite.setVerifyStatus(FriendRequestVerifyStatus.SUCCESS.type);

        friendRequestMapper.update(requestOpposite, updateWrapper);
    }

    // 根据好友请求ID查询单条好友请求记录
    private FriendRequest getSingle(String friendRequestId) {
        return friendRequestMapper.selectById(friendRequestId);
    }
}
