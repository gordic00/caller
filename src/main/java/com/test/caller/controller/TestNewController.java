package com.test.caller.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.caller.helper.FileHelper;
import com.test.caller.model.AliexpressData;
import com.test.caller.model.SiteData;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@Validated
@RestController
@RequestMapping(path = "/api/v1/test-2")
@RequiredArgsConstructor
public class TestNewController {
    private final FileHelper fileHelper;

    @Value("${chrome.driver.path}")
    private String chromeDriver;

    private String getUrl(
            String ip,
            String url,
            Boolean scrollToBottom,
            String chromeArg,
            String hoverElement,
            String scrollAndClick,
            String pressAndHold,
            Integer pressAndHoldWait,
            String waitForElement,
            Integer wait,
            String endpoint,
            String searchBarSelector,
            String searchBarValue,
            String searchButtonSelector
    ) {
        chromeArg = chromeArg == null ? "" : "&chromeArg=" + fileHelper.base64Encode(chromeArg);
        hoverElement = hoverElement == null ? "" : "&hoverElement=" + fileHelper.base64UrlEncode("a[data-qaid='company_name']");
        scrollAndClick = scrollAndClick == null ? "" : "&scrollAndClick=" + fileHelper.base64Encode("button:has-text('Show more results')");
        pressAndHold = pressAndHold == null ? "" : "&pressAndHold=" + fileHelper.base64Encode("div[class='x92rtbv x10l6tqk x1tk7jg1 x1vjfegm'] > div");
        String pressAndHoldWaitStr = pressAndHoldWait == null ? "" : "&pressAndHoldWait=" + pressAndHoldWait;
        waitForElement = waitForElement == null ? "" : "&waitForElement=" + fileHelper.base64Encode("div[class='flex-auto flex-column  DXQgih']");
        searchBarSelector = searchBarSelector == null ? "" : "&searchBarSelector=" + fileHelper.base64Encode("div[class='rax-view searchbar-input-wrap'] > input");
        searchBarValue = searchBarValue == null ? "" : "&searchBarValue=" + fileHelper.base64Encode(searchBarValue);
        searchButtonSelector = searchButtonSelector == null ? "" : "&searchButtonSelector=" + fileHelper.base64Encode("div[class='rax-view search-button']");
        return "http://" + ip + ":8081" + "/api/v1/" + endpoint + "?url=" + fileHelper.base64UrlEncode(url)
               + (scrollToBottom ? "&scrollToBottom=true" : "&scrollToBottom=false")
               + chromeArg
               + hoverElement
               + scrollAndClick
               + pressAndHold
               + pressAndHoldWaitStr
               + waitForElement
               + searchBarSelector
               + searchBarValue
               + searchButtonSelector
//                + "&initWait=2000"
               + "&wait=" + (wait != null ? wait : "8000");
    }

    @GetMapping(path = "/get/aliexpress")
    public ResponseEntity<List<AliexpressData>> getProdUrls() {
        List<AliexpressData> resp = new ArrayList<>();
        prodUrls.forEach(p -> {
            RestTemplate rest = new RestTemplate();
            try {
                ResponseEntity<SiteData> response = rest.getForEntity(getUrl("localhost", p, false,
                        null, null, "null", null, null,
                        null, 10000, "get/playwright/site-data",
                        null, null, null), SiteData.class);
                if (response.getBody() != null && response.getStatusCode().equals(HttpStatus.OK)) {
                    Document document = Jsoup.parse(response.getBody().getHtml());

                    Element titleElement = document.selectFirst("div[class='title--wrap--Ms9Zv4A']");
                    Element descriptionElement = document.selectFirst("meta[property='og:description']");
                    Element priceElement = document.selectFirst("div[class='price--current--H7sGzqb product-price-current'] > div");
                    Elements imageElements = document.select("div[class='slider--box--TJYmEtw'] > div > img");
                    Element sellerElement = document.selectFirst("a[class='store-header--storeName--vINzvPw']");
                    String currency = "RSD";

                    String price = "";
                    if (priceElement != null) {
                        price = priceElement.text().replaceAll("[^0-9.]", "");
                    }

                    ArrayList<String> imgUrls = new ArrayList<>();
                    imageElements.forEach(element -> imgUrls.add(element.attr("src")));

                    String title = titleElement != null ? titleElement.text() : "";
                    String seller = sellerElement != null ? sellerElement.text() : "";

                    AliexpressData data = new AliexpressData();
                    data.setLink(p);
                    data.setTitle(title);
                    data.setDescription(descriptionElement != null ? descriptionElement.text() : title);
                    data.setImageUrls(imgUrls);
                    data.setSeller(seller);
                    data.setPrice(price);
                    data.setCurrency(currency);
                    data.setSellerUrl(sellerElement != null ? sellerElement.attr("href") : "");
                    data.setScreenshotPath(response.getBody().getScreenshotUrl());
                    resp.add(data);
                    ObjectMapper om = new ObjectMapper();
                    System.out.println(om.writerWithDefaultPrettyPrinter().writeValueAsString(data) + ",");
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        });
        return ResponseEntity.ok(resp);
    }


    @GetMapping(path = "/get/shopee")
    public ResponseEntity<List<AliexpressData>> getShopProdUrls() {
        List<AliexpressData> resp = new ArrayList<>();
        prodUrls.forEach(p -> {
            RestTemplate rest = new RestTemplate();
            try {
                ResponseEntity<SiteData> response = rest.getForEntity(getUrl("localhost", p, false,
                        null, null, "null", null, null,
                        null, 10000, "get/playwright/site-data",
                        null, null, null), SiteData.class);
                if (response.getBody() != null && response.getStatusCode().equals(HttpStatus.OK)) {
                    Document document = Jsoup.parse(response.getBody().getHtml());

                    Element titleElement = document.selectFirst("meta[property='og:title']");
                    Element descriptionElement = document.selectFirst("meta[name='description']");
                    Element priceElement = document.selectFirst("div[class='flex items-center'] > div[class='G27FPf']");
                    Elements imagesElements = document.select("picture >img");
                    Element sellerElement = document.selectFirst("div[class='PYEGyz'] > div[class='fV3TIn']");
                    Element sellerUrlElement = document.selectFirst("a[class='lG5Xxv']");
                    Elements categoryElements = document.select("div[class*='page-product__breadcrumb'] > a");
                    Set<String> categories = new HashSet<>();
                    categoryElements.forEach(element -> categories.add(element.text()));

                    List<String> imagesUrls = new ArrayList<>();
                    imagesElements.forEach(element -> imagesUrls.add(element.attr("src")));

                    String priceValue = "";
                    String minPrice = "";
                    if (priceElement != null) {
                        priceValue = priceElement.text();
                        if (priceValue.contains("-")) {
                            minPrice = priceValue.split("-")[0];

                            priceValue = minPrice.replaceAll("[^0-9.]", "");
                        } else {
                            priceValue = priceValue.replaceAll("[^0-9.]", "");
                        }
                    }

                    String title = titleElement != null ? titleElement.attr("content") : "";

                    AliexpressData data = new AliexpressData();
                    data.setLink(p);
                    data.setTitle(title);
                    data.setDescription(descriptionElement != null ? descriptionElement.text() : title);
                    data.setImageUrls(imagesUrls);
                    data.setSeller(sellerElement != null ? sellerElement.text() : "");
                    data.setSellerUrl(sellerUrlElement != null ? "https://shopee.co.id" + sellerUrlElement.attr("href") : "");
                    data.setPrice(priceValue);
                    data.setCurrency("IDR");
                    data.setScreenshotPath(response.getBody().getScreenshotUrl());
                    resp.add(data);
                    ObjectMapper om = new ObjectMapper();
                    System.out.println(om.writerWithDefaultPrettyPrinter().writeValueAsString(data) + ",");
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        });
        return ResponseEntity.ok(resp);
    }


    @GetMapping(path = "/get/alibaba")
    public ResponseEntity<List<AliexpressData>> getAlibabaProdUrls() {
        List<AliexpressData> resp = new ArrayList<>();
        prodUrls.forEach(p -> {
            RestTemplate rest = new RestTemplate();
            try {
                ResponseEntity<SiteData> response = rest.getForEntity(getUrl("localhost", p, false,
                        null, null, "null", null, null,
                        null, 8000, "get/playwright/site-data",
                        null, null, null), SiteData.class);
                if (response.getBody() != null && response.getStatusCode().equals(HttpStatus.OK)) {
                    Document document = Jsoup.parse(response.getBody().getHtml());

                    Element titleElement = document.selectFirst("div[class^='product-title'] > h1");
                    Element descriptionElement = document.selectFirst("meta[name='description']");
                    Elements imagesElements = document.select("div[class='image-list-slider'] > div > img");
                    Element priceElement = document.selectFirst("div[class='price'] > span");
                    Element sellerElement = document.selectFirst("div[class*='company'] > div[class*='company'] > div[class*='header-'] > div[class*='company'] > a");
                    Elements categoryElements = document.select("nav > ul > li > a");

                    List<String> imagesUrls = new ArrayList<>();
                    imagesElements.forEach(element -> imagesUrls.add(element.attr("src")));

                    String priceValue = priceElement != null ? priceElement.text().replaceAll("[^0-9.]", "") : "";

                    String title = titleElement != null ? titleElement.text() : "";

                    AliexpressData data = new AliexpressData();
                    data.setLink(p);
                    data.setTitle(title);
                    data.setDescription(descriptionElement != null ? descriptionElement.attr("content") : title);
                    data.setImageUrls(imagesUrls);
                    data.setSeller(sellerElement != null ? sellerElement.text() : "");
                    data.setSellerUrl(sellerElement != null ? sellerElement.attr("href") : "");
                    data.setPrice(priceValue);
                    data.setCurrency("USD");
                    data.setScreenshotPath(response.getBody().getScreenshotUrl());
                    resp.add(data);
                    ObjectMapper om = new ObjectMapper();
                    System.out.println(om.writerWithDefaultPrettyPrinter().writeValueAsString(data) + ",");
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        });
        return ResponseEntity.ok(resp);
    }


    @GetMapping(path = "/get/dolap")
    public ResponseEntity<List<AliexpressData>> getDolapProdUrls() {
        List<AliexpressData> resp = new ArrayList<>();
        prodUrls.forEach(p -> {
            RestTemplate rest = new RestTemplate();
            try {
                ResponseEntity<SiteData> response = rest.getForEntity(getUrl("localhost", p, false,
                        null, null, "null", null, null,
                        null, 8000, "get/playwright/site-data",
                        null, null, null), SiteData.class);
                if (response.getBody() != null && response.getStatusCode().equals(HttpStatus.OK)) {
                    Document document = Jsoup.parse(response.getBody().getHtml());

                    Element titleElement = document.selectFirst("meta[name='title']");
                    Element descriptionElement = document.selectFirst("meta[name='description']");
                    Elements imagesElements = document.select("ul[class='p-slideset'] > li > a > img");
                    Element priceElement = document.selectFirst("span[class='price ']");
                    Element sellerElement = document.selectFirst("div[class='title-stars-block'] > a[class='title']");

                    List<String> imagesUrls = new ArrayList<>();
                    imagesElements.forEach(element -> imagesUrls.add(element.attr("src")));

                    String priceValue = priceElement != null ? priceElement.text().replaceAll("[^0-9.]", "") : "";

                    String title = titleElement != null ? titleElement.text() : "";

                    AliexpressData data = new AliexpressData();
                    data.setLink(p);
                    data.setTitle(title);
                    data.setDescription(descriptionElement != null ? descriptionElement.attr("content") : title);
                    data.setImageUrls(imagesUrls);
                    data.setSeller(sellerElement != null ? sellerElement.text() : "");
                    data.setSellerUrl(sellerElement != null ? sellerElement.attr("href") : "");
                    data.setPrice(priceValue);
                    data.setCurrency("USD");
                    data.setScreenshotPath(response.getBody().getScreenshotUrl());
                    resp.add(data);
                    ObjectMapper om = new ObjectMapper();
                    System.out.println(om.writerWithDefaultPrettyPrinter().writeValueAsString(data) + ",");
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        });
        return ResponseEntity.ok(resp);
    }

    private List<String> prodUrls = Stream.of(""
    ).toList();
}


