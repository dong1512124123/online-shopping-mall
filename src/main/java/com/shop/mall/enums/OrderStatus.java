package com.shop.mall.enums;

public enum OrderStatus {
    PENDING("주문접수"),
    PAID("결제완료"),
    PREPARING("배송준비"),
    SHIPPING("배송중"),
    DELIVERED("배송완료"),
    CANCELLED("주문취소"),
    REFUNDED("환불완료");

    private final String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() { return displayName; }
}
