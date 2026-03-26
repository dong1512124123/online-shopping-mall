package com.shop.mall.repository;

import com.shop.mall.entity.Member;
import com.shop.mall.entity.Order;
import com.shop.mall.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByMemberOrderByCreatedAtDesc(Member member);

    Optional<Order> findByOrderNumber(String orderNumber);

    long countByStatus(OrderStatus status);

    List<Order> findAllByOrderByCreatedAtDesc();

    List<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status);

    @Query("SELECT COALESCE(SUM(o.totalPrice), 0) FROM Order o " +
           "WHERE o.createdAt >= :from AND o.createdAt < :to " +
           "AND o.status NOT IN (com.shop.mall.enums.OrderStatus.CANCELLED, com.shop.mall.enums.OrderStatus.REFUNDED)")
    long sumTotalPriceBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT COUNT(o) FROM Order o " +
           "WHERE o.createdAt >= :from AND o.createdAt < :to " +
           "AND o.status NOT IN (com.shop.mall.enums.OrderStatus.CANCELLED, com.shop.mall.enums.OrderStatus.REFUNDED)")
    long countOrdersBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    /** 특정 연도의 유효 주문 목록 조회 (취소/환불 제외) */
    @Query("SELECT o FROM Order o " +
           "WHERE YEAR(o.createdAt) = :year " +
           "AND o.status NOT IN (com.shop.mall.enums.OrderStatus.CANCELLED, com.shop.mall.enums.OrderStatus.REFUNDED) " +
           "ORDER BY o.createdAt ASC")
    List<Order> findValidOrdersByYear(@Param("year") int year);

    /** 매출 데이터가 존재하는 연도 목록 조회 */
    @Query("SELECT DISTINCT YEAR(o.createdAt) FROM Order o " +
           "WHERE o.status NOT IN (com.shop.mall.enums.OrderStatus.CANCELLED, com.shop.mall.enums.OrderStatus.REFUNDED) " +
           "ORDER BY YEAR(o.createdAt) ASC")
    List<Integer> findDistinctOrderYears();
}
