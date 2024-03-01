package com.test.caller.service;

import com.test.caller.helper.FileHelper;
import com.test.caller.model.PageRequest;
import com.test.caller.model.SiteData;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class CallerService {
    private final FileHelper fileHelper;

    /**
     * Get Html for Url.
     *
     * @param pageRequest PageRequest
     * @return String
     */
    public String getHtml(PageRequest pageRequest) {
        RestTemplate rest = new RestTemplate();
        String requestUrl= getUrl(pageRequest);
        System.out.println(requestUrl);
        ResponseEntity<byte[]> response = rest.getForEntity(requestUrl, byte[].class);

        if (response.getBody() != null) {
            try {
                Document document = Jsoup.parse(new String(response.getBody(), StandardCharsets.UTF_8));
                Element titleElement = document.selectFirst("meta[name='title']");
                Element descriptionElement = document.selectFirst("meta[name='description']");
                Elements imageElements = document.select("ul[class='p-slideset'] > li > a > img");
                Element priceElement = document.selectFirst("span[class='price ']");
                Element listPriceElement = document.selectFirst("div[class='price-block'] > div[class='price-detail'] > span[class='disc-price']");
                Element sellerElement = document.selectFirst("div[class='title-stars-block'] > a[class='title']");
                Elements categoryElements = document.select("ul[class='breadcrumb hidden-xs'] > li > a");

            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            return new String(response.getBody(), StandardCharsets.UTF_8);
        }
        return null;
    }

    /**
     * Get All Data by Url.
     *
     * @param pageRequest PageRequest
     * @return String
     */
    public String getAllData(PageRequest pageRequest) {
        RestTemplate rest = new RestTemplate();
        /*
        You can sand this two params as endpoints:
            get/site-data
            get/playwright/site-data
         */
        ResponseEntity<SiteData> response = rest.getForEntity(getUrl(pageRequest), SiteData.class);

        if (response.getBody() != null) {
            try {
                SiteData siteData = response.getBody();
                Document document = Jsoup.parse(siteData.getHtml());
                System.out.println(siteData.getScreenshotUrl());
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        return response.getBody().getHtml();
    }

    /**
     * Prepare request url.
     *
     * @param pageRequest PageRequest
     * @return String
     */
    private String getUrl(PageRequest pageRequest) {
        preparePageRequest(pageRequest);
        String pressAndHoldWaitStr = pageRequest.getPressAndHoldWait() == null ? "" : "&pressAndHoldWait=" + pageRequest.getPressAndHoldWait();
        String initWaitStr = pageRequest.getInitWait() == null ? "" : "&initWait=" + pageRequest.getInitWait();
        String waitStr = pageRequest.getWait() == null ? "&wait=8000" : "&wait=" + pageRequest.getWait();
        String scrollAndClickCountStr = pageRequest.getScrollAndClickCount() == null ? "" : "&scrollAndClickCount=" + pageRequest.getScrollAndClickCount();

        String srbJs = """
                var nazivInput = document.getElementById(\"naziv\");
                nazivInput.value = \"andol\";
                var searchButton = document.getElementById(\"button\");
                searchButton.click();""";

        return "http://" + pageRequest.getIp() + ":8081" + "/api/v1/" + pageRequest.getEndpoint() + "?url=" + fileHelper.base64UrlEncode(pageRequest.getUrl())
                + (pageRequest.getScrollToBottom() ? "&scrollToBottom=true" : "&scrollToBottom=false")
                + pageRequest.getChromeArg()
                + pageRequest.getHoverElement()
                + pageRequest.getScrollAndClick()
                + scrollAndClickCountStr
                + pageRequest.getPressAndHold()
                + pressAndHoldWaitStr
                + pageRequest.getWaitForElement()
                + pageRequest.getCookie()
                + initWaitStr
//                + "&javascript=" + fileHelper.base64Encode("() => runParams")
//                + "&javascript=" + fileHelper.base64Encode(uk)
//                + "&javascript=" + fileHelper.base64Encode(scroll)
                + waitStr;
    }

    private void preparePageRequest(PageRequest pageRequest) {
        pageRequest.setChromeArg(pageRequest.getChromeArg() == null ? "" : "&chromeArg=" + fileHelper.base64Encode(pageRequest.getChromeArg()));
        pageRequest.setHoverElement(pageRequest.getHoverElement() == null ? "" : "&hoverElement=" + fileHelper.base64UrlEncode("a[data-qaid='company_name']"));
        pageRequest.setScrollAndClick(pageRequest.getScrollAndClick() == null ? "" : "&scrollAndClick=" + fileHelper.base64Encode("button:has-text('Show more results')"));
        pageRequest.setPressAndHold(pageRequest.getPressAndHold() == null ? "" : "&pressAndHold=" + fileHelper.base64Encode("div[class='x92rtbv x10l6tqk x1tk7jg1 x1vjfegm'] > div"));
        pageRequest.setWaitForElement(pageRequest.getWaitForElement() == null ? "" : "&waitForElement=" + fileHelper.base64Encode("div[class='flex-auto flex-column  DXQgih']"));
        pageRequest.setCookie(pageRequest.getCookie() == null ? "" : "&cookie=" + fileHelper.base64Encode(pageRequest.getCookie()));
        pageRequest.setMultipleClick(pageRequest.getMultipleClick() == null ? "" : "&multipleClick=" + fileHelper.base64Encode(pageRequest.getMultipleClick()));
    }
}
