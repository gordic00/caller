package com.test.caller.model;

import lombok.Data;

import java.util.List;

@Data
public class FbData {
    private String link;
    private String description;
    private List<String> imageUrls;
    private String seller;
    private String sellerUrl;
    private String screenshotPath;
}
