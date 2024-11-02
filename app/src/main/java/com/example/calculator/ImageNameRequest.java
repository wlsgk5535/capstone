package com.example.calculator;
public class ImageNameRequest {
    private String filename;
    private String pageUrl;  // 추가된 필드

    public ImageNameRequest(String filename,String pageUrl) {
        this.filename = filename;
        this.pageUrl = pageUrl;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
    public String getPageUrl() {  // 추가된 getter
        return pageUrl;
    }

    public void setPageUrl(String pageUrl) {  // 추가된 setter
        this.pageUrl = pageUrl;
    }
}
