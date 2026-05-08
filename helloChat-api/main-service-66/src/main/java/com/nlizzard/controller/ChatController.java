package com.nlizzard.controller;

import com.nlizzard.base.BaseInfoProperties;
import com.nlizzard.grace.result.GraceJSONResult;
import com.nlizzard.service.ChatMessageService;
import com.nlizzard.utils.PagedGridResult;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("chat")
@Validated
@RequiredArgsConstructor
public class ChatController extends BaseInfoProperties {

    private final ChatMessageService chatMessageService;

    /**
     * 获取我的未读消息数量
     */
    @PostMapping("getMyUnReadCounts")
    public GraceJSONResult getMyUnReadCounts(@NotBlank(message="用户id不能为空") String myId) {
        Map<Object, Object> map = redis.hgetall(CHAT_MSG_LIST + ":" + myId);
        return GraceJSONResult.ok(map);
    }

    /**
     * 清空我的未读消息数量
     */
    @PostMapping("clearMyUnReadCounts")
    public GraceJSONResult clearMyUnReadCounts(@NotBlank(message="用户id不能为空") String myId,
                                               @NotBlank(message="消息发送方的id不能为空") String oppositeId) {
        redis.setHashValue(CHAT_MSG_LIST + ":" + myId, oppositeId, "0");
        return GraceJSONResult.ok();
    }

    /**
     * 获取我的聊天消息列表
     */
    @PostMapping("list/{senderId}/{receiverId}")
    public GraceJSONResult list(@PathVariable @NotBlank(message="发送方id不能为空") String senderId,
                                @PathVariable @NotBlank(message="接收方id不能为空") String receiverId,
                                Integer page,
                                Integer pageSize) {

        if (page == null) page = 1;
        if (pageSize == null) pageSize = 20;

        PagedGridResult gridResult = chatMessageService.queryChatMsgList(
                senderId,
                receiverId,
                page,
                pageSize);
        return GraceJSONResult.ok(gridResult);
    }
}
