package com.comp5348.warehouse.service;

import com.comp5348.warehouse.model.OrderAllocation;
import com.comp5348.warehouse.model.WarehouseProduct;
import com.comp5348.warehouse.repository.WarehouseProductRepository;
import com.comp5348.warehouse.repository.OrderAllocationRepository;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Log
@Service
public class WarehouseService {

    private final WarehouseProductRepository warehouseProductRepository;
    private final OrderAllocationRepository orderAllocationRepository;

    @Autowired
    public WarehouseService(WarehouseProductRepository warehouseProductRepository, OrderAllocationRepository orderAllocationRepository) {
        this.warehouseProductRepository = warehouseProductRepository;
        this.orderAllocationRepository = orderAllocationRepository;
    }

    // 获取指定产品的总库存
    public int getTotalStock(Long productId) {
        List<WarehouseProduct> productsInWarehouses = warehouseProductRepository.findByProductId(productId);
        return productsInWarehouses.stream()
                .mapToInt(WarehouseProduct::getStockLevels)
                .sum();
    }

    // 更新指定产品的库存
    @Transactional
    public boolean updateProductStock(Long productId, int quantityChange) {
        List<WarehouseProduct> productsInWarehouses = warehouseProductRepository.findByProductId(productId);

        if (productsInWarehouses.isEmpty()) {
            return false;
        }

        // 简单地将库存变化平均分配到所有仓库（仅为示例）
        int warehousesCount = productsInWarehouses.size();
        int quantityPerWarehouse = quantityChange / warehousesCount;
        int remainder = quantityChange % warehousesCount;

        for (WarehouseProduct product : productsInWarehouses)
        {
            int newStockLevel = product.getStockLevels() + quantityPerWarehouse + (remainder > 0 ? 1 : 0);
            remainder--;

            if (newStockLevel < 0) {
                return false; // 库存不能为负数
            }

            product.setStockLevels(newStockLevel);
            warehouseProductRepository.save(product);
        }

        return true;
    }

    // 获取所有产品库存详情
    public List<WarehouseProduct> getAllProductStocks() {
        return warehouseProductRepository.findAll();
    }

    // 原有的库存检查方法
    public boolean isStockAvailable(Long productId, int requiredQuantity) {
        int totalQuantity = getTotalStock(productId);
        return totalQuantity >= requiredQuantity;
    }

    // 分配库存（扣减库存）
    @Transactional
    public boolean allocateStock(Long orderId, Long productId, int requiredQuantity) {
        // 获取所有库存记录
        List<WarehouseProduct> productsInWarehouses =
                warehouseProductRepository.findByProductId(productId);

        // 按库存数量降序排序
        productsInWarehouses.sort((p1, p2) -> Integer.compare(p2.getStockLevels(), p1.getStockLevels()));

        int remainingQuantity = requiredQuantity;

        for (WarehouseProduct product : productsInWarehouses) {
            if (remainingQuantity <= 0) break;

            int availableQuantity = product.getStockLevels();
            int deductedQuantity = Math.min(availableQuantity, remainingQuantity);

            // 扣减库存并保存
            product.setStockLevels(availableQuantity - deductedQuantity);
            warehouseProductRepository.save(product);

            // 创建订单分配记录
            OrderAllocation allocation = new OrderAllocation(orderId, productId, product.getWarehouse().getId(), deductedQuantity);
            orderAllocationRepository.save(allocation);

            remainingQuantity -= deductedQuantity;
        }

        // 返回是否成功扣减所有库存
        return remainingQuantity <= 0;
    }

    @Transactional
    public void releaseStock(Long orderId) {
        List<OrderAllocation> allocations = orderAllocationRepository.findByOrderId(orderId);
        for (OrderAllocation allocation : allocations) {
            // 查找对应的库存记录
            WarehouseProduct warehouseProduct = warehouseProductRepository
                    .findByWarehouseIdAndProductId(allocation.getWarehouseId(), allocation.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Inventory not found for warehouseId=" + allocation.getWarehouseId() + " and productId=" + allocation.getWarehouseId()));

            warehouseProduct.setStockLevels(warehouseProduct.getStockLevels() + allocation.getAllocatedQuantity());
            warehouseProductRepository.save(warehouseProduct);
            // 删除订单分配记录
            orderAllocationRepository.delete(allocation);
            log.info("Increased stock for Product ID: " + allocation.getProductId() +
                    " in Warehouse ID: " + allocation.getWarehouseId() +
                    " by " + allocation.getAllocatedQuantity());
        }
    }

}
