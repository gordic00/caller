package com.test.caller.model;

import lombok.Data;

@Data
public class PageRequest {
    private String url;
    private String scrollAndClick;
    private String multipleClick;
    private Integer scrollAndClickCount;
    private String javascript;
    private Integer wait;
    private String waitForElement;
    private String hoverElement;
    private String pressAndHold;
    private Integer pressAndHoldWait;
    private String cookie;
    private String chromeArg;
    private Boolean scrollToBottom;
    private Boolean useBridgeStorage;
    private Integer crawlerLog;
    private Integer initWait;
    private String ip;
    private String endpoint;
}
