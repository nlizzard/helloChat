package com.nlizzard.rabbitmq;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 的配置类
 */
@Configuration
public class RabbitMQTestConfig {

    // 定义交换机的名字
    public static final String HELLOCHAT_EXCHANGE = "helloChat_exchange";

    // 定义队列的名字
    public static final String HELLOCHAT_QUEUE = "helloChat_queue";

    // 定义路由键
    public static final String ROUTING_KEY_HELLOCHAT_MSG_SEND = "helloChat.msg.send";

    // 创建交换机
    @Bean(HELLOCHAT_EXCHANGE)
    public Exchange exchange() {
        return ExchangeBuilder.topicExchange(HELLOCHAT_EXCHANGE).durable(true).build();
    }

    // 创建队列
    @Bean(HELLOCHAT_QUEUE)
    public Queue queue() {
        return QueueBuilder.durable(HELLOCHAT_QUEUE).build();
    }

    // 定义队列绑定到交换机的关系
    @Bean
    public Binding binding(@Qualifier(HELLOCHAT_EXCHANGE) Exchange exchange,
                           @Qualifier(HELLOCHAT_QUEUE) Queue queue) {
        return BindingBuilder
                .bind(queue)
                .to(exchange)
                .with("helloChat.#")
                .noargs();  // 执行绑定关系
    }

}

