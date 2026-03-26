package com.shop.mall.repository;

import com.shop.mall.entity.Product;
import com.shop.mall.enums.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByCategoryAndEnabledTrueOrderByCreatedAtDesc(Category category);

    List<Product> findByEnabledTrueOrderByCreatedAtDesc();

    List<Product> findTop8ByEnabledTrueAndIsNewTrueOrderByCreatedAtDesc();

    List<Product> findTop8ByEnabledTrueOrderByViewCountDesc();

    List<Product> findTop8ByEnabledTrueAndIsSaleTrueOrderByCreatedAtDesc();

    List<Product> findByNameContainingIgnoreCaseAndEnabledTrueOrderByCreatedAtDesc(String keyword);

    // Admin: all products including disabled
    List<Product> findAllByOrderByCreatedAtDesc();

    List<Product> findByNameContainingIgnoreCaseOrderByCreatedAtDesc(String keyword);

    // 재고 5개 이하 상품 (활성 상품만)
    List<Product> findByEnabledTrueAndStockLessThanEqualOrderByStockAsc(int stock);
}
