package com.nlizzard.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nlizzard.pojo.FriendCircle;
import com.nlizzard.pojo.Users;
import com.nlizzard.pojo.vo.FriendCircleVO;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

public interface FriendCircleMapper extends BaseMapper<FriendCircle> {

    // 查询朋友圈列表
    Page<FriendCircleVO> queryFriendCircleList(
            @Param("page") Page<FriendCircleVO> page,
            @Param("paramMap") Map<String, Object> map);
}
