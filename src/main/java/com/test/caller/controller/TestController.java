package com.test.caller.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.caller.helper.FileHelper;
import com.test.caller.model.FbData;
import com.test.caller.model.SiteData;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@Validated
@RestController
@RequestMapping(path = "/api/v1/test")
@RequiredArgsConstructor
public class TestController {
    private final FileHelper fileHelper;

    @Value("${chrome.driver.path}")
    private String chromeDriver;

    @GetMapping(path = "/get/html")
    public ResponseEntity<String> getHtmlFromCollector(
            @RequestParam(name = "ip")
            String ip,
            @RequestParam(name = "url")
            String url,
            @RequestParam(name = "scroll_to_bottom")
            Boolean scrollToBottom,
            @RequestParam(name = "chrome_arg", required = false)
            String chromeArg,
            @RequestParam(name = "hover_element", required = false)
            String hoverElement,
            @RequestParam(name = "scroll_and_click", required = false)
            String scrollAndClick,
            @RequestParam(required = false)
            String pressAndHold,
            @RequestParam(required = false)
            Integer pressAndHoldWait,
            @RequestParam(name = "wait_for_element", required = false)
            String waitForElement,
            @RequestParam(name = "searchBarSelector", required = false)
            String searchBarSelector,
            @RequestParam(name = "searchBarValue", required = false)
            String searchBarValue,
            @RequestParam(name = "searchButtonSelector", required = false)
            String searchButtonSelector,
            @RequestParam(name = "wait", required = false)
            Integer wait) {
        try {
            RestTemplate rest = new RestTemplate();
            ResponseEntity<byte[]> response = rest.getForEntity(getUrl(ip, url, scrollToBottom, chromeArg, hoverElement,
                    scrollAndClick, pressAndHold, pressAndHoldWait, waitForElement, wait, "get/playwright/html",
                    searchBarSelector, searchBarValue, searchButtonSelector), byte[].class);
//            ResponseEntity<byte[]> response = rest.getForEntity(getUrl(ip, url, scrollToBottom, chromeArg, hoverElement,
//                    scrollAndClick, pressAndHold, pressAndHoldWait, waitForElement, wait, "get/selenium/html",
//                    searchBarSelector, searchBarValue, searchButtonSelector), byte[].class);
//            ResponseEntity<byte[]> response = rest.getForEntity(getUrl(ip, url, scrollToBottom, chromeArg, hoverElement, scrollAndClick, pressAndHold, pressAndHoldWait, waitForElement, wait, "get/playwright/js"), byte[].class);
//            ResponseEntity<byte[]> response = rest.getForEntity(getUrl(ip, url, scrollToBottom, chromeArg, hoverElement, scrollAndClick, pressAndHold, pressAndHoldWait, waitForElement, wait, "get/html"), byte[].class);
//            ResponseEntity<byte[]> response = rest.getForEntity(getUrl(ip, url, scrollToBottom, chromeArg, hoverElement, scrollAndClick, pressAndHold, pressAndHoldWait, waitForElement, wait, "get/selenium/html"), byte[].class);


            if (response.getBody() != null) {
                try {
                    Document document = Jsoup.parse(new String(response.getBody(), StandardCharsets.UTF_8));



//                    ObjectMapper om = new ObjectMapper();
//                    JsonNode jNode = om.readValue(response.getBody(), JsonNode.class);
//                    if (jNode.has(""));

//                    html2Image(document.html());
//                    screenshotSelenium(document.html());

                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
            return ResponseEntity.ok(new String(response.getBody(), StandardCharsets.UTF_8));
//            return ResponseEntity.ok(responseEntity);
        } catch (Exception exception) {
            System.out.println(exception.getMessage());
            return ResponseEntity.badRequest().body(exception.getMessage());
        }
    }

    private void screenshotSelenium(String html) {
        System.setProperty("webdriver.chrome.driver", chromeDriver);
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--no-sandbox");
        options.addArguments("--start-maximized");
        options.addArguments("--headless");
        options.addArguments("--disable-gpu");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--accept-language=en-US,en;q=0.9");
        options.addArguments("--accept=*/*");
        options.addArguments("--accept-Encoding=gzip, deflate, br");
        options.addArguments("--connection=keep-alive");
        options.addArguments("--cache-Control=no-cache");

        WebDriver driver = new ChromeDriver(options);
        File htmlFile = new File("temp.html");
        try (FileWriter fileWriter = new FileWriter(htmlFile)) {
            //driver.get("about:blank");
//            ((JavascriptExecutor) driver).executeScript("document.write(arguments[0]);", html);
            fileWriter.write(html);
            driver.get(htmlFile.toURI().toURL().toString());
            waitForPageLoad(driver, 10000);
            scrollToBottom(driver);
            setFullscreen(driver);
            File screenshotFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            File outputScreenshot = new File("output.png");
            FileCopyUtils.copy(screenshotFile, outputScreenshot);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }
    }

    private void setFullscreen(WebDriver driver) {
        JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
        long pageHeight = (Long) jsExecutor.executeScript("return Math.max( document.body.scrollHeight, " +
                "document.body.offsetHeight, document.documentElement.clientHeight, " +
                "document.documentElement.scrollHeight, document.documentElement.offsetHeight );");
        driver.manage().window().setSize(new org.openqa.selenium.Dimension(1920, (int) pageHeight));
    }

    private void waitForPageLoad(WebDriver driver, int wait) {
        WebDriverWait webDriverWait = new WebDriverWait(driver, Duration.ofMillis(wait));
        ExpectedCondition<Boolean> jsLoadCondition = driver1 -> ((JavascriptExecutor) driver)
                .executeScript("return document.readyState").equals("complete");
        webDriverWait.until(jsLoadCondition);
    }

    private void scrollToBottom(WebDriver webDriver) {
        boolean scroll = true;
        Long startHeight;
        Long currentHeight;
        while (scroll) {
            JavascriptExecutor js = ((JavascriptExecutor) webDriver);
            startHeight = (Long) js.executeScript("return document.body.scrollHeight;");
            scrollDownSlowly(js);
            currentHeight = (Long) js.executeScript("return document.body.scrollHeight;");
            if (startHeight.equals(currentHeight)) {
                scroll = false;
            }
        }
    }

    private void scrollDownSlowly(JavascriptExecutor js) {
        js.executeScript(
                "function scrollToBottom() {" +
                        "   var currentPosition = window.scrollY;" +
                        "   var targetPosition = document.body.scrollHeight;" +
                        "   var increment = 10;" +
                        "   var speed = 20;" +
                        "   var timer = setInterval(function() {" +
                        "       if (currentPosition >= targetPosition) {" +
                        "           clearInterval(timer);" +
                        "       } else {" +
                        "           currentPosition += increment;" +
                        "           window.scrollTo(0, currentPosition);" +
                        "       }" +
                        "   }, speed);" +
                        "}" +
                        "scrollToBottom();"
        );
    }

    private void html2Image(String html) {
        try {
            int width = 2000;
            int height = 5500;
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            JEditorPane jep = new JEditorPane("text/html", html);
            jep.setPreferredSize(new Dimension(width, height));
            jep.setSize(jep.getPreferredSize());
            Graphics2D graphics2D = image.createGraphics();
            jep.paint(graphics2D);
            ImageIO.write(image, "png", new File("Image.png"));

            // Convert BufferedImage to bytes
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);

//            // Save the image to a file (optional)
//            File outputDir = new File("images");
//            outputDir.mkdirs();
//            String fileName = "output.png";
//            File outputFile = new File(outputDir, fileName);
//            ImageIO.write(image, "png", outputFile);
//
//            System.out.println("Image saved as " + outputFile.getPath() + "\nHTML Width: " + width + "px\nHTML Height: " + height + "px");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private String getFullUrl(String url) {
        return url.contains("www.amazon.com") ? url : "https://www.amazon.com" + url;
    }

    @GetMapping(path = "/get/site-data")
    public ResponseEntity<String> getAllDataFromCollector(
            @RequestParam(name = "ip")
            String ip,
            @RequestParam(name = "url")
            String url,
            @RequestParam(name = "scroll_to_bottom")
            Boolean scrollToBottom,
            @RequestParam(name = "chrome_arg", required = false)
            String chromeArg,
            @RequestParam(name = "hover_element", required = false)
            String hoverElement,
            @RequestParam(name = "scroll_and_click", required = false)
            String scrollAndClick,
            @RequestParam(required = false)
            String pressAndHold,
            @RequestParam(required = false)
            Integer pressAndHoldWait,
            @RequestParam(name = "wait_for_element", required = false)
            String waitForElement,
            @RequestParam(name = "wait", required = false)
            Integer wait) {
        try {
            RestTemplate rest = new RestTemplate();
//            ResponseEntity<SiteData> response = rest.getForEntity(getUrl(ip, url, scrollToBottom, chromeArg, hoverElement, scrollAndClick, waitForElement, wait, "get/site-data"), SiteData.class);
            ResponseEntity<SiteData> response = rest.getForEntity(getUrl(ip, url, scrollToBottom, chromeArg, hoverElement,
                    scrollAndClick, pressAndHold, pressAndHoldWait, waitForElement, wait, "get/playwright/site-data", null, null, null), SiteData.class);

            if (response.getBody() != null) {
                try {
                    SiteData sd = response.getBody();
                    Document document = Jsoup.parse(sd.getHtml());
                    System.out.println(sd.getScreenshotUrl());
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
            return ResponseEntity.ok(response.getBody().getHtml());
        } catch (Exception exception) {
            System.out.println(exception.getMessage());
            return ResponseEntity.badRequest().body(exception.getMessage());
        }
    }

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
//        hoverElement = hoverElement == null ? "" : "&hoverElement=" + fileHelper.base64UrlEncode("div[class='product-page__grid'] > div");
        hoverElement = hoverElement == null ? "" : "&hoverElement=" + fileHelper.base64UrlEncode("a[class='pcv3__info-content css-gwkf0u']");
        scrollAndClick = scrollAndClick == null ? "" : "&scrollAndClick=" + fileHelper.base64Encode("img[class^='_2ALz8ocn+G06z3rQ7vkfdg']");
        pressAndHold = pressAndHold == null ? "" : "&pressAndHold=" + fileHelper.base64Encode("button[id='didomi-notice-agree-button']");
        String pressAndHoldWaitStr = pressAndHoldWait == null ? "" : "&pressAndHoldWait=" + pressAndHoldWait;
        waitForElement = waitForElement == null ? "" : "&waitForElement=" + fileHelper.base64Encode("a[class='a-link-normal s-no-outline']");
//        searchBarSelector = searchBarSelector == null ? "" : "&searchBarSelector=" + fileHelper.base64Encode(searchBarSelector);
        searchBarSelector = searchBarSelector == null ? "" : "&searchBarSelector=" + fileHelper.base64Encode("div[class='rax-view searchbar-input-wrap'] > input");
        searchBarValue = searchBarValue == null ? "" : "&searchBarValue=" + fileHelper.base64Encode(searchBarValue);
//        searchButtonSelector = searchButtonSelector == null ? "" : "&searchButtonSelector=" + fileHelper.base64Encode(searchButtonSelector);
        searchButtonSelector = searchButtonSelector == null ? "" : "&searchButtonSelector=" + fileHelper.base64Encode("div[class='rax-view search-button']");
//        searchButtonSelector = searchButtonSelector == null ? "" : "&searchButtonSelector=" + fileHelper.base64Encode("#search-link");

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
//                + "&javascript=" + fileHelper.base64Encode("() => runParams")
                + "&javascript=" + fileHelper.base64Encode("() => _dida_config_._init_data_.data.data.root.fields")
                + "&crawlerLog=5439"
//                + "&cookie=" + fileHelper.base64Encode(".amazon.com")
//                + "&chromeArg=" + fileHelper.base64Encode("shopee")
                + "&multipleClick=" + fileHelper.base64Encode("div[class='x78zum5 xdt5ytf xg6iff7 x1n2onr6']")
                + "&wait=" + (wait != null ? wait : "8000");
    }

    @GetMapping(path = "/get/fb-photo")
    public ResponseEntity<List<FbData>> getFbPosts() {
        List<FbData> resp = new ArrayList<>();
        fbPosts.forEach(p -> {
            RestTemplate rest = new RestTemplate();
            ResponseEntity<SiteData> response = rest.getForEntity(getUrl("localhost", p, true,
                    null, null, "null", null, null,
                    null, 30000, "get/playwright/site-data",
                    null, null, null), SiteData.class);
            if (response.getBody() != null && response.getStatusCode().equals(HttpStatus.OK)) {
                try {
                    Document document = Jsoup.parse(response.getBody().getHtml());

                    Element descriptionElement = document.selectFirst("div[class='xdj266r x11i5rnm xat24cr x1mh8g0r x1vvkbs'] > div");
                    Element imgElement = document.selectFirst("div > div > img");
                    Element sellerElement = document.selectFirst("a[class='x1i10hfl xjbqb8w x6umtig x1b1mbwd xaqea5y xav7gou x9f619 x1ypdohk xt0psk2 xe8uvvx xdj266r x11i5rnm xat24cr x1mh8g0r xexx8yu x4uap5 x18d9i69 xkhd6sd x16tdsg8 x1hl2dhg xggy1nq x1a2a7pz xt0b8zv xzsf02u x1s688f']");

                    List<String> imgUrls = new ArrayList<>();
                    if (imgElement != null) {
                        imgUrls.add(imgElement.attr("src"));
                    } else {
                        imgElement = document.selectFirst("meta[property='og:image']");
                        if (imgElement != null) {
                            imgUrls.add(imgElement.attr("content"));
                        }
                    }

                    FbData fbData = new FbData();
                    fbData.setLink(p);
                    fbData.setDescription(descriptionElement != null ? descriptionElement.text() : "");
                    fbData.setImageUrls(imgUrls);
                    fbData.setSeller(sellerElement != null ? sellerElement.text() : "");
                    fbData.setSellerUrl(sellerElement != null ? sellerElement.attr("href") : "");
                    fbData.setScreenshotPath(response.getBody().getScreenshotUrl());
                    resp.add(fbData);
                    ObjectMapper om = new ObjectMapper();
                    System.out.println(om.writerWithDefaultPrettyPrinter().writeValueAsString(fbData) + ",");
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        });
        return ResponseEntity.ok(resp);
    }


    private List<String> fbPosts = Stream.of(
            ""
    ).toList();
}


