package com.nlizzard.service;

import com.nlizzard.grace.result.GraceJSONResult;
import com.nlizzard.pojo.Users;
import com.nlizzard.pojo.bo.ModifyUserBO;
import jakarta.servlet.http.HttpServletRequest;

public interface UsersService {

    /**
     * 修改用户信息
     * @param modifyUserBO 用户信息修改BO
     */
    void modifyUserInfo(ModifyUserBO modifyUserBO);

    /**
     * 获取用户信息
     * @param userId 用户ID
     * @return Users
     */
    Users getById(String userId);

    /**
     * 根据微信号或手机查询用户信息
     * @param queryString 查询字符串，可以是微信号或手机号
     * @return Users
     */
    Users getByWechatNumOrMobile(String queryString);
}
