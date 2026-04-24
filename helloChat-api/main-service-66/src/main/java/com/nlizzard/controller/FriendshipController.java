package com.nlizzard.controller;

import com.nlizzard.base.BaseInfoProperties;
import com.nlizzard.grace.result.GraceJSONResult;
import com.nlizzard.pojo.Friendship;
import com.nlizzard.service.FriendshipService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
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
    public GraceJSONResult pass(String friendId, HttpServletRequest request) {

        String myId = request.getHeader(HEADER_USER_ID);
        // 获取好友关系
        Friendship friendship = friendshipService.getFriendship(myId, friendId);
        return GraceJSONResult.ok(friendship);
    }
}
