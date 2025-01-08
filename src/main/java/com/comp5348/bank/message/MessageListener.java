package com.comp5348.bank.message;

import com.comp5348.bank.config.RabbitMQConfig;
import com.comp5348.bank.dto.TransactionRecordDTO;
import com.comp5348.bank.errors.PaymentFailedException;
import com.comp5348.bank.service.TransactionRecordService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.java.Log;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.util.Map;

@Log
@Component
public class MessageListener {

    private final TransactionRecordService transactionRecordService;
    private final MessageSender messageSender;
    private final ObjectMapper objectMapper;

    public MessageListener(TransactionRecordService transactionRecordService, MessageSender messageSender) {
        this.transactionRecordService = transactionRecordService;
        this.messageSender = messageSender;
        this.objectMapper = new ObjectMapper();
    }

    // 添加 @Retryable 实现重试机制
    @RabbitListener(queues = RabbitMQConfig.PAYMENT_REQUEST_QUEUE)
    @Retryable(
            value = {PaymentFailedException.class}, // 重试的异常类型
            maxAttempts = 3, // 最大重试次数
            backoff = @Backoff(delay = 5000) // 每次重试之间延迟5秒
    )
    public void handlePaymentRequest(String message) throws PaymentFailedException {
        Long orderId = null;
        boolean isSuccess = false;

        try {
            // 解析消息内容
            Map<String, Object> request = objectMapper.readValue(message, Map.class);
            orderId = Long.valueOf(request.get("orderId").toString());
            Long fromCustomerId = Long.valueOf(request.get("fromCustomerId").toString());
            Long fromAccountId = Long.valueOf(request.get("fromAccountId").toString());
            Long toCustomerId = Long.valueOf(request.get("toCustomerId").toString());
            Long toAccountId = Long.valueOf(request.get("toAccountId").toString());
            Double amount = Double.valueOf(request.get("amount").toString());
            String memo = request.getOrDefault("memo", "").toString();

            log.info("Received payment request for Order ID: " + orderId);

            // 模拟5%的失败率
            if (!simulatePayment()) {
                throw new PaymentFailedException("Simulated payment failure");
            }

            // 执行交易
            TransactionRecordDTO transactionRecordDTO = transactionRecordService.performTransaction(
                    fromCustomerId, fromAccountId,
                    toCustomerId, toAccountId,
                    amount, memo
            );

            // 交易成功
            isSuccess = true;
            log.info(String.format("Payment processing completed for orderId=%d with status=%s",
                    orderId, "SUCCESS"));

        } catch (Exception e) {
            log.severe("Failed to handle payment request: " + e.getMessage());
            // 交易失败
            isSuccess = false;
            throw new PaymentFailedException(e.getMessage()); // 抛出异常触发重试
        } finally {
            // 无论成功与否，都发送支付响应
            if (orderId != null) {
                messageSender.sendPaymentResponse(orderId, isSuccess);
            } else {
                log.severe("Order ID is null, cannot send payment response");
            }
        }
    }

    // 模拟5%失败率的支付逻辑
    private boolean simulatePayment() {
        return Math.random() >= 0.05; // 95% 成功率，5% 失败率
    }

    // 当重试次数超过最大次数时调用此方法
    @Recover
    public void recover(PaymentFailedException e, String message) {
        try {
            Map<String, Object> request = objectMapper.readValue(message, Map.class);
            Long orderId = Long.valueOf(request.get("orderId").toString());
            log.severe("Payment failed after retries for order ID: " + orderId + ", Error: " + e.getMessage());

            // 发送支付失败响应
            messageSender.sendPaymentResponse(orderId, false);

        } catch (Exception ex) {
            log.severe("Failed to handle recovery: " + ex.getMessage());
        }
    }

    @RabbitListener(queues = RabbitMQConfig.REFUND_REQUEST_QUEUE)
    public void handleRefundRequest(String message) {
        Long orderId = null;
        boolean isSuccess = false;
        try {
            Map<String, Object> request = objectMapper.readValue(message, Map.class);
            orderId = Long.valueOf(request.get("orderId").toString());
            Long fromCustomerId = Long.valueOf(request.get("fromCustomerId").toString());
            Long fromAccountId = Long.valueOf(request.get("fromAccountId").toString());
            Long toCustomerId = Long.valueOf(request.get("toCustomerId").toString());
            Long toAccountId = Long.valueOf(request.get("toAccountId").toString());
            Double amount = Double.valueOf(request.get("amount").toString());
            String memo = request.getOrDefault("memo", "").toString();

            log.info("Received refund request for Order ID: " + orderId);

            // 处理退款
            TransactionRecordDTO transactionRecordDTO = transactionRecordService.performTransaction(
                    fromCustomerId, fromAccountId,
                    toCustomerId, toAccountId,
                    amount, memo);

            // 如果没有抛出异常，退款成功
            isSuccess = true;

            log.info(String.format("Refund processing completed for orderId=%d with status=%s",
                    orderId, isSuccess ? "SUCCESS" : "FAILED"));

        } catch (Exception e) {
            log.severe("Failed to handle refund request: " + e.getMessage());
            // 退款失败
            isSuccess = false;
        } finally {
            // 无论成功与否，都发送退款响应
            if (orderId != null) {
                messageSender.sendRefundResponse(orderId, isSuccess);
            } else {
                log.severe("Order ID is null, cannot send refund response");
            }
        }
    }
}
