package com.example.calculator;

public class Item {
    private String productUrl;
    private String category;
    private String thumbnailUrl;

    // 생성자
    public Item(String productUrl, String category, String thumbnailUrl) {
        this.productUrl = productUrl;
        this.category = category;
        this.thumbnailUrl = thumbnailUrl;
    }

    // Getter 메서드
    public String getProductUrl() {
        return productUrl;
    }

    public String getCategory() {
        return category;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }
}
