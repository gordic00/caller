package com.test.caller.model;

import lombok.Data;

import java.util.List;

@Data
public class AliexpressData {
    private String link;
    private String title;
    private String description;
    private String price;
    private List<String> imageUrls;
    private String seller;
    private String sellerUrl;
    private String currency;
    private String screenshotPath;
}
