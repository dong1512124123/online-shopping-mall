package com.shop.mall.controller;

import com.shop.mall.service.CartService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    // 장바구니 목록
    @GetMapping
    public String cartList(Authentication authentication, Model model) {
        var items = cartService.getCartItems(authentication.getName());
        model.addAttribute("cartItems", items);
        int totalPrice = items.stream().mapToInt(i -> i.getTotalPrice()).sum();
        model.addAttribute("totalPrice", totalPrice);
        return "cart/list";
    }

    // 장바구니에 담기
    @PostMapping("/add")
    public String addToCart(@RequestParam Long productId,
                            @RequestParam(defaultValue = "1") int quantity,
                            Authentication authentication,
                            RedirectAttributes redirectAttributes) {
        cartService.addToCart(authentication.getName(), productId, quantity);
        redirectAttributes.addFlashAttribute("message", "장바구니에 담았습니다.");
        return "redirect:/product/" + productId;
    }

    // 수량 변경
    @PostMapping("/{id}/update")
    public String updateQuantity(@PathVariable Long id,
                                  @RequestParam int quantity,
                                  Authentication authentication,
                                  RedirectAttributes redirectAttributes) {
        cartService.updateQuantity(id, quantity, authentication.getName());
        redirectAttributes.addFlashAttribute("message", "수량이 변경되었습니다.");
        return "redirect:/cart";
    }

    // 장바구니에서 삭제
    @PostMapping("/{id}/remove")
    public String removeFromCart(@PathVariable Long id,
                                  Authentication authentication,
                                  RedirectAttributes redirectAttributes) {
        cartService.removeFromCart(id, authentication.getName());
        redirectAttributes.addFlashAttribute("message", "장바구니에서 삭제되었습니다.");
        return "redirect:/cart";
    }
}
