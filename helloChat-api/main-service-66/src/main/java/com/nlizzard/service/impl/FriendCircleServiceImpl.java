package com.nlizzard.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nlizzard.base.BaseInfoProperties;
import com.nlizzard.mapper.FriendCircleMapper;
import com.nlizzard.pojo.FriendCircle;
import com.nlizzard.pojo.bo.FriendCircleBO;
import com.nlizzard.pojo.vo.FriendCircleVO;
import com.nlizzard.service.FriendCircleService;
import com.nlizzard.utils.PagedGridResult;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

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
}
