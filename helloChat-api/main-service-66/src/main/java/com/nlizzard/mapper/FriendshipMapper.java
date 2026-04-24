package com.nlizzard.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nlizzard.pojo.Friendship;
import com.nlizzard.pojo.vo.ContactsVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface FriendshipMapper extends BaseMapper<Friendship> {

    List<ContactsVO> queryMyFriends(@Param("paramMap") Map<String, Object> map);
}
