package com.shop.mall.repository;

import com.shop.mall.entity.Member;
import com.shop.mall.entity.Product;
import com.shop.mall.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    List<Wishlist> findByMemberOrderByCreatedAtDesc(Member member);

    Optional<Wishlist> findByMemberAndProduct(Member member, Product product);

    boolean existsByMemberAndProduct(Member member, Product product);

    void deleteByMemberAndProduct(Member member, Product product);
}
