package com.comp5348.storeapp.model;

public enum OrderStatus {
    PENDING,     // 订单已创建，等待处理
    PROCESSING,  // 处理中（库存或支付未完成）
    COMPLETED,   // 订单完成
    FAILED,      // 订单失败（库存不足或支付失败）
    CANCELLED    // 订单已取消
}
