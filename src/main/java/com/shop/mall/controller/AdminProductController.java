package com.shop.mall.controller;

import com.shop.mall.entity.Product;
import com.shop.mall.enums.Category;
import com.shop.mall.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/product")
public class AdminProductController {

    private final ProductService productService;

    public AdminProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String keyword, Model model) {
        if (keyword != null && !keyword.isBlank()) {
            model.addAttribute("products", productService.searchForAdmin(keyword));
            model.addAttribute("keyword", keyword);
        } else {
            model.addAttribute("products", productService.findAllForAdmin());
        }
        return "admin/product/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("product", productService.findByIdForAdmin(id));
        return "admin/product/detail";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", Category.values());
        model.addAttribute("isEdit", false);
        return "admin/product/form";
    }

    @PostMapping("/new")
    public String create(@ModelAttribute Product product, RedirectAttributes redirectAttributes) {
        productService.save(product);
        redirectAttributes.addFlashAttribute("message", "상품이 등록되었습니다.");
        return "redirect:/admin/product";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("product", productService.findByIdForAdmin(id));
        model.addAttribute("categories", Category.values());
        model.addAttribute("isEdit", true);
        return "admin/product/form";
    }

    @PostMapping("/{id}/edit")
    public String edit(@PathVariable Long id, @ModelAttribute Product product,
                       RedirectAttributes redirectAttributes) {
        Product existing = productService.findByIdForAdmin(id);
        existing.setName(product.getName());
        existing.setDescription(product.getDescription());
        existing.setPrice(product.getPrice());
        existing.setOriginalPrice(product.getOriginalPrice());
        existing.setCategory(product.getCategory());
        existing.setImageUrl(product.getImageUrl());
        existing.setStock(product.getStock());
        existing.setSale(product.isSale());
        existing.setNew(product.isNew());
        existing.setEnabled(product.isEnabled());
        productService.save(existing);
        redirectAttributes.addFlashAttribute("message", "상품이 수정되었습니다.");
        return "redirect:/admin/product";
    }

    @PostMapping("/{id}/toggle")
    public String toggleEnabled(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        productService.toggleEnabled(id);
        redirectAttributes.addFlashAttribute("message", "상품 상태가 변경되었습니다.");
        return "redirect:/admin/product";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        productService.deleteProduct(id);
        redirectAttributes.addFlashAttribute("message", "상품이 비활성화되었습니다.");
        return "redirect:/admin/product";
    }
}
