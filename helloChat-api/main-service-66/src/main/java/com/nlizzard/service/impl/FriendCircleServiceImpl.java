package com.nlizzard.service.impl;

import com.nlizzard.base.BaseInfoProperties;
import com.nlizzard.mapper.FriendCircleMapper;
import com.nlizzard.pojo.FriendCircle;
import com.nlizzard.pojo.bo.FriendCircleBO;
import com.nlizzard.service.FriendCircleService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FriendCircleServiceImpl extends BaseInfoProperties implements FriendCircleService {

    private final FriendCircleMapper friendCircleMapper;

    // 发布朋友圈
    @Transactional
    @Override
    public void publish(FriendCircleBO friendCircleBO) {

        FriendCircle pendingFriendCircle = new FriendCircle();

        BeanUtils.copyProperties(friendCircleBO, pendingFriendCircle);

        friendCircleMapper.insert(pendingFriendCircle);
    }
}
