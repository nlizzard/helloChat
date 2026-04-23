package com.nlizzard.controller;

import com.nlizzard.base.BaseInfoProperties;
import com.nlizzard.grace.result.GraceJSONResult;
import com.nlizzard.grace.result.ResponseStatusEnum;
import com.nlizzard.pojo.Users;
import com.nlizzard.pojo.bo.ModifyUserBO;
import com.nlizzard.pojo.vo.UsersVO;
import com.nlizzard.service.UsersService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("userInfo")
@RequiredArgsConstructor
@Validated
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
    public GraceJSONResult get(@RequestParam("userId") @NotBlank(message = "用户ID不能为空") String userId){
        return GraceJSONResult.ok(getUserInfo(userId,false));
    }

    /**
     * 修改用户头像接口
     * @param userId 用户ID
     * @param faceUrl 头像地址
     * @return GraceJSONResult
     */
    @PostMapping("updateFace")
    public GraceJSONResult updateFace(@RequestParam("userId") @NotBlank(message = "用户ID不能为空") String userId,
                                      @RequestParam("faceUrl") @NotBlank(message = "头像地址不能为空") String faceUrl) {
        ModifyUserBO modifyUserBO = new ModifyUserBO();
        modifyUserBO.setUserId(userId);
        modifyUserBO.setFace(faceUrl);
        usersService.modifyUserInfo(modifyUserBO);

        UsersVO usersVO = getUserInfo(userId,true);
        return GraceJSONResult.ok(usersVO);
    }

    /**
     * 修改用户朋友圈背景图接口
     * @param userId 用户ID
     * @param friendCircleBg 朋友圈背景图地址
     * @return GraceJSONResult
     */
    @PostMapping("updateFriendCircleBg")
    public GraceJSONResult updateFriendCircleBg(
            @RequestParam("userId") String userId,
            @RequestParam("friendCircleBg") String friendCircleBg) {

        ModifyUserBO userBO = new ModifyUserBO();
        userBO.setUserId(userId);
        userBO.setFriendCircleBg(friendCircleBg);

        // 修改用户信息
        usersService.modifyUserInfo(userBO);

        // 返回最新用户信息
        UsersVO usersVO = getUserInfo(userBO.getUserId(), true);

        return GraceJSONResult.ok(usersVO);
    }

    /**
     * 修改用户聊天背景图接口
     * @param userId 用户ID
     * @param chatBg 聊天背景图地址
     * @return GraceJSONResult
     */
    @PostMapping("updateChatBg")
    public GraceJSONResult updateChatBg(
                                        @RequestParam("userId") String userId,
                                        @RequestParam("chatBg") String chatBg) {
        ModifyUserBO userBO = new ModifyUserBO();
        userBO.setUserId(userId);
        userBO.setChatBg(chatBg);

        // 修改用户信息
        usersService.modifyUserInfo(userBO);

        // 返回最新用户信息
        UsersVO usersVO = getUserInfo(userBO.getUserId(), true);

        return GraceJSONResult.ok(usersVO);

    }

    /**
     * 搜索好友接口，根据微信号或手机号查询用户信息
     * @param queryString 微信号或手机号
     * @param request HttpServletRequest对象，用于获取当前用户ID
     * @return GraceJSONResult
     */
    @PostMapping("queryFriend")
    public GraceJSONResult queryFriend(@NotBlank(message = "搜索字段不能为空") String queryString,
                                       HttpServletRequest request) {

        Users friend = usersService.getByWechatNumOrMobile(queryString);
        if (friend == null) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.FRIEND_NOT_EXIST_ERROR);
        }

        // 判断，不能添加自己为好友
        String myId = request.getHeader(HEADER_USER_ID);
        if (myId.equals(friend.getId())) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.CAN_NOT_ADD_SELF_FRIEND_ERROR);
        }

        return GraceJSONResult.ok(friend);
    }
}
