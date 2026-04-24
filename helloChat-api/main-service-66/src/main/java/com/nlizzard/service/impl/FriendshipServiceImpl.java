package com.nlizzard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.nlizzard.base.BaseInfoProperties;
import com.nlizzard.enums.YesOrNo;
import com.nlizzard.mapper.FriendshipMapper;
import com.nlizzard.pojo.Friendship;
import com.nlizzard.pojo.vo.ContactsVO;
import com.nlizzard.service.FriendshipService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    // 查询我的好友列表(通讯录)
    @Override
    public List<ContactsVO> queryMyFriends(String myId, boolean needBlack) {

        Map<String, Object> map = new HashMap<>();
        map.put("myId", myId);
        map.put("needBlack", needBlack);

        return friendshipMapper.queryMyFriends(map);
    }

    // 修改我的好友的备注名
    @Transactional
    @Override
    public void updateFriendRemark(String myId,
                                   String friendId,
                                   String friendRemark) {

        LambdaQueryWrapper<Friendship> updateWrapper = new LambdaQueryWrapper<>();
        updateWrapper.eq(Friendship::getMyId, myId)
                     .eq(Friendship::getFriendId, friendId);

        Friendship friendship = new Friendship();
        friendship.setFriendRemark(friendRemark);
        friendship.setUpdatedTime(LocalDateTime.now());

        friendshipMapper.update(friendship, updateWrapper);
    }

    // 拉黑或者恢复好友
    @Transactional
    @Override
    public void updateBlackList(String myId,String friendId,YesOrNo yesOrNo) {

        LambdaUpdateWrapper<Friendship> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Friendship::getMyId, myId)
                              .eq(Friendship::getFriendId, friendId);

        Friendship friendship = new Friendship();
        friendship.setIsBlack(yesOrNo.type);
        friendship.setUpdatedTime(LocalDateTime.now());

        friendshipMapper.update(friendship, updateWrapper);
    }

    // 判断两个朋友之间的关系是否拉黑
    @Override
    public boolean isBlackEachOther(String friendId1st, String friendId2nd) {


        LambdaQueryWrapper<Friendship> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.eq(Friendship::getMyId, friendId1st)
                    .eq(Friendship::getFriendId, friendId2nd)
                    .eq(Friendship::getIsBlack, YesOrNo.YES.type);

        Friendship friendship1st = friendshipMapper.selectOne(queryWrapper1);

        LambdaQueryWrapper<Friendship> queryWrapper2 = new LambdaQueryWrapper<>();
        queryWrapper2.eq(Friendship::getMyId, friendId2nd)
                .eq(Friendship::getFriendId, friendId1st)
                .eq(Friendship::getIsBlack, YesOrNo.YES.type);

        Friendship friendship2nd = friendshipMapper.selectOne(queryWrapper2);

        return friendship1st != null || friendship2nd != null;
    }

    // 删除好友
    @Transactional
    @Override
    public void delete(String myId, String friendId) {

        LambdaUpdateWrapper<Friendship> deleteWrapper1 = new LambdaUpdateWrapper<>();
        deleteWrapper1.eq(Friendship::getMyId,myId)
                .eq(Friendship::getFriendId,friendId);

        friendshipMapper.delete(deleteWrapper1);

        LambdaUpdateWrapper<Friendship> deleteWrapper2 = new LambdaUpdateWrapper<>();
        deleteWrapper2.eq(Friendship::getMyId,friendId)
                .eq(Friendship::getFriendId,myId);

        friendshipMapper.delete(deleteWrapper2);
    }
}
