package com.shop.mall.repository;

import com.shop.mall.entity.CartItem;
import com.shop.mall.entity.Member;
import com.shop.mall.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByMemberOrderByCreatedAtDesc(Member member);

    Optional<CartItem> findByMemberAndProduct(Member member, Product product);

    void deleteByMemberAndProduct(Member member, Product product);

    int countByMember(Member member);
}
