package com.shop.mall.controller;

import com.shop.mall.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final ProductService productService;

    public HomeController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("newProducts", productService.getNewArrivals());
        model.addAttribute("rankingProducts", productService.getRanking());
        model.addAttribute("saleProducts", productService.getSaleProducts());
        return "index";
    }
}
