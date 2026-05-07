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
    }
}
