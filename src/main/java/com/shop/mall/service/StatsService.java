package com.shop.mall.service;

import com.shop.mall.entity.Product;
import com.shop.mall.repository.MemberRepository;
import com.shop.mall.repository.OrderRepository;
import com.shop.mall.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shop.mall.entity.Order;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class StatsService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;

    public StatsService(OrderRepository orderRepository,
                        ProductRepository productRepository,
                        MemberRepository memberRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.memberRepository = memberRepository;
    }

    // === 매출 통계 ===

    /** 이번 달 매출 */
    public long getMonthlySales() {
        LocalDate today = LocalDate.now();
        LocalDateTime from = today.withDayOfMonth(1).atStartOfDay();
        LocalDateTime to = today.plusDays(1).atStartOfDay();
        return orderRepository.sumTotalPriceBetween(from, to);
    }

    /** 이번 달 주문 건수 */
    public long getMonthlyOrderCount() {
        LocalDate today = LocalDate.now();
        LocalDateTime from = today.withDayOfMonth(1).atStartOfDay();
        LocalDateTime to = today.plusDays(1).atStartOfDay();
        return orderRepository.countOrdersBetween(from, to);
    }

    /** 이번 주 매출 (월요일~오늘) */
    public long getWeeklySales() {
        LocalDate today = LocalDate.now();
        LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDateTime from = monday.atStartOfDay();
        LocalDateTime to = today.plusDays(1).atStartOfDay();
        return orderRepository.sumTotalPriceBetween(from, to);
    }

    /** 이번 주 주문 건수 */
    public long getWeeklyOrderCount() {
        LocalDate today = LocalDate.now();
        LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDateTime from = monday.atStartOfDay();
        LocalDateTime to = today.plusDays(1).atStartOfDay();
        return orderRepository.countOrdersBetween(from, to);
    }

    // === 재고 알람 ===

    /** 재고 5개 이하 상품 목록 */
    public List<Product> getLowStockProducts() {
        return productRepository.findByEnabledTrueAndStockLessThanEqualOrderByStockAsc(5);
    }

    // === 기본 통계 ===

    public long getTotalProducts() {
        return productRepository.count();
    }

    public long getTotalMembers() {
        return memberRepository.count();
    }

    public long getTotalOrders() {
        return orderRepository.count();
    }

    // === 연도별 월별 매출 통계 ===

    /** 특정 연도의 월별 매출을 1월~12월로 반환 (데이터 없는 달은 0) */
    public Map<String, Long> getMonthlyRevenueByYear(int year) {
        List<Order> orders = orderRepository.findValidOrdersByYear(year);

        Map<String, Long> result = new LinkedHashMap<>();
        for (int m = 1; m <= 12; m++) {
            result.put(m + "월", 0L);
        }

        for (Order order : orders) {
            if (order.getCreatedAt() != null) {
                int month = order.getCreatedAt().getMonthValue();
                String key = month + "월";
                result.put(key, result.get(key) + order.getTotalPrice());
            }
        }
        return result;
    }

    /** 매출 데이터가 있는 연도 목록 (없으면 현재 연도) */
    public List<Integer> getAvailableYears() {
        List<Integer> years = orderRepository.findDistinctOrderYears();
        if (years.isEmpty()) {
            years = List.of(LocalDate.now().getYear());
        }
        return years;
    }

    /** 특정 연도의 총 매출액 */
    public long getTotalRevenueByYear(int year) {
        List<Order> orders = orderRepository.findValidOrdersByYear(year);
        return orders.stream().mapToLong(Order::getTotalPrice).sum();
    }
}
