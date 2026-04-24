package com.nlizzard.controller;

import com.nlizzard.base.BaseInfoProperties;
import com.nlizzard.enums.YesOrNo;
import com.nlizzard.grace.result.GraceJSONResult;
import com.nlizzard.pojo.Friendship;
import com.nlizzard.service.FriendshipService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
@Validated
@RequestMapping("friendship")
public class FriendshipController extends BaseInfoProperties {

    private final FriendshipService friendshipService;

    /**
     * 获取好友关系接口
     * @param friendId 好友ID
     * @param request HttpServletRequest对象，用于获取用户ID等信息
     * @return GraceJSONResult对象，包含好友关系信息
     */
    @PostMapping("getFriendship")
    public GraceJSONResult pass(@NotBlank(message = "好友ID不能为空") String friendId, HttpServletRequest request) {

        String myId = request.getHeader(HEADER_USER_ID);
        // 获取好友关系
        Friendship friendship = friendshipService.getFriendship(myId, friendId);
        return GraceJSONResult.ok(friendship);
    }

    /**
     * 查询我的好友列表接口(通讯录功能)
     * @param request HttpServletRequest对象，用于获取用户ID等信息
     * @return GraceJSONResult对象，包含好友列表信息
     */
    @PostMapping("queryMyFriends")
    public GraceJSONResult queryMyFriends(HttpServletRequest request) {

        String myId = request.getHeader(HEADER_USER_ID);

        return GraceJSONResult.ok(friendshipService.queryMyFriends(myId, false));
    }

    /**
     * 更新好友备注接口
     * @param request HttpServletRequest对象，用于获取用户ID等信息
     * @param friendId 好友ID
     * @param friendRemark 好友备注信息
     * @return GraceJSONResult对象，表示操作结果
     */
    @PostMapping("updateFriendRemark")
    public GraceJSONResult updateFriendRemark(HttpServletRequest request,
                                              @NotBlank(message = "好友ID不能为空") String friendId,
                                              String friendRemark) {

        String myId = request.getHeader(HEADER_USER_ID);
        friendshipService.updateFriendRemark(myId, friendId, friendRemark);
        return GraceJSONResult.ok();
    }

    /**
     * 加入黑名单接口
     * @param request HttpServletRequest对象，用于获取用户ID等信息
     * @param friendId 好友ID
     * @return GraceJSONResult对象，表示操作结果
     */
    @PostMapping("tobeBlack")
    public GraceJSONResult tobeBlack(HttpServletRequest request,
                                     String friendId) {

        if (StringUtils.isBlank(friendId)) {
            return GraceJSONResult.error();
        }

        String myId = request.getHeader(HEADER_USER_ID);
        friendshipService.updateBlackList(myId, friendId, YesOrNo.YES);
        return GraceJSONResult.ok();
    }

    /**
     * 移出黑名单接口
     * @param request HttpServletRequest对象，用于获取用户ID等信息
     * @param friendId 好友ID
     * @return GraceJSONResult对象，表示操作结果
     */
    @PostMapping("moveOutBlack")
    public GraceJSONResult moveOutBlack(HttpServletRequest request,
                                        String friendId) {

        if (StringUtils.isBlank(friendId)) {
            return GraceJSONResult.error();
        }

        String myId = request.getHeader(HEADER_USER_ID);
        friendshipService.updateBlackList(myId, friendId, YesOrNo.NO);
        return GraceJSONResult.ok();
    }

    /**
     * 查询我的黑名单列表接口
     * @param request HttpServletRequest对象，用于获取用户ID等信息
     * @return GraceJSONResult对象，包含黑名单列表信息
     */
    @PostMapping("queryMyBlackList")
    public GraceJSONResult queryMyBlackList(HttpServletRequest request) {
        String myId = request.getHeader(HEADER_USER_ID);
        return GraceJSONResult.ok(friendshipService.queryMyFriends(myId, true));
    }

    /**
     * 判断两个朋友之前的关系是否拉黑
     * @param friendId1st 第一个朋友ID
     * @param friendId2nd 第二个朋友ID
     * @return GraceJSONResult对象，存在拉黑关系则返回true，否则返回false
     */
    @GetMapping("isBlack")
    public GraceJSONResult isBlack(String friendId1st, String friendId2nd) {

        // 需要进行两次查询，A拉黑B，B拉黑A，AB相互拉黑
        // 只需要符合其中的一个条件，就表示双发发送消息不可送达
        return GraceJSONResult.ok(
                friendshipService.isBlackEachOther(
                        friendId1st, friendId2nd));
    }

    /**
     * 删除好友接口
     * @param request HttpServletRequest对象，用于获取用户ID等信息
     * @param friendId 好友ID
     * @return GraceJSONResult对象，表示操作结果
     */
    @PostMapping("delete")
    public GraceJSONResult delete(HttpServletRequest request,
                                  @NotBlank(message = "好友ID不能为空") String friendId) {

        String myId = request.getHeader(HEADER_USER_ID);

        friendshipService.delete(myId, friendId);
        return GraceJSONResult.ok();
    }

}
