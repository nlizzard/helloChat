package com.nlizzard.service;

import com.nlizzard.pojo.netty.ChatMsg;
import com.nlizzard.utils.PagedGridResult;
import jakarta.validation.constraints.NotBlank;

public interface ChatMessageService {

    /**
     * 保存聊天信息
     * @param chatMsg 聊天信息对象
     */
    void saveMsg(ChatMsg chatMsg);

    /**
     * 查询聊天信息列表
     * @param senderId 发送方id
     * @param receiverId 接收方id
     * @param page 页码
     * @param pageSize 每页条数
     * @return 分页结果对象
     */
    PagedGridResult queryChatMsgList(String senderId,
                                            String receiverId,
                                            Integer page,
                                            Integer pageSize);

    /**
     * 修改语音消息为已读
     * @param msgId 消息id
     */
    void updateMsgSignRead(String msgId);
}
