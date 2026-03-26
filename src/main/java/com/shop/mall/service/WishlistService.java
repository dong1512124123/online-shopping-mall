package com.shop.mall.service;

import com.shop.mall.entity.Member;
import com.shop.mall.entity.Product;
import com.shop.mall.entity.Wishlist;
import com.shop.mall.repository.MemberRepository;
import com.shop.mall.repository.ProductRepository;
import com.shop.mall.repository.WishlistRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;

    public WishlistService(WishlistRepository wishlistRepository,
                           MemberRepository memberRepository,
                           ProductRepository productRepository) {
        this.wishlistRepository = wishlistRepository;
        this.memberRepository = memberRepository;
        this.productRepository = productRepository;
    }

    public List<Wishlist> getWishlist(String username) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
        return wishlistRepository.findByMemberOrderByCreatedAtDesc(member);
    }

    @Transactional
    public boolean toggleWishlist(String username, Long productId) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

        if (wishlistRepository.existsByMemberAndProduct(member, product)) {
            wishlistRepository.deleteByMemberAndProduct(member, product);
            product.setWishCount(Math.max(0, product.getWishCount() - 1));
            return false; // 좋아요 해제
        } else {
            Wishlist wishlist = new Wishlist();
            wishlist.setMember(member);
            wishlist.setProduct(product);
            wishlistRepository.save(wishlist);
            product.setWishCount(product.getWishCount() + 1);
            return true; // 좋아요 추가
        }
    }

    public boolean isWished(String username, Long productId) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));
        return wishlistRepository.existsByMemberAndProduct(member, product);
    }
}
