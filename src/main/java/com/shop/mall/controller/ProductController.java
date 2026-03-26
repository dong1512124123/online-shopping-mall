package com.shop.mall.controller;

import com.shop.mall.enums.Category;
import com.shop.mall.service.ProductService;
import com.shop.mall.service.WishlistService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ProductController {

    private final ProductService productService;
    private final WishlistService wishlistService;

    public ProductController(ProductService productService, WishlistService wishlistService) {
        this.productService = productService;
        this.wishlistService = wishlistService;
    }

    // 카테고리별 상품 목록 (로그인 없이 볼 수 있음)
    @GetMapping("/category/{category}")
    public String categoryList(@PathVariable String category, Model model) {
        Category cat = Category.valueOf(category.toUpperCase());
        model.addAttribute("products", productService.findByCategory(cat));
        model.addAttribute("category", cat);
        model.addAttribute("categoryName", cat.getDisplayName());
        return "product/list";
    }

    // 상품 상세 (로그인 없이 볼 수 있음)
    @GetMapping("/product/{id}")
    public String productDetail(@PathVariable Long id, Model model, Authentication authentication) {
        var product = productService.findById(id);
        model.addAttribute("product", product);

        // 로그인한 일반 사용자면 좋아요 여부 체크 (관리자는 제외)
        if (authentication != null && authentication.isAuthenticated()
                && authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_USER"))) {
            model.addAttribute("isWished", wishlistService.isWished(authentication.getName(), id));
        } else {
            model.addAttribute("isWished", false);
        }

        return "product/detail";
    }

    // 검색 (로그인 없이 가능)
    @GetMapping("/search")
    public String search(@RequestParam(defaultValue = "") String keyword, Model model) {
        model.addAttribute("products", productService.search(keyword));
        model.addAttribute("keyword", keyword);
        return "product/search";
    }
}
