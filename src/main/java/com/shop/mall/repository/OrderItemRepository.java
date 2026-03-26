package com.shop.mall.repository;

import com.shop.mall.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query("SELECT oi.product.id, oi.productName, SUM(oi.quantity), SUM(oi.price * oi.quantity) " +
           "FROM OrderItem oi " +
           "WHERE oi.order.status NOT IN (com.shop.mall.enums.OrderStatus.CANCELLED, com.shop.mall.enums.OrderStatus.REFUNDED) " +
           "GROUP BY oi.product.id, oi.productName " +
           "ORDER BY SUM(oi.quantity) DESC")
    List<Object[]> findBestSellingProducts();
}
