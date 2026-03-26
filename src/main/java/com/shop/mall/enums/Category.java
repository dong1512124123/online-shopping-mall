package com.shop.mall.enums;

public enum Category {
    MAN("남성"),
    WOMAN("여성"),
    SPORTS("스포츠"),
    OUTLET("아울렛"),
    KIDS("키즈");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
