package com.nlizzard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nlizzard.base.BaseInfoProperties;
import com.nlizzard.mapper.FriendshipMapper;
import com.nlizzard.pojo.Friendship;
import com.nlizzard.service.FriendshipService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class FriendshipServiceImpl extends BaseInfoProperties implements FriendshipService {

    private final FriendshipMapper friendshipMapper;
    // 获取好友关系
    @Override
    public Friendship getFriendship(String myId, String friendId) {
        LambdaQueryWrapper<Friendship> friendShipQueryWrapper = new LambdaQueryWrapper<>();
        friendShipQueryWrapper.eq(Friendship::getMyId,myId)
                            .eq(Friendship::getFriendId,friendId);
        return friendshipMapper.selectOne(friendShipQueryWrapper);
    }
}
