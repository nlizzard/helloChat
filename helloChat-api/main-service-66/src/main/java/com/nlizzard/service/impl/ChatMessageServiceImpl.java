package com.nlizzard.service.impl;

import com.nlizzard.base.BaseInfoProperties;
import com.nlizzard.mapper.ChatMessageMapper;
import com.nlizzard.pojo.ChatMessage;
import com.nlizzard.pojo.netty.ChatMsg;
import com.nlizzard.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatMessageServiceImpl extends BaseInfoProperties implements ChatMessageService {

    private final ChatMessageMapper chatMessageMapper;

    // 保存聊天消息到数据库中
    @Transactional
    @Override
    public void saveMsg(ChatMsg chatMsg) {

        ChatMessage message = new ChatMessage();
        BeanUtils.copyProperties(chatMsg, message);

        // 手动设置聊天信息的主键id
        message.setId(chatMsg.getMsgId());

        chatMessageMapper.insert(message);

        // 记录未读消息数量，让前端消息界面可以显示未读消息的数量
        String receiverId = chatMsg.getReceiverId();
        String senderId = chatMsg.getSenderId();
        // 通过redis累加信息接受者的对应记录
        redis.incrementHash(CHAT_MSG_LIST + ":" + receiverId, senderId, 1);

    }
}
