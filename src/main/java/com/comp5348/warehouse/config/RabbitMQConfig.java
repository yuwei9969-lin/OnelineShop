package com.comp5348.warehouse.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // 与 StoreApp 使用相同的交换机、队列和路由键
    public static final String ORDER_REQUEST_EXCHANGE = "store.order.request.exchange";

    public static final String WAREHOUSE_UPDATE_QUEUE = "store.warehouse.update.queue";
    public static final String ORDER_RESPONSE_EXCHANGE = "store.order.response.exchange";
    public static final String WAREHOUSE_REQUEST_QUEUE = "store.warehouse.request.queue";
    public static final String WAREHOUSE_RESPONSE_QUEUE = "store.warehouse.response.queue";
    public static final String WAREHOUSE_REQUEST_ROUTING_KEY = "warehouse.request";
    public static final String WAREHOUSE_RESPONSE_ROUTING_KEY = "warehouse.response";

    public static final String WAREHOUSE_UPDATE_ROUTING_KEY = "warehouse.update";

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
    public Queue warehouseRequestQueue() {
        return new Queue(WAREHOUSE_REQUEST_QUEUE);
    }

    @Bean
    public Queue warehouseUpdateQueue() {
        return new Queue(WAREHOUSE_UPDATE_QUEUE);
    }

    // 定义响应队列
    @Bean
    public Queue warehouseResponseQueue() {
        return new Queue(WAREHOUSE_RESPONSE_QUEUE);
    }

    // 绑定请求队列到请求交换机
    @Bean
    public Binding warehouseRequestBinding() {
        return BindingBuilder.bind(warehouseRequestQueue()).to(orderRequestExchange()).with(WAREHOUSE_REQUEST_ROUTING_KEY);
    }
    // 绑定库存更新请求队列到请求交换机
    @Bean
    public Binding warehouseUpdateBinding() {
        return BindingBuilder.bind(warehouseUpdateQueue())
                .to(orderRequestExchange())
                .with(WAREHOUSE_UPDATE_ROUTING_KEY);
    }


    // 绑定响应队列到响应交换机
    @Bean
    public Binding warehouseResponseBinding() {
        return BindingBuilder.bind(warehouseResponseQueue()).to(orderResponseExchange()).with(WAREHOUSE_RESPONSE_ROUTING_KEY);
    }
}
