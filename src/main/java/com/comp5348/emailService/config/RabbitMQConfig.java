package com.comp5348.emailService.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // 交换机名称（与 StoreApp 一致）
    public static final String EMAIL_REQUEST_EXCHANGE = "store.email.request.exchange";

    // 队列名称
    public static final String EMAIL_REQUEST_QUEUE = "store.email.request.queue";

    // 路由键
    public static final String EMAIL_REQUEST_ROUTING_KEY = "email.request";

    // 定义电子邮件请求交换机
    @Bean
    public DirectExchange emailRequestExchange() {
        return new DirectExchange(EMAIL_REQUEST_EXCHANGE);
    }

    // 定义电子邮件请求队列
    @Bean
    public Queue emailRequestQueue() {
        return new Queue(EMAIL_REQUEST_QUEUE, true);
    }

    // 绑定电子邮件请求队列到交换机
    @Bean
    public Binding emailRequestBinding() {
        return BindingBuilder.bind(emailRequestQueue())
                .to(emailRequestExchange())
                .with(EMAIL_REQUEST_ROUTING_KEY);
    }
}
