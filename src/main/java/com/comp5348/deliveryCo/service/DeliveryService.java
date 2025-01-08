package com.comp5348.deliveryCo.service;


import com.comp5348.deliveryCo.message.MessageSender;
import com.comp5348.deliveryCo.model.Delivery;
import com.comp5348.deliveryCo.model.DeliveryStatus;
import com.comp5348.deliveryCo.repository.DeliveryRepository;
import lombok.extern.java.Log;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log
@Service
public class DeliveryService {

    private final MessageSender messageSender;
    private final DeliveryRepository deliveryRepository;

    public DeliveryService(MessageSender messageSender, DeliveryRepository deliveryRepository) {
        this.messageSender = messageSender;
        this.deliveryRepository = deliveryRepository;
    }


    // 提供查询送货信息的方法
    public Delivery getDeliveryByOrderId(Long orderId) {
        return deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Delivery not found for orderId=" + orderId));
    }

    // 提供更新送货状态的方法
    @Transactional
    public void updateDeliveryStatus(Long orderId, String status) {
        Delivery delivery = getDeliveryByOrderId(orderId);
        delivery.setStatus(DeliveryStatus.valueOf(status));
        deliveryRepository.save(delivery);
        messageSender.sendDeliveryStatusUpdate(orderId, status);
    }
}

