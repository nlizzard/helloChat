package com.nlizzard.rabbitmq;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nlizzard.pojo.netty.ChatMsg;
import com.nlizzard.service.ChatMessageService;
import com.nlizzard.utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class RabbitMQConsumer {

    private final ChatMessageService chatMessageService;

    @RabbitListener(queues = {RabbitMQTestConfig.HELLOCHAT_QUEUE})
    public void watchQueue(String payload, Message message) throws JsonProcessingException {
        String routingKey = message.getMessageProperties().getReceivedRoutingKey();
        log.info("routingKey = " + routingKey);

        if (RabbitMQTestConfig.ROUTING_KEY_HELLOCHAT_MSG_SEND.equals(routingKey)) {
            String msg = payload;
            ChatMsg chatMsg = JsonUtils.jsonToPojo(msg, ChatMsg.class);

            chatMessageService.saveMsg(chatMsg);
        }

    }

}

