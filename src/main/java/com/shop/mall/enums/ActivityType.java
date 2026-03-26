package com.shop.mall.enums;

public enum ActivityType {
    VIEW("상품 조회"),
    CART_ADD("장바구니 담기"),
    PURCHASE("구매"),
    WISHLIST("좋아요");

    private final String displayName;

    ActivityType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() { return displayName; }
}
