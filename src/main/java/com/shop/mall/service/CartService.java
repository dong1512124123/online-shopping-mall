package com.shop.mall.service;

import com.shop.mall.entity.CartItem;
import com.shop.mall.entity.Member;
import com.shop.mall.entity.Product;
import com.shop.mall.repository.CartItemRepository;
import com.shop.mall.repository.MemberRepository;
import com.shop.mall.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;

    public CartService(CartItemRepository cartItemRepository,
                       MemberRepository memberRepository,
                       ProductRepository productRepository) {
        this.cartItemRepository = cartItemRepository;
        this.memberRepository = memberRepository;
        this.productRepository = productRepository;
    }

    public List<CartItem> getCartItems(String username) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
        return cartItemRepository.findByMemberOrderByCreatedAtDesc(member);
    }

    @Transactional
    public void addToCart(String username, Long productId, int quantity) {
        if (quantity < 1 || quantity > 999) {
            throw new IllegalArgumentException("수량은 1~999 사이여야 합니다.");
        }

        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

        Optional<CartItem> existing = cartItemRepository.findByMemberAndProduct(member, product);
        if (existing.isPresent()) {
            CartItem item = existing.get();
            int newQty = item.getQuantity() + quantity;
            item.setQuantity(Math.min(newQty, 999));
        } else {
            CartItem item = new CartItem();
            item.setMember(member);
            item.setProduct(product);
            item.setQuantity(quantity);
            cartItemRepository.save(item);
        }
    }

    @Transactional
    public void updateQuantity(Long cartItemId, int quantity, String username) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("장바구니 항목을 찾을 수 없습니다."));
        if (!item.getMember().getUsername().equals(username)) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }
        if (quantity <= 0) {
            cartItemRepository.delete(item);
        } else if (quantity > 999) {
            throw new IllegalArgumentException("수량은 999개를 초과할 수 없습니다.");
        } else {
            item.setQuantity(quantity);
        }
    }

    @Transactional
    public void removeFromCart(Long cartItemId, String username) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("장바구니 항목을 찾을 수 없습니다."));
        if (!item.getMember().getUsername().equals(username)) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }
        cartItemRepository.delete(item);
    }

    public int getCartCount(String username) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
        return cartItemRepository.countByMember(member);
    }
}
