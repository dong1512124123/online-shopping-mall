package com.shop.mall.controller;

import com.shop.mall.enums.OrderStatus;
import com.shop.mall.service.OrderService;
import com.shop.mall.service.StatsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    private final StatsService statsService;
    private final OrderService orderService;

    public AdminDashboardController(StatsService statsService,
                                     OrderService orderService) {
        this.statsService = statsService;
        this.orderService = orderService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // 기본 통계
        model.addAttribute("productCount", statsService.getTotalProducts());
        model.addAttribute("memberCount", statsService.getTotalMembers());
        model.addAttribute("orderCount", statsService.getTotalOrders());
        model.addAttribute("pendingOrderCount", orderService.getOrderCountByStatus(OrderStatus.PAID));

        // 매출 통계
        model.addAttribute("monthlySales", statsService.getMonthlySales());
        model.addAttribute("monthlyOrderCount", statsService.getMonthlyOrderCount());
        model.addAttribute("weeklySales", statsService.getWeeklySales());
        model.addAttribute("weeklyOrderCount", statsService.getWeeklyOrderCount());

        // 재고 알람
        model.addAttribute("lowStockProducts", statsService.getLowStockProducts());

        // 베스트셀러
        model.addAttribute("bestSellers", orderService.getBestSellers(10));

        return "admin/dashboard";
    }

    /** 매출 현황 페이지 (연도별/월별 그래프) */
    @GetMapping("/revenue")
    public String revenueChart(
            @RequestParam(value = "year", required = false) Integer year,
            Model model) {

        int selectedYear = (year != null) ? year : LocalDate.now().getYear();

        List<Integer> years = statsService.getAvailableYears();
        Map<String, Long> monthlyRevenue = statsService.getMonthlyRevenueByYear(selectedYear);
        long totalRevenue = statsService.getTotalRevenueByYear(selectedYear);

        model.addAttribute("years", years);
        model.addAttribute("selectedYear", selectedYear);
        model.addAttribute("monthlyRevenue", monthlyRevenue);
        model.addAttribute("totalRevenue", totalRevenue);

        return "admin/revenue";
    }
}
