package com.comp5348.deliveryCo.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class RabbitMQConfig {

    // 交换机名称
    public static final String ORDER_REQUEST_EXCHANGE = "store.order.request.exchange";
    public static final String ORDER_RESPONSE_EXCHANGE = "store.order.response.exchange";

    // 队列名称
    public static final String DELIVERY_REQUEST_QUEUE = "store.delivery.request.queue";
    public static final String DELIVERY_RESPONSE_QUEUE = "store.delivery.response.queue";

    // 死信队列和交换机
    public static final String DEAD_LETTER_EXCHANGE = "store.dlx.exchange";
    public static final String DEAD_LETTER_QUEUE = "store.deadletter.queue";

    // 路由键
    public static final String DELIVERY_REQUEST_ROUTING_KEY = "delivery.request";
    public static final String DELIVERY_RESPONSE_ROUTING_KEY = "delivery.response";

    // 定义交换机
    @Bean
    public DirectExchange orderRequestExchange() {
        return new DirectExchange(ORDER_REQUEST_EXCHANGE);
    }

    @Bean
    public DirectExchange orderResponseExchange() {
        return new DirectExchange(ORDER_RESPONSE_EXCHANGE);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DEAD_LETTER_EXCHANGE);
    }
    // 定义队列

    // 定义队列，并启用死信队列
    @Bean
    public Queue deliveryRequestQueue() {
        return QueueBuilder.durable(DELIVERY_REQUEST_QUEUE)
                .withArgument("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", "deadletter")
                .withArgument("x-max-length", 10)  // 可选：队列最大消息数
                .withArgument("x-message-ttl", 60000)  // 可选：消息的存活时间（60秒）
                .build();
    }


    @Bean
    public Queue deliveryResponseQueue() {
        return new Queue(DELIVERY_RESPONSE_QUEUE);
    }

    // 定义死信队列
    @Bean
    public Queue deadLetterQueue() {
        return new Queue(DEAD_LETTER_QUEUE, true);
    }

    // 绑定队列到交换机
    @Bean
    public Binding deliveryRequestBinding() {
        return BindingBuilder.bind(deliveryRequestQueue())
                .to(orderRequestExchange())
                .with(DELIVERY_REQUEST_ROUTING_KEY);
    }

    @Bean
    public Binding deliveryResponseBinding() {
        return BindingBuilder.bind(deliveryResponseQueue())
                .to(orderResponseExchange())
                .with(DELIVERY_RESPONSE_ROUTING_KEY);
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with("deadletter");
    }
}
