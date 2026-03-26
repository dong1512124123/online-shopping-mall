package com.shop.mall.controller;

import com.shop.mall.entity.CartItem;
import com.shop.mall.entity.Order;
import com.shop.mall.enums.PaymentMethod;
import com.shop.mall.service.CartService;
import com.shop.mall.service.OrderService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/order")
public class OrderController {

    private final OrderService orderService;
    private final CartService cartService;

    public OrderController(OrderService orderService, CartService cartService) {
        this.orderService = orderService;
        this.cartService = cartService;
    }

    @GetMapping("/checkout")
    public String checkout(Model model, Authentication auth) {
        List<CartItem> cartItems = cartService.getCartItems(auth.getName());
        if (cartItems.isEmpty()) {
            return "redirect:/cart";
        }

        int totalPrice = cartItems.stream().mapToInt(CartItem::getTotalPrice).sum();

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("paymentMethods", PaymentMethod.values());
        return "order/checkout";
    }

    @PostMapping("/place")
    public String placeOrder(@RequestParam PaymentMethod paymentMethod,
                              @RequestParam String recipientName,
                              @RequestParam String recipientPhone,
                              @RequestParam String zipcode,
                              @RequestParam String address,
                              @RequestParam(required = false) String addressDetail,
                              @RequestParam(required = false) String memo,
                              Authentication auth,
                              RedirectAttributes redirectAttributes) {
        try {
            // 배송 정보 검증
            if (recipientName == null || recipientName.trim().isEmpty() || recipientName.length() > 100) {
                throw new IllegalArgumentException("받는 분 이름을 올바르게 입력해주세요. (1~100자)");
            }
            if (recipientPhone == null || !recipientPhone.matches("^01[0-9]-?\\d{3,4}-?\\d{4}$")) {
                throw new IllegalArgumentException("올바른 연락처를 입력해주세요.");
            }
            if (zipcode == null || !zipcode.matches("^\\d{5}$")) {
                throw new IllegalArgumentException("올바른 우편번호를 입력해주세요. (5자리 숫자)");
            }
            if (address == null || address.trim().isEmpty() || address.length() > 255) {
                throw new IllegalArgumentException("주소를 올바르게 입력해주세요.");
            }
            if (addressDetail != null && addressDetail.length() > 200) {
                throw new IllegalArgumentException("상세주소는 200자 이내로 입력해주세요.");
            }
            if (memo != null && memo.length() > 500) {
                throw new IllegalArgumentException("배송 메모는 500자 이내로 입력해주세요.");
            }

            Order order = orderService.createOrderFromCart(
                    auth.getName(), paymentMethod,
                    recipientName.trim(), recipientPhone.trim(),
                    zipcode.trim(), address.trim(),
                    addressDetail != null ? addressDetail.trim() : null,
                    memo != null ? memo.trim() : null);
            return "redirect:/order/complete/" + order.getId();
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/order/checkout";
        }
    }

    @GetMapping("/complete/{id}")
    public String orderComplete(@PathVariable Long id, Model model, Authentication auth) {
        Order order = orderService.getOrderDetail(id, auth.getName());
        model.addAttribute("order", order);
        return "order/complete";
    }

    @GetMapping("/list")
    public String myOrders(Model model, Authentication auth) {
        model.addAttribute("orders", orderService.getMyOrders(auth.getName()));
        return "order/list";
    }

    @GetMapping("/{id}")
    public String orderDetail(@PathVariable Long id, Model model, Authentication auth) {
        Order order = orderService.getOrderDetail(id, auth.getName());
        model.addAttribute("order", order);
        return "order/detail";
    }

    @PostMapping("/{id}/cancel")
    public String cancelOrder(@PathVariable Long id, Authentication auth,
                               RedirectAttributes redirectAttributes) {
        try {
            orderService.cancelOrder(id, auth.getName());
            redirectAttributes.addFlashAttribute("message", "주문이 취소되었습니다.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/order/list";
    }
}
