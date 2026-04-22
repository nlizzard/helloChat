package com.nlizzard.service.impl;

import com.nlizzard.base.BaseInfoProperties;

import com.nlizzard.exceptions.GraceException;
import com.nlizzard.grace.result.ResponseStatusEnum;
import com.nlizzard.mapper.UsersMapper;
import com.nlizzard.pojo.Users;
import com.nlizzard.pojo.bo.ModifyUserBO;
import com.nlizzard.service.UsersService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static com.nlizzard.grace.result.ResponseStatusEnum.USER_ISNOT_EXIST_ERROR;
import static com.nlizzard.grace.result.ResponseStatusEnum.WECHAT_NUM_ALREADY_MODIFIED_ERROR;

@Service
@RequiredArgsConstructor
public class UsersServiceImpl extends BaseInfoProperties implements UsersService {

    private final UsersMapper usersMapper;

    @Override
    public void modifyUserInfo(ModifyUserBO modifyUserBO) {
        Users pendingUser = new Users();
        String userId = modifyUserBO.getUserId();
        String wechatNum = modifyUserBO.getWechatNum();

        Users dbUser = usersMapper.selectById(userId);
        // 如果用户不存在
        if(dbUser == null) {
            GraceException.display(USER_ISNOT_EXIST_ERROR);
        }

        // 判断微信号是否进行修改：微信号需要限定唯一性和年度修改次数
        // 如果微信号不为空，并且用户已经修改过微信号了，则不能再修改了
        if(StringUtils.isNoneBlank(wechatNum) && redis.keyIsExist(REDIS_USER_ALREADY_UPDATE_WECHAT_NUM+":"+userId)){
            GraceException.display(WECHAT_NUM_ALREADY_MODIFIED_ERROR);
        }

        if(StringUtils.isNoneBlank(wechatNum)){
            // 设置用户修改过微信号，存入redis中，限制用户年度内只能修改一次微信号
            redis.setByDays(REDIS_USER_ALREADY_UPDATE_WECHAT_NUM+":"+userId
                            ,userId
                            ,365);
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
