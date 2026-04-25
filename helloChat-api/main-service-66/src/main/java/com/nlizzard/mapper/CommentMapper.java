package com.nlizzard.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nlizzard.pojo.Comment;
import com.nlizzard.pojo.vo.CommentVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface CommentMapper extends BaseMapper<Comment> {

    List<CommentVO> queryFriendCircleComments(@Param("paramMap") Map<String, Object> map);

}
