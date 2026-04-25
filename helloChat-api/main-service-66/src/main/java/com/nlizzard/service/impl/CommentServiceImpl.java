package com.nlizzard.service.impl;

import com.nlizzard.base.BaseInfoProperties;
import com.nlizzard.mapper.CommentMapper;
import com.nlizzard.pojo.vo.CommentVO;
import com.nlizzard.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl extends BaseInfoProperties implements CommentService {

    private final CommentMapper commentMapper;

    @Override
    public List<CommentVO> queryAll(String friendCircleId) {
        Map<String, Object> map = new HashMap<>();
        map.put("friendCircleId", friendCircleId);

        return commentMapper.queryFriendCircleComments(map);
    }
}
