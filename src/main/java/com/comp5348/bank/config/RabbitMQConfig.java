package com.comp5348.bank.config;


import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // 交换机名称（与 StoreApp 一致）
    public static final String ORDER_REQUEST_EXCHANGE = "store.order.request.exchange";
    public static final String ORDER_RESPONSE_EXCHANGE = "store.order.response.exchange";

    // 队列名称
    public static final String PAYMENT_REQUEST_QUEUE = "store.payment.request.queue";
    public static final String PAYMENT_RESPONSE_QUEUE = "store.payment.response.queue";
    public static final String REFUND_REQUEST_QUEUE = "store.refund.request.queue";
    public static final String REFUND_RESPONSE_QUEUE = "store.refund.response.queue";

    // 路由键
    public static final String PAYMENT_REQUEST_ROUTING_KEY = "payment.request";
    public static final String PAYMENT_RESPONSE_ROUTING_KEY = "payment.response";
    public static final String REFUND_REQUEST_ROUTING_KEY = "refund.request";
    public static final String REFUND_RESPONSE_ROUTING_KEY = "refund.response";

    // 定义请求交换机
    @Bean
    public DirectExchange orderRequestExchange() {
        return new DirectExchange(ORDER_REQUEST_EXCHANGE);
    }

    // 定义响应交换机
    @Bean
    public DirectExchange orderResponseExchange() {
        return new DirectExchange(ORDER_RESPONSE_EXCHANGE);
    }

    // 定义请求队列
    @Bean
    public Queue paymentRequestQueue() {
        return new Queue(PAYMENT_REQUEST_QUEUE);
    }

    @Bean
    public Queue refundRequestQueue() {
        return new Queue(REFUND_REQUEST_QUEUE);
    }

    // 定义响应队列
    @Bean
    public Queue paymentResponseQueue() {
        return new Queue(PAYMENT_RESPONSE_QUEUE);
    }
    @Bean
    public Queue refundResponseQueue() {
        return new Queue(REFUND_RESPONSE_QUEUE);
    }


    // 绑定请求队列到请求交换机
    @Bean
    public Binding paymentRequestBinding() {
        return BindingBuilder.bind(paymentRequestQueue())
                .to(orderRequestExchange())
                .with(PAYMENT_REQUEST_ROUTING_KEY);
    }
    // 绑定退款请求队列到请求交换机
    @Bean
    public Binding refundRequestBinding() {
        return BindingBuilder.bind(refundRequestQueue())
                .to(orderRequestExchange())
                .with(REFUND_REQUEST_ROUTING_KEY);
    }

    // 绑定响应队列到响应交换机
    @Bean
    public Binding paymentResponseBinding() {
        return BindingBuilder.bind(paymentResponseQueue())
                .to(orderResponseExchange())
                .with(PAYMENT_RESPONSE_ROUTING_KEY);
    }

    @Bean
    public Binding refundResponseBinding() {
        return BindingBuilder.bind(refundResponseQueue())
                .to(orderResponseExchange())
                .with(REFUND_RESPONSE_ROUTING_KEY);
    }
}
