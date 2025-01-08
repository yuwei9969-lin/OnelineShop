package com.comp5348.deliveryCo.message;


import com.comp5348.deliveryCo.config.RabbitMQConfig;
import com.comp5348.deliveryCo.message.MessageSender;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.java.Log;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import com.rabbitmq.client.Channel;




import java.io.IOException;
import java.util.Map;

@Log
@Component
public class MessageListener {

    private final MessageSender messageSender;
    private final ObjectMapper objectMapper;

    public MessageListener(MessageSender messageSender) {
        this.messageSender = messageSender;
        this.objectMapper = new ObjectMapper();
    }

    @RabbitListener(queues = RabbitMQConfig.DELIVERY_REQUEST_QUEUE)
    public void handleDeliveryRequest(String message, Channel channel, Message mqMessage) throws IOException {
        try {
            // 模拟5%的丢包率
            if (Math.random() < 0.05) {
                throw new RuntimeException("Simulated delivery failure (5% drop rate)");
            }

            // 解析消息
            Map<String, Object> request = objectMapper.readValue(message, Map.class);
            Long orderId = Long.valueOf(request.get("orderId").toString());

            log.info("Received delivery request for Order ID: " + orderId);

            // 发送状态更新：RECEIVED
            messageSender.sendDeliveryStatusUpdate(orderId, "RECEIVED_REQUEST");

            // 模拟配送过程
            Thread.sleep(5000); // 等待 5 秒

            // 发送状态更新：PICKED_UP
            messageSender.sendDeliveryStatusUpdate(orderId, "PICKED_UP");

            Thread.sleep(5000); // 等待 5 秒

            // 发送状态更新：IN_TRANSIT
            messageSender.sendDeliveryStatusUpdate(orderId, "IN_TRANSIT");

            Thread.sleep(5000); // 等待 5 秒

            // 发送状态更新：DELIVERED
            messageSender.sendDeliveryStatusUpdate(orderId, "DELIVERED");

            log.info("Delivery completed for Order ID: " + orderId);

            // 成功处理后发送 ACK 确认
            channel.basicAck(mqMessage.getMessageProperties().getDeliveryTag(), false);
            log.info("Message ACK sent for Order ID: " + orderId);

        } catch (Exception e) {
            log.severe("Failed to handle delivery request: " + e.getMessage());

            // 处理失败时拒绝消息并重新入队，RabbitMQ 会重试投递
            channel.basicNack(mqMessage.getMessageProperties().getDeliveryTag(), false, true);
        }
    }
}

