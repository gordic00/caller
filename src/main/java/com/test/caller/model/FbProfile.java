package com.test.caller.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class FbProfile {
    private String url;
    private List<String> postUrls = new ArrayList<>();
}
