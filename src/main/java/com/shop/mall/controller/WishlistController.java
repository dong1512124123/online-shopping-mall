package com.shop.mall.controller;

import com.shop.mall.service.WishlistService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    // 좋아요 목록
    @GetMapping
    public String wishlist(Authentication authentication, Model model) {
        model.addAttribute("wishItems", wishlistService.getWishlist(authentication.getName()));
        return "wishlist/list";
    }

    // 좋아요 토글 (AJAX)
    @PostMapping("/toggle")
    @ResponseBody
    public Map<String, Object> toggleWishlist(@RequestParam Long productId,
                                               Authentication authentication) {
        boolean wished = wishlistService.toggleWishlist(authentication.getName(), productId);
        return Map.of("wished", wished, "message", wished ? "좋아요에 추가했습니다." : "좋아요를 해제했습니다.");
    }
}
