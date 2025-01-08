package com.comp5348.storeapp.message;

import com.comp5348.storeapp.config.RabbitMQConfig;
import com.comp5348.storeapp.model.*;
import com.comp5348.storeapp.repository.OrderRepository;
import com.comp5348.storeapp.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.java.Log;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Log
@Component
public class MessageListener {

    private final OrderRepository orderRepository;
    private final ObjectMapper objectMapper;

//    private final MessageSender messageSender;
    private final BankService bankService;
    private final DeliveryCoService deliveryCoService;
    private final EmailService emailService;
    private final OrderService orderService;

    public MessageListener(OrderRepository orderRepository,
                           BankService bankService,
                           DeliveryCoService deliveryCoService,
                           EmailService emailService, OrderService orderService) {
        this.orderRepository = orderRepository;
//        this.messageSender = messageSender;
        this.bankService = bankService;
        this.deliveryCoService = deliveryCoService;
        this.emailService = emailService;
        this.orderService = orderService;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Handle the response from the warehouse service.
     */
    @RabbitListener(queues = RabbitMQConfig.WAREHOUSE_RESPONSE_QUEUE)
    public void handleWarehouseResponse(String message) {
        try {
            Thread.sleep(20000);
            log.info("Received warehouse response: " + message);
            Map<String, Object> response = objectMapper.readValue(message, Map.class);
            Long orderId = Long.valueOf(response.get("orderId").toString());
            boolean isAvailable = Boolean.parseBoolean(response.get("isAvailable").toString());

            Optional<Order> optionalOrder = orderRepository.findById(orderId);
            if (optionalOrder.isPresent()) {
                Order order = optionalOrder.get();
                if (isAvailable) {
                    // Stock is sufficient, update order status to PROCESSING and send a payment request.
                    log.info("Stock available for order ID: " + orderId + ". Updating status to PROCESSING.");
                    order.setOrderStatus(OrderStatus.PROCESSING);
                    orderRepository.save(order);

                    // Send a payment request
                    log.info("Sending payment request for order ID: " + orderId);
                    bankService.sendPaymentRequest(orderId);
                } else {
                    // Stock is insufficient, update order status to FAILED.
                    log.warning("Insufficient stock for order ID: " + orderId + ". Marking order as FAILED.");
                    order.setOrderStatus(OrderStatus.FAILED);
                    orderRepository.save(order);

                    // 发送订单失败通知邮件
                    emailService.sendEmail("OUT_OF_STOCK", order);
                }
            } else {
                log.warning("Order not found: " + orderId);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.severe("Failed to handle warehouse response: " + e.getMessage());
        }
    }

    /**
     * Handle the response from the payment service.
     */
    @RabbitListener(queues = RabbitMQConfig.PAYMENT_RESPONSE_QUEUE)
    public void handlePaymentResponse(String message) {
        try {
            log.info("Received payment response: " + message);
            Map<String, Object> response = objectMapper.readValue(message, Map.class);
            Long orderId = Long.valueOf(response.get("orderId").toString());
            boolean isSuccess = Boolean.parseBoolean(response.get("isSuccess").toString());

            Optional<Order> optionalOrder = orderRepository.findById(orderId);
            if (optionalOrder.isPresent()) {
                Order order = optionalOrder.get();
                if (isSuccess) {
                    // Payment successful, update payment and order status.
                    log.info("Payment successful for order ID: " + orderId + ". Marking order as COMPLETED.");
                    order.setPaymentStatus(PaymentStatus.SUCCESS);
                    order.setOrderStatus(OrderStatus.PROCESSING);
                    orderRepository.save(order);
                    boolean cancelled = orderService.cancelOrder(orderId);
                    if(!cancelled){
                        deliveryCoService.sendDeliveryRequest(orderId);
                    }
                } else {
                    // Payment failed, update order status to FAILED.
                    log.warning("Payment failed for order ID: " + orderId + ". Marking order as FAILED.");
                    order.setPaymentStatus(PaymentStatus.FAILED);
                    order.setOrderStatus(OrderStatus.FAILED);
                    orderRepository.save(order);

//                    messageSender.sendEmailRequest("PAYMENT_FAILED", order);
                    emailService.sendEmail("PAYMENT_FAILED", order);
                }
            } else {
                log.warning("Order not found: " + orderId);
            }
        } catch (Exception e) {
            log.severe("Failed to handle payment response: " + e.getMessage());
        }
    }

    /**
     * Handle the response from the deliveryCo service.
     */
    @RabbitListener(queues = RabbitMQConfig.DELIVERY_RESPONSE_QUEUE)
    public void handleDeliveryStatusUpdate(String message) {
        try {
            Map<String, Object> response = objectMapper.readValue(message, Map.class);
            Long orderId = Long.valueOf(response.get("orderId").toString());
            String deliveryStatus = response.get("deliveryStatus").toString();

            log.info("Received delivery status update for Order ID: " + orderId + ", status: " + deliveryStatus);

            // 更新订单的配送状态
            deliveryCoService.updateDeliveryStatus(orderId, deliveryStatus);
            Optional<Order> optionalOrder = orderRepository.findById(orderId);
            if (optionalOrder.isPresent()) {
                Order order = optionalOrder.get();
                // Send email notification via EmailService
                if (deliveryStatus.equals("PICKED_UP") || deliveryStatus.equals("IN_TRANSIT") || deliveryStatus.equals("DELIVERED")) {
                    emailService.sendEmail("DELIVERY_STATUS_UPDATE", order);
                }
            } else {
                log.warning("Order not found: " + orderId);
            }

        } catch (Exception e) {
            log.severe("Failed to handle delivery status update: " + e.getMessage());
        }
    }

    @RabbitListener(queues = RabbitMQConfig.REFUND_RESPONSE_QUEUE)
    public void handleRefundResponse(String message) {
        try {
            log.info("Received refund response: " + message);
            Map<String, Object> response = objectMapper.readValue(message, Map.class);
            Long orderId = Long.valueOf(response.get("orderId").toString());
            boolean isSuccess = Boolean.parseBoolean(response.get("isSuccess").toString());

            Optional<Order> optionalOrder = orderRepository.findById(orderId);
            if (optionalOrder.isPresent()) {
                Order order = optionalOrder.get();
                if (isSuccess) {
                    // 退款成功，更新订单状态
                    order.setPaymentStatus(PaymentStatus.REFUNDED);
                    orderRepository.save(order);

                    // 发送退款成功的邮件通知
                    emailService.sendEmail("REFUND_SUCCESS", order);
                } else {
                    // 退款失败，记录日志或采取其他措施
                    log.warning("Refund failed for order ID: " + orderId);
                }
            } else {
                log.warning("Order not found: " + orderId);
            }
        } catch (Exception e) {
            log.severe("Failed to handle refund response: " + e.getMessage());
        }
    }
}
