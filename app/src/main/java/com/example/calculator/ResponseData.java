package com.example.calculator;
import java.util.List;

public class ResponseData {
    private String message;
    private List<Item> items;
    private String filename;
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }
    public String getFilename() {
        return filename;
    }
}
