package com.example.perfume;

public class Perfume {
    private final String name;   // 'final'로 선언
    private final String notes;  // 'final'로 선언
    private final String imageUrl;  // 'final'로 선언

    public Perfume(String name, String notes, String imageUrl) {
        this.name = name;
        this.notes = notes;
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public String getNotes() {
        return notes;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
