package com.shop.mall.enums;

public enum PaymentStatus {
    PENDING("결제대기"),
    COMPLETED("결제완료"),
    FAILED("결제실패"),
    REFUNDED("환불완료");

    private final String displayName;

    PaymentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() { return displayName; }
}
