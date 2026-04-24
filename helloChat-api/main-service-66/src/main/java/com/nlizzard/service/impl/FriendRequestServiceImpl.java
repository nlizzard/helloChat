package com.nlizzard.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nlizzard.base.BaseInfoProperties;
import com.nlizzard.enums.FriendRequestVerifyStatus;
import com.nlizzard.mapper.FriendRequestMapper;
import com.nlizzard.pojo.FriendRequest;
import com.nlizzard.pojo.bo.NewFriendRequestBO;
import com.nlizzard.pojo.vo.NewFriendsVO;
import com.nlizzard.service.FriendRequestService;
import com.nlizzard.utils.PagedGridResult;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class FriendRequestServiceImpl extends BaseInfoProperties implements FriendRequestService {

    private final FriendRequestMapper friendRequestMapper;

    // 添加好友请求
    @Override
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
}
