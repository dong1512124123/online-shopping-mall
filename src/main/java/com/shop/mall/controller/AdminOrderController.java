package com.shop.mall.controller;

import com.shop.mall.enums.OrderStatus;
import com.shop.mall.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/order")
public class AdminOrderController {

    private final OrderService orderService;

    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String status, Model model) {
        if (status != null && !status.isBlank()) {
            try {
                OrderStatus orderStatus = OrderStatus.valueOf(status);
                model.addAttribute("orders", orderService.getOrdersByStatus(orderStatus));
                model.addAttribute("currentStatus", status);
            } catch (IllegalArgumentException e) {
                // 잘못된 상태값은 무시하고 전체 목록 표시
                model.addAttribute("orders", orderService.getAllOrders());
            }
        } else {
            model.addAttribute("orders", orderService.getAllOrders());
        }
        model.addAttribute("statuses", OrderStatus.values());
        return "admin/order/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("order", orderService.getOrderById(id));
        model.addAttribute("statuses", OrderStatus.values());
        return "admin/order/detail";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id,
                                @RequestParam OrderStatus status,
                                RedirectAttributes redirectAttributes) {
        orderService.updateOrderStatus(id, status);
        redirectAttributes.addFlashAttribute("message", "주문 상태가 변경되었습니다.");
        return "redirect:/admin/order/" + id;
    }
}
