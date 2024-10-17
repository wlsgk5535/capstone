package com.example.calculator;

import java.util.List;

public class Perfume {
    private String name;
    private String brand;
    private String description;
    private String notes;
    private String imageURL;
    private List<String> keywords;  // 키워드 필드 추가

    public Perfume(String name, String brand, String description, String notes, String imageURL, List<String> keywords) {
        this.name = name;
        this.brand = brand;
        this.description = description;
        this.notes = notes;
        this.imageURL = imageURL;
        this.keywords = keywords;  // 키워드 초기화
    }

    public String getName() {
        return name;
    }

    public String getBrand() {
        return brand;
    }

    public String getDescription() {
        return description;
    }

    public String getNotes() {
        return notes;
    }

    public String getImageURL() {
        return imageURL;
    }

    public List<String> getKeywords() {
        return keywords;  // 키워드 접근자
    }
}
