package com.nlizzard.service.impl;

import com.nlizzard.base.BaseInfoProperties;

import com.nlizzard.exceptions.GraceException;
import com.nlizzard.grace.result.ResponseStatusEnum;
import com.nlizzard.mapper.UsersMapper;
import com.nlizzard.pojo.Users;
import com.nlizzard.pojo.bo.ModifyUserBO;
import com.nlizzard.service.UsersService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UsersServiceImpl extends BaseInfoProperties implements UsersService {

    private final UsersMapper usersMapper;

    @Override
    public void modifyUserInfo(ModifyUserBO modifyUserBO) {
        Users pendingUser = new Users();
        String userId = modifyUserBO.getUserId();

        Users dbUser = usersMapper.selectById(userId);
        // 如果用户不存在
        if(dbUser == null) {
            GraceException.display(ResponseStatusEnum.USER_ISNOT_EXIST_ERROR);
        }

        pendingUser.setId(modifyUserBO.getUserId());
        pendingUser.setUpdatedTime(LocalDateTime.now());

        BeanUtils.copyProperties(modifyUserBO,pendingUser);
        // 更新用户信息
        usersMapper.updateById(pendingUser);
    }

    @Override
    public Users getById(String userId) {
        return usersMapper.selectById(userId);
    }
}
