package com.shop.mall.service;

import com.shop.mall.entity.Product;
import com.shop.mall.enums.Category;
import com.shop.mall.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> findByCategory(Category category) {
        return productRepository.findByCategoryAndEnabledTrueOrderByCreatedAtDesc(category);
    }

    public List<Product> findAll() {
        return productRepository.findByEnabledTrueOrderByCreatedAtDesc();
    }

    @Transactional
    public Product findById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: " + id));
        product.setViewCount(product.getViewCount() + 1);
        return product;
    }

    public List<Product> getNewArrivals() {
        return productRepository.findTop8ByEnabledTrueAndIsNewTrueOrderByCreatedAtDesc();
    }

    public List<Product> getRanking() {
        return productRepository.findTop8ByEnabledTrueOrderByViewCountDesc();
    }

    public List<Product> getSaleProducts() {
        return productRepository.findTop8ByEnabledTrueAndIsSaleTrueOrderByCreatedAtDesc();
    }

    public List<Product> search(String keyword) {
        return productRepository.findByNameContainingIgnoreCaseAndEnabledTrueOrderByCreatedAtDesc(keyword);
    }

    // --- Admin methods ---

    public List<Product> findAllForAdmin() {
        return productRepository.findAllByOrderByCreatedAtDesc();
    }

    public Product findByIdForAdmin(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: " + id));
    }

    @Transactional
    public Product save(Product product) {
        return productRepository.save(product);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: " + id));
        product.setEnabled(false);
    }

    @Transactional
    public void toggleEnabled(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: " + id));
        product.setEnabled(!product.isEnabled());
    }

    public List<Product> searchForAdmin(String keyword) {
        return productRepository.findByNameContainingIgnoreCaseOrderByCreatedAtDesc(keyword);
    }
}
