package com.nlizzard.controller;

import com.nlizzard.base.BaseInfoProperties;
import com.nlizzard.grace.result.GraceJSONResult;
import com.nlizzard.pojo.Friendship;
import com.nlizzard.service.FriendshipService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
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
}
