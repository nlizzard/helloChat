package com.nlizzard.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nlizzard.pojo.FriendRequest;
import com.nlizzard.pojo.vo.NewFriendsVO;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

public interface FriendRequestMapper extends BaseMapper<FriendRequest> {

    /**
     * 查询好友请求列表
     * @param page 分页对象
     * @param map 查询参数
     * @return 好友请求列表
     */
    Page<NewFriendsVO> queryNewFriendList(@Param("page") Page<NewFriendsVO> page,
                                          @Param("paramMap") Map<String, Object> map);

}
