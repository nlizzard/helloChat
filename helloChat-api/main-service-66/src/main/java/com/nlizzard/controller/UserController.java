package com.nlizzard.controller;

import com.nlizzard.base.BaseInfoProperties;
import com.nlizzard.grace.result.GraceJSONResult;
import com.nlizzard.pojo.Users;
import com.nlizzard.pojo.bo.ModifyUserBO;
import com.nlizzard.pojo.vo.UsersVO;
import com.nlizzard.service.UsersService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("userInfo")
@RequiredArgsConstructor
public class UserController extends BaseInfoProperties {

    private final UsersService usersService;

    /**
     * 修改用户信息接口
     * @param modifyUserBO 用户信息修改BO
     * @return GraceJSONResult
     */
    @PostMapping("modify")
    public GraceJSONResult modify(@RequestBody ModifyUserBO modifyUserBO) {
        usersService.modifyUserInfo(modifyUserBO);

        UsersVO usersVO = getUserInfo(modifyUserBO.getUserId(),true);
        return GraceJSONResult.ok(usersVO);
    }

    /**
     * 获取用户信息接口,根据needToken参数判断是否需要生成用户token令牌，存入redis中，分布式会话
     * @param userId 用户ID
     * @return GraceJSONResult
     */
    private UsersVO getUserInfo(String userId,boolean needToken) {
        // 查询最新用户信息
        Users latestUser =usersService.getById(userId);
        UsersVO usersVO = new UsersVO();
        BeanUtils.copyProperties(latestUser, usersVO);

        // 生成用户token令牌，存入redis中，分布式会话
        if(needToken){
            // 设置用户分布式会话，保存用户的token令牌，存储到redis中
            String uToken = TOKEN_USER_PREFIX + SYMBOL_DOT + UUID.randomUUID();
            // 本方式只能限制用户在一台设备进行登录
            redis.set(REDIS_USER_TOKEN + ":" + userId, uToken);   // 设置分布式会话
            // 本方式允许用户在多端多设备进行登录
            //redis.set(REDIS_USER_TOKEN + ":" + uToken, user.getId());   // 设置分布式会话
            usersVO.setUserToken(uToken);
        }
        return usersVO;
    }

    /**
     * 根据用户ID获取用户信息接口
     * @param userId 用户ID
     * @return GraceJSONResult
     */
    @PostMapping("get")
    public GraceJSONResult get(String userId){
        return GraceJSONResult.ok(getUserInfo(userId,false));
    }

}
