package com.shop.mall.enums;

public enum RankingPeriod {
    DAILY("일간"),
    WEEKLY("주간"),
    MONTHLY("월간");

    private final String displayName;

    RankingPeriod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() { return displayName; }
}
