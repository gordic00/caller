package com.test.caller.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.caller.helper.FileHelper;
import com.test.caller.model.FbData;
import com.test.caller.model.FbProfile;
import com.test.caller.model.SiteData;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
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
import java.util.Objects;
import java.util.stream.Collectors;
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
            Integer wait,
            @RequestParam String endpoint) {
        try {
            RestTemplate rest = new RestTemplate();
            ResponseEntity<byte[]> response = rest.getForEntity(getUrl(ip, url, scrollToBottom, chromeArg, hoverElement,
                    scrollAndClick, pressAndHold, pressAndHoldWait, waitForElement, wait, endpoint,
                    searchBarSelector, searchBarValue, searchButtonSelector), byte[].class);

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
//        hoverElement = hoverElement == null ? "" : "&hoverElement=" + fileHelper.base64UrlEncode("div[id='section-good-desc']");
        hoverElement = hoverElement == null ? "" : "&hoverElement=" + fileHelper.base64UrlEncode("a[data-qaid='company_name']");
        scrollAndClick = scrollAndClick == null ? "" : "&scrollAndClick=" + fileHelper.base64Encode("button:has-text('Show more results')");
//        scrollAndClick = scrollAndClick == null ? "" : "&scrollAndClick=" + fileHelper.base64Encode("div[class='x92rtbv x10l6tqk x1tk7jg1 x1vjfegm'] > div > i[class='x1b0d499 x1d69dk1']"); // fb profile
        //press and hold tag for fb photo "div[class='x92rtbv x10l6tqk x1tk7jg1 x1vjfegm'] > div"
        pressAndHold = pressAndHold == null ? "" : "&pressAndHold=" + fileHelper.base64Encode("div[class='x92rtbv x10l6tqk x1tk7jg1 x1vjfegm'] > div");
        String pressAndHoldWaitStr = pressAndHoldWait == null ? "" : "&pressAndHoldWait=" + pressAndHoldWait;
        waitForElement = waitForElement == null ? "" : "&waitForElement=" + fileHelper.base64Encode("div[class='flex-auto flex-column  DXQgih']");
//        searchBarSelector = searchBarSelector == null ? "" : "&searchBarSelector=" + fileHelper.base64Encode(searchBarSelector);
        searchBarSelector = searchBarSelector == null ? "" : "&searchBarSelector=" + fileHelper.base64Encode("div[class='rax-view searchbar-input-wrap'] > input");
        searchBarValue = searchBarValue == null ? "" : "&searchBarValue=" + fileHelper.base64Encode(searchBarValue);
//        searchButtonSelector = searchButtonSelector == null ? "" : "&searchButtonSelector=" + fileHelper.base64Encode(searchButtonSelector);
        searchButtonSelector = searchButtonSelector == null ? "" : "&searchButtonSelector=" + fileHelper.base64Encode("div[class='rax-view search-button']");
//        searchButtonSelector = searchButtonSelector == null ? "" : "&searchButtonSelector=" + fileHelper.base64Encode("#search-link");

        String srbJs = "var nazivInput = document.getElementById(\"naziv\");\n" +
                       "nazivInput.value = \"andol\";\n" +
                       "var searchButton = document.getElementById(\"button\");\n" +
                       "searchButton.click();";

        String mnJs = """
                var nazivInput = document.getElementById("humani_registar_naziv");
                                nazivInput.value = "%s";
                                var searchButton = document.getElementsByName("humani_lijekovi_submit");
                                searchButton[0].click();""";

        String northM = """
                var nazivInput = document.getElementById("name");
                    nazivInput.value = "%s";
                    var searchButton = document.getElementById("submit_3");
                    searchButton.click();""";

        String bosnia = """
                var divElement = document.getElementById('MainContent_ReportGrid_ReportGrid_ItemsPerPage_0');
                if(divElement) {
                    var linkElement = divElement.querySelector('a:last-of-type');
                    if (linkElement) {
                        linkElement.click();
                    }
                }
                var nazivInput = document.getElementById("MainContent_txtNazivLijeka_txtInput")
                nazivInput.value = "panadol";
                var searchButton = document.getElementById("MainContent_btnSearch");
                searchButton.click();""";

        String s = """
                var linkElement = document.querySelector('a[href="javascript:__doPostBack(\\'ctl00$MainContent$ReportGrid$ctl09$ctl06\\',\\'\\')"');
                linkElement.click();""";

        String uk = """
                var searchField = document.getElementById("searchField");
                searchField.value = "panadol";
                var searchButton = document.getElementById("searchBtn");
                searchButton.click();
                """;

        String scroll = "() => { \" +\n" +
                        "                    \"  const scrollStep = window.innerHeight / 20; \" +\n" +
                        "                    \"  const scrollDuration = 500; \" +\n" +
                        "                    \"  function scroll() { \" +\n" +
                        "                    \"    if (window.scrollY < document.body.clientHeight - window.innerHeight) { \" +\n" +
                        "                    \"      window.scrollBy(0, scrollStep); \" +\n" +
                        "                    \"      requestAnimationFrame(scroll); \" +\n" +
                        "                    \"    } \" +\n" +
                        "                    \"  } \" +\n" +
                        "                    \"  scroll(); \" +\n" +
                        "                    \"}";

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
//                + "&javascript=" + fileHelper.base64Encode(uk)
//                + "&javascript=" + fileHelper.base64Encode(scroll)
//                + "&crawlerLog=5439"
               + "&scrollAndClickCount=3"
//                + "&cookie=" + fileHelper.base64Encode(".amazon.com")
//                + "&chromeArg=" + fileHelper.base64Encode("shopee")
//                + "&multipleClick=" + fileHelper.base64Encode("div[class='x78zum5 xdt5ytf xg6iff7 x1n2onr6']")
//                + "&initWait=5000"
               + "&wait=" + (wait != null ? wait : "8000");
    }

    @GetMapping(path = "/get/fb-photo")
    public ResponseEntity<List<FbData>> getFbPosts() {
        List<FbData> resp = new ArrayList<>();
        fbPosts.forEach(p -> {
            RestTemplate rest = new RestTemplate();
            try {
                ResponseEntity<SiteData> response = rest.getForEntity(getUrl("localhost", p, false,
                        null, null, "null", "yes", 2,
                        null, 30000, "get/playwright/site-data",
                        null, null, null), SiteData.class);
                if (response.getBody() != null && response.getStatusCode().equals(HttpStatus.OK)) {
                    Document document = Jsoup.parse(response.getBody().getHtml());

                    Element descriptionElement = document.selectFirst("div[class='xdj266r x11i5rnm xat24cr x1mh8g0r x1vvkbs'] > div");
                    Element imgElement = document.selectFirst("div > div > img");
                    Element sellerElement = document.selectFirst("span > span > a");
//                    Element sellerElement = document.selectFirst("a[class='x1i10hfl xjbqb8w x6umtig x1b1mbwd xaqea5y xav7gou x9f619 x1ypdohk xt0psk2 xe8uvvx xdj266r x11i5rnm xat24cr x1mh8g0r xexx8yu x4uap5 x18d9i69 xkhd6sd x16tdsg8 x1hl2dhg xggy1nq x1a2a7pz xt0b8zv xzsf02u x1s688f']");

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
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        });
        return ResponseEntity.ok(resp);
    }

    @GetMapping(path = "/get/fb-profile")
    public ResponseEntity<List<FbProfile>> getFbProfilePosts() {
        List<FbProfile> resp = new ArrayList<>();
        for (String profile : profileUrls) {
            RestTemplate rest = new RestTemplate();
            try {
                ResponseEntity<byte[]> response = rest.getForEntity(getUrl("localhost", profile, true,
                        null, "yes", "null", "yes", 2,
                        null, 30000, "get/playwright/html",
                        null, null, null), byte[].class);

                if (response.getBody() != null && response.getStatusCode().equals(HttpStatus.OK)) {
                    try {
                        FbProfile fbProfile = new FbProfile();
                        fbProfile.setUrl(profile);
                        Document document = Jsoup.parse(new String(response.getBody(), StandardCharsets.UTF_8));
                        Elements postUrls = document.select("div[class='x6ikm8r x10wlt62 x10l6tqk'] > a");
                        postUrls.forEach(p -> fbProfile.getPostUrls().add(p.attr("href")));

                        resp.add(fbProfile);
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        return ResponseEntity.ok(resp);
    }

    private List<String> profileUrls = Stream.of(
            "https://www.facebook.com/loro.piana.18"
    ).toList();

    private List<String> fbPosts = Stream.of(
            "https://www.facebook.com/photo/?fbid=305017695872481&set=pb.100090927594318.-2207520000",
            "https://www.facebook.com/photo/?fbid=305017692539148&set=pb.100090927594318.-2207520000",
            "https://www.facebook.com/photo/?fbid=353930544197497&set=gm.2114823982231512&idorvanity=564506430596616",
            "https://www.facebook.com/photo.php?fbid=236559279511934&set=pb.100094735910437.-2207520000&type=3",
            "https://www.facebook.com/photo?fbid=236559242845271&set=pb.100094735910437.-2207520000",
            "https://www.facebook.com/photo?fbid=236559166178612&set=pb.100094735910437.-2207520000",
            "https://www.facebook.com/photo.php?fbid=225444850623377&set=pb.100094735910437.-2207520000&type=3",
            "https://www.facebook.com/photo?fbid=225444807290048&set=pb.100094735910437.-2207520000",
            "https://www.facebook.com/photo?fbid=225444737290055&set=pb.100094735910437.-2207520000",
            "https://www.facebook.com/Motoparts.KPK/photos/pb.100067854994485.-2207520000/1357779794712967/?type=3",
            "https://www.facebook.com/Motoparts.KPK/photos/pb.100067854994485.-2207520000/1230121600812121/?type=3",
            "https://www.facebook.com/Motoparts.KPK/photos/pb.100067854994485.-2207520000/1182496618907953/?type=3",
            "https://www.facebook.com/photo.php?fbid=773086831513888&set=pb.100064377351703.-2207520000&type=3",
            "https://www.facebook.com/photo/?fbid=773086794847225&set=pb.100064377351703.-2207520000",
            "https://www.facebook.com/photo?fbid=773086758180562&set=pb.100064377351703.-2207520000",
            "https://www.facebook.com/photo?fbid=773086714847233&set=pb.100064377351703.-2207520000",
            "https://www.facebook.com/photo?fbid=773086678180570&set=pb.100064377351703.-2207520000",
            "https://www.facebook.com/photo?fbid=773086641513907&set=pb.100064377351703.-2207520000",
            "https://www.facebook.com/photo?fbid=773086604847244&set=pb.100064377351703.-2207520000",
            "https://www.facebook.com/photo.php?fbid=766626818826556&set=pb.100064377351703.-2207520000&type=3",
            "https://www.facebook.com/photo.php?fbid=742932591195979&set=pb.100064377351703.-2207520000&type=3",
            "https://www.facebook.com/photo.php?fbid=740610454761526&set=pb.100064377351703.-2207520000&type=3",
            "https://www.facebook.com/photo.php?fbid=740610564761515&set=pb.100064377351703.-2207520000&type=3",
            "https://www.facebook.com/photo/?fbid=740610534761518&set=pb.100064377351703.-2207520000",
            "https://www.facebook.com/photo/?fbid=740610491428189&set=pb.100064377351703.-2207520000",
            "https://www.facebook.com/photo.php?fbid=737582521730986&set=pb.100064377351703.-2207520000&type=3",
            "https://www.facebook.com/photo/?fbid=737582491730989&set=pb.100064377351703.-2207520000",
            "https://www.facebook.com/photo/?fbid=737582465064325&set=pb.100064377351703.-2207520000",
            "https://www.facebook.com/photo/?fbid=737582441730994&set=pb.100064377351703.-2207520000",
            "https://www.facebook.com/photo/?fbid=737582401730998&set=pb.100064377351703.-2207520000",
            "https://www.facebook.com/photo/?fbid=737582365064335&set=pb.100064377351703.-2207520000",
            "https://www.facebook.com/photo/?fbid=737019718453933&set=pb.100064377351703.-2207520000",
            "https://www.facebook.com/photo/?fbid=737019658453939&set=pb.100064377351703.-2207520000",
            "https://www.facebook.com/photo/?fbid=737019658453939&set=pb.100064377351703.-2207520000",
            "https://www.facebook.com/photo/?fbid=737019581787280&set=pb.100064377351703.-2207520000",
            "https://www.facebook.com/photo/?fbid=737019551787283&set=pb.100064377351703.-2207520000",
            "https://www.facebook.com/photo/?fbid=737019521787286&set=pb.100064377351703.-2207520000",
            "https://www.facebook.com/photo.php?fbid=122106046616198948&set=pb.61555968459155.-2207520000&type=3",
            "https://www.facebook.com/photo/?fbid=7757168177630620&set=pcb.3596297537355940",
            "https://www.facebook.com/photo?fbid=7757168167630621&set=pcb.3596297537355940",
            "https://www.facebook.com/photo/?fbid=7757168160963955&set=pcb.3596297537355940",
            "https://www.facebook.com/photo/?fbid=6381394135208038&set=pcb.3324798521172511",
            "https://www.facebook.com/photo?fbid=6381394221874696&set=pcb.3324798521172511",
            "https://www.facebook.com/photo?fbid=6381394308541354&set=pcb.3324798521172511",
            "https://www.facebook.com/photo?fbid=6381394375208014&set=pcb.3324798521172511",
            "https://www.facebook.com/photo?fbid=6381394458541339&set=pcb.3324798521172511",
            "https://www.facebook.com/photo/?fbid=3548319828515497&set=pcb.2610391329279904",
            "https://www.facebook.com/photo?fbid=3548319901848823&set=pcb.2610391329279904",
            "https://www.facebook.com/photo/?fbid=2050575201984535&set=pcb.3614782672174093",
            "https://www.facebook.com/photo?fbid=2050575195317869&set=pcb.3614782672174093",
            "https://www.facebook.com/photo?fbid=2050575198651202&set=pcb.3614782672174093",
            "https://www.facebook.com/photo?fbid=2050575205317868&set=pcb.3614782672174093",
            "https://www.facebook.com/photo.php?fbid=585945553228003&set=pb.100054376906871.-2207520000&type=3",
            "https://www.facebook.com/photo/?fbid=2935494656709622&set=pb.100054376906871.-2207520000",
            "https://www.facebook.com/photo/?fbid=2839689369623485&set=pb.100054376906871.-2207520000",
            "https://www.facebook.com/photo/?fbid=2839689269623495&set=pb.100054376906871.-2207520000",
            "https://www.facebook.com/photo/?fbid=2839689226290166&set=pb.100054376906871.-2207520000",
            "https://www.facebook.com/2251161578476270/photos/pb.100054376906871.-2207520000/2251162831809478/?type=3",
            "https://www.facebook.com/photo.php?fbid=441187908113215&set=pb.100066660291421.-2207520000&type=3",
            "https://www.facebook.com/photo/?fbid=107324997502048&set=pb.100066660291421.-2207520000",
            "https://www.facebook.com/photo/?fbid=107324904168724&set=pb.100066660291421.-2207520000",
            "https://www.facebook.com/photo?fbid=107324464168768&set=pb.100066660291421.-2207520000",
            "https://www.facebook.com/107318817502666/photos/pb.100066660291421.-2207520000/107324440835437/?type=3",
            "https://www.facebook.com/photo?fbid=107324407502107&set=pb.100066660291421.-2207520000",
            "https://www.facebook.com/sumbervariasibatam/photos/pb.100063995773997.-2207520000/215070890422805/?type=3&locale=ms_MY",
            "https://www.facebook.com/sumbervariasibatam/photos/pb.100063995773997.-2207520000/206354131294481/?type=3&locale=ms_MY",
            "https://www.facebook.com/sumbervariasibatam/photos/pb.100063995773997.-2207520000/206353557961205/?type=3&locale=ms_MY",
            "https://www.facebook.com/sumbervariasibatam/photos/pb.100063995773997.-2207520000/206353537961207/?type=3&locale=ms_MY",
            "https://www.facebook.com/sumbervariasibatam/photos/pb.100063995773997.-2207520000/185397973390097/?type=3&locale=ms_MY",
            "https://www.facebook.com/sumbervariasibatam/photos/pb.100063995773997.-2207520000/185397860056775/?type=3&locale=ms_MY",
            "https://www.facebook.com/sumbervariasibatam/photos/pb.100063995773997.-2207520000/173094867953741/?type=3&locale=ms_MY",
            "https://www.facebook.com/sumbervariasibatam/photos/pb.100063995773997.-2207520000/173094497953778/?type=3&locale=ms_MY",
            "https://www.facebook.com/photo/?fbid=173094481287113&set=pb.100063995773997.-2207520000&locale=ms_MY",
            "https://www.facebook.com/photo/?fbid=173093201287241&set=pb.100063995773997.-2207520000&locale=ms_MY",
            "https://www.facebook.com/photo/?fbid=173093164620578&set=pb.100063995773997.-2207520000&locale=ms_MY",
            "https://www.facebook.com/photo/?fbid=173093134620581&set=pb.100063995773997.-2207520000&locale=ms_MY",
            "https://www.facebook.com/photo/?fbid=173093094620585&set=pb.100063995773997.-2207520000&locale=ms_MY",
            "https://www.facebook.com/photo/?fbid=173093064620588&set=pb.100063995773997.-2207520000&locale=ms_MY",
            "https://www.facebook.com/sumbervariasibatam/photos/pb.100063995773997.-2207520000/158592959403932/?type=3&locale=ms_MY",
            "https://www.facebook.com/photo/?fbid=158592939403934&set=pb.100063995773997.-2207520000&locale=ms_MY",
            "https://www.facebook.com/photo/?fbid=158592916070603&set=pb.100063995773997.-2207520000&locale=ms_MY",
            "https://www.facebook.com/sumbervariasibatam/photos/pb.100063995773997.-2207520000/158592862737275/?type=3&locale=ms_MY",
            "https://www.facebook.com/photo/?fbid=158592766070618&set=pb.100063995773997.-2207520000&locale=ms_MY",
            "https://www.facebook.com/photo/?fbid=158229472773614&set=pb.100063995773997.-2207520000&locale=ms_MY",
            "https://www.facebook.com/photo/?fbid=158229449440283&set=pb.100063995773997.-2207520000&locale=ms_MY",
            "https://www.facebook.com/photo/?fbid=158229429440285&set=pb.100063995773997.-2207520000&locale=ms_MY",
            "https://www.facebook.com/sumbervariasibatam/photos/pb.100063995773997.-2207520000/140966807833214/?type=3&locale=ms_MY",
            "https://www.facebook.com/sumbervariasibatam/photos/pb.100063995773997.-2207520000/112365074026721/?type=3&locale=ms_MY",
            "https://www.facebook.com/sumbervariasibatam/photos/pb.100063995773997.-2207520000/112245930705302/?type=3&locale=ms_MY",
            "https://www.facebook.com/sumbervariasibatam/photos/pb.100063995773997.-2207520000/112242390705656/?type=3&locale=ms_MY",
            "https://www.facebook.com/sumbervariasibatam/photos/pb.100063995773997.-2207520000/110113964251832/?type=3&locale=ms_MY",
            "https://www.facebook.com/photo/?fbid=110113917585170&set=pb.100063995773997.-2207520000&locale=ms_MY",
            "https://www.facebook.com/photo/?fbid=110113890918506&set=pb.100063995773997.-2207520000&locale=ms_MY",
            "https://www.facebook.com/photo/?fbid=110113840918511&set=pb.100063995773997.-2207520000&locale=ms_MY",
            "https://www.facebook.com/photo/?fbid=110113794251849&set=pb.100063995773997.-2207520000&locale=ms_MY",
            "https://www.facebook.com/sumbervariasibatam/photos/pb.100063995773997.-2207520000/110113584251870/?type=3&locale=ms_MY",
            "https://www.facebook.com/photo/?fbid=110113497585212&set=pb.100063995773997.-2207520000&locale=ms_MY",
            "https://www.facebook.com/sumbervariasibatam/photos/pb.100063995773997.-2207520000/109783814284847/?type=3&locale=ms_MY",
            "https://www.facebook.com/sumbervariasibatam/photos/pb.100063995773997.-2207520000/109783774284851/?type=3&locale=ms_MY",
            "https://www.facebook.com/photo/?fbid=109531700976725&set=pb.100063995773997.-2207520000&locale=ms_MY",
            "https://www.facebook.com/photo/?fbid=109504054312823&set=pb.100063995773997.-2207520000&locale=ms_MY",
            "https://www.facebook.com/sumbervariasibatam/photos/pb.100063995773997.-2207520000/106490371280858/?type=3&locale=ms_MY",
            "https://www.facebook.com/photo/?fbid=106490344614194&set=pb.100063995773997.-2207520000&locale=ms_MY",
            "https://www.facebook.com/photo/?fbid=106490311280864&set=pb.100063995773997.-2207520000&locale=ms_MY",
            "https://www.facebook.com/photo/?fbid=106490274614201&set=pb.100063995773997.-2207520000&locale=ms_MY",
            "https://www.facebook.com/sumbervariasibatam/photos/pb.100063995773997.-2207520000/101216298474932/?type=3&locale=ms_MY",
            "https://www.facebook.com/photo/?fbid=101216275141601&set=pb.100063995773997.-2207520000&locale=ms_MY",
            "https://www.facebook.com/sumbervariasibatam/photos/pb.100063995773997.-2207520000/101216228474939/?type=3&locale=ms_MY",
            "https://www.facebook.com/photo/?fbid=101216205141608&set=pb.100063995773997.-2207520000&locale=ms_MY",
            "https://www.facebook.com/sumbervariasibatam/photos/pb.100063995773997.-2207520000/100583691871526/?type=3&locale=ms_MY",
            "https://www.facebook.com/photo/?fbid=100583671871528&set=pb.100063995773997.-2207520000&locale=ms_MY",
            "https://www.facebook.com/photo/?fbid=100583448538217&set=pb.100063995773997.-2207520000&locale=ms_MY",
            "https://www.facebook.com/photo/?fbid=100583225204906&set=pb.100063995773997.-2207520000&locale=ms_MY",
            "https://www.facebook.com/photo/?fbid=100583201871575&set=pb.100063995773997.-2207520000&locale=ms_MY",
            "https://www.facebook.com/photo/?fbid=100583178538244&set=pb.100063995773997.-2207520000&locale=ms_MY",
            "https://www.facebook.com/photo?fbid=100583161871579&set=pb.100063995773997.-2207520000&locale=ms_MY",
            "https://www.facebook.com/photo?fbid=100583135204915&set=pb.100063995773997.-2207520000&locale=ms_MY",
            "https://www.facebook.com/photo?fbid=100583105204918&set=pb.100063995773997.-2207520000&locale=ms_MY",
            "https://www.facebook.com/photo?fbid=100583085204920&set=pb.100063995773997.-2207520000&locale=ms_MY",
            "https://www.facebook.com/photo?fbid=100583045204924&set=pb.100063995773997.-2207520000&locale=ms_MY",
            "https://www.facebook.com/sumbervariasibatam/videos/363068028278811?locale=ms_MY",
            "https://www.facebook.com/sumbervariasibatam/videos/3782596741773140?locale=ms_MY",
            "https://www.facebook.com/marketplace/item/322040510680968/?ref=category_feed&referral_code=null&referral_story_type=post&tracking=browse_serp%3A9de49b89-f2af-47c4-987a-a7593395740a",
            "https://www.facebook.com/marketplace/item/1685571885302264/?ref=category_feed&referral_code=null&referral_story_type=post&tracking=browse_serp%3A9de49b89-f2af-47c4-987a-a7593395740a",
            "https://www.facebook.com/marketplace/item/890985679045407/?ref=category_feed&referral_code=null&referral_story_type=post&tracking=browse_serp%3A9de49b89-f2af-47c4-987a-a7593395740a",
            "8https://www.facebook.com/marketplace/item/890985679045407/?ref=category_feed&referral_code=null&referral_story_type=post&tracking=browse_serp%3A9de49b89-f2af-47c4-987a-a7593395740a",
            "https://www.facebook.com/marketplace/item/890985679045407/?ref=category_feed&referral_code=null&referral_story_type=post&tracking=browse_serp%3A9de49b89-f2af-47c4-987a-a7593395740a",
            "https://www.facebook.com/marketplace/item/890985679045407/?ref=category_feed&referral_code=null&referral_story_type=post&tracking=browse_serp%3A9de49b89-f2af-47c4-987a-a7593395740a",
            "https://www.facebook.com/marketplace/item/2044508509266370/?ref=category_feed&referral_code=null&referral_story_type=post&tracking=browse_serp%3A9de49b89-f2af-47c4-987a-a7593395740a",
            "https://www.facebook.com/photo.php?fbid=122118920414128173&set=pb.61553845207477.-2207520000&type=3",
            "https://www.facebook.com/photo/?fbid=122115969830128173&set=pb.61553845207477.-2207520000",
            "https://www.facebook.com/photo/?fbid=122115969728128173&set=pb.61553845207477.-2207520000",
            "https://www.facebook.com/photo/?fbid=122115969680128173&set=pb.61553845207477.-2207520000",
            "https://www.facebook.com/photo/?fbid=122115969632128173&set=pb.61553845207477.-2207520000",
            "https://www.facebook.com/photo/?fbid=122115969578128173&set=pb.61553845207477.-2207520000",
            "https://www.facebook.com/photo/?fbid=122115969524128173&set=pb.61553845207477.-2207520000",
            "https://www.facebook.com/photo/?fbid=122115969464128173&set=pb.61553845207477.-2207520000",
            "https://www.facebook.com/photo/?fbid=122115969416128173&set=pb.61553845207477.-2207520000",
            "https://www.facebook.com/photo/?fbid=122115969314128173&set=pb.61553845207477.-2207520000",
            "https://www.facebook.com/photo/?fbid=122115969212128173&set=pb.61553845207477.-2207520000",
            "https://www.facebook.com/photo/?fbid=122115969068128173&set=pb.61553845207477.-2207520000",
            "https://www.facebook.com/photo/?fbid=122115968924128173&set=pb.61553845207477.-2207520000",
            "https://www.facebook.com/photo.php?fbid=122113940402128173&set=pb.61553845207477.-2207520000&type=3",
            "https://www.facebook.com/photo.php?fbid=122113223120128173&set=pb.61553845207477.-2207520000&type=3",
            "https://www.facebook.com/photo/?fbid=122113223066128173&set=pb.61553845207477.-2207520000",
            "https://www.facebook.com/photo/?fbid=122113223024128173&set=pb.61553845207477.-2207520000",
            "https://www.facebook.com/photo/?fbid=122113222970128173&set=pb.61553845207477.-2207520000",
            "https://www.facebook.com/photo/?fbid=122113222922128173&set=pb.61553845207477.-2207520000",
            "https://www.facebook.com/photo/?fbid=122113222862128173&set=pb.61553845207477.-2207520000",
            "https://www.facebook.com/photo/?fbid=122113222802128173&set=pb.61553845207477.-2207520000",
            "https://www.facebook.com/photo/?fbid=122113222664128173&set=pb.61553845207477.-2207520000",
            "https://www.facebook.com/photo/?fbid=122113222616128173&set=pb.61553845207477.-2207520000",
            "https://www.facebook.com/photo/?fbid=122113222568128173&set=pb.61553845207477.-2207520000",
            "https://www.facebook.com/photo.php?fbid=122111261132128173&set=pb.61553845207477.-2207520000&type=3",
            "https://www.facebook.com/1923634144429740/photos/pb.100067997943747.-2207520000/2335415543251596/?type=3",
            "https://www.facebook.com/1923634144429740/photos/pb.100067997943747.-2207520000/2335415466584937/?type=3",
            "https://www.facebook.com/1923634144429740/photos/pb.100067997943747.-2207520000/2328056573987493/?type=3",
            "https://www.facebook.com/photo/?fbid=2328056520654165&set=pb.100067997943747.-2207520000",
            "https://www.facebook.com/photo/?fbid=2328056457320838&set=pb.100067997943747.-2207520000",
            "https://www.facebook.com/photo.php?fbid=921950712880820&set=pb.100051976999271.-2207520000&type=3",
            "https://www.facebook.com/photo/?fbid=921950709547487&set=pb.100051976999271.-2207520000",
            "https://www.facebook.com/photo.php?fbid=913717957037429&set=pb.100051976999271.-2207520000&type=3",
            "https://www.facebook.com/photo/?fbid=913717953704096&set=pb.100051976999271.-2207520000",
            "https://www.facebook.com/photo/?fbid=913717950370763&set=pb.100051976999271.-2207520000",
            "https://www.facebook.com/photo/?fbid=913717947037430&set=pb.100051976999271.-2207520000",
            "https://www.facebook.com/photo.php?fbid=875726390836586&set=pb.100051976999271.-2207520000&type=3",
            "https://www.facebook.com/pratamavariasimobilmakassar/photos/pb.100051976999271.-2207520000/2365214793625405/?type=3",
            "https://www.facebook.com/pratamavariasimobilmakassar/photos/pb.100051976999271.-2207520000/2205366159610270/?type=3",
            "https://www.facebook.com/pratamavariasimobilmakassar/photos/pb.100051976999271.-2207520000/2054974837982737/?type=3",
            "https://www.facebook.com/pratamavariasimobilmakassar/photos/pb.100051976999271.-2207520000/2052866384860249/?type=3",
            "https://www.facebook.com/114188745926073/photos/pb.100066411715162.-2207520000/246362952708651/?type=3",
            "https://www.facebook.com/photo/?fbid=246362369375376&set=pb.100066411715162.-2207520000",
            "https://www.facebook.com/114188745926073/photos/pb.100066411715162.-2207520000/114207615924186/?type=3",
            "https://www.facebook.com/114188745926073/photos/pb.100066411715162.-2207520000/114207555924192/?type=3",
            "https://www.facebook.com/bgvariasi/photos/pb.100058721715510.-2207520000/3249463705076092/?type=3",
            "https://www.facebook.com/bgvariasi/photos/pb.100058721715510.-2207520000/2064385700250571/?type=3",
            "https://www.facebook.com/bgvariasi/photos/pb.100058721715510.-2207520000/1155112994511184/?type=3",
            "https://www.facebook.com/bgvariasi/photos/pb.100058721715510.-2207520000/1021545357867949/?type=3",
            "https://www.facebook.com/photo.php?fbid=747673084050473&set=pb.100064233631920.-2207520000&type=3&locale=en_GB",
            "https://www.facebook.com/photo.php?fbid=729808625836919&set=pb.100064233631920.-2207520000&type=3&locale=en_GB",
            "https://www.facebook.com/photo.php?fbid=724113749739740&set=pb.100064233631920.-2207520000&type=3&locale=en_GB",
            "https://www.facebook.com/photo.php?fbid=720318550119260&set=pb.100064233631920.-2207520000&type=3&locale=en_GB",
            "https://www.facebook.com/Proformax/photos/pb.100064233631920.-2207520000/1146625609302221/?type=3&locale=en_GB",
            "https://www.facebook.com/Proformax/photos/pb.100064233631920.-2207520000/928894517741999/?type=3&locale=en_GB",
            "https://www.facebook.com/Proformax/photos/pb.100064233631920.-2207520000/744401612857958/?type=3&locale=en_GB",
            "https://www.facebook.com/Proformax/photos/pb.100064233631920.-2207520000/732476870717099/?type=3&locale=en_GB",
            "https://www.facebook.com/Proformax/photos/pb.100064233631920.-2207520000/666235930674527/?type=3&locale=en_GB",
            "https://www.facebook.com/Proformax/photos/pb.100064233631920.-2207520000/509581186340003/?type=3&locale=en_GB",
            "https://www.facebook.com/Proformax/photos/pb.100064233631920.-2207520000/485170465447742/?type=3&locale=en_GB",
            "https://www.facebook.com/Proformax/photos/pb.100064233631920.-2207520000/331002737531183/?type=3&locale=en_GB",
            "https://www.facebook.com/Proformax/photos/pb.100064233631920.-2207520000/296959647602159/?type=3&locale=en_GB",
            "https://www.facebook.com/photo/?fbid=296959414268849&set=pb.100064233631920.-2207520000&locale=en_GB",
            "https://www.facebook.com/Proformax/photos/pb.100064233631920.-2207520000/283483585616432/?type=3&locale=en_GB",
            "https://www.facebook.com/Proformax/photos/pb.100064233631920.-2207520000/274867483144709/?type=3&locale=en_GB",
            "https://www.facebook.com/Proformax/photos/pb.100064233631920.-2207520000/274839673147490/?type=3&locale=en_GB",
            "https://www.facebook.com/photo.php?fbid=463162439167811&set=pb.100064221756354.-2207520000&type=3",
            "https://www.facebook.com/photo/?fbid=463162435834478&set=pb.100064221756354.-2207520000",
            "https://www.facebook.com/101665918614229/photos/pb.100064221756354.-2207520000/417199350394216/?type=3",
            "https://www.facebook.com/photo/?fbid=417199347060883&set=pb.100064221756354.-2207520000",
            "https://www.facebook.com/101665918614229/photos/pb.100064221756354.-2207520000/406015311512620/?type=3",
            "https://www.facebook.com/101665918614229/photos/pb.100064221756354.-2207520000/404897758291042/?type=3",
            "https://www.facebook.com/photo/?fbid=404897754957709&set=pb.100064221756354.-2207520000",
            "https://www.facebook.com/photo/?fbid=404897751624376&set=pb.100064221756354.-2207520000",
            "https://www.facebook.com/photo/?fbid=404897748291043&set=pb.100064221756354.-2207520000",
            "https://www.facebook.com/101665918614229/photos/pb.100064221756354.-2207520000/404220671692084/?type=3",
            "https://www.facebook.com/photo/?fbid=404220668358751&set=pb.100064221756354.-2207520000",
            "https://www.facebook.com/101665918614229/photos/pb.100064221756354.-2207520000/399764142137737/?type=3",
            "https://www.facebook.com/101665918614229/photos/pb.100064221756354.-2207520000/394334382680713/?type=3",
            "https://www.facebook.com/photo/?fbid=394334379347380&set=pb.100064221756354.-2207520000",
            "https://www.facebook.com/photo/?fbid=394334376014047&set=pb.100064221756354.-2207520000",
            "https://www.facebook.com/photo/?fbid=394334372680714&set=pb.100064221756354.-2207520000",
            "https://www.facebook.com/101665918614229/photos/pb.100064221756354.-2207520000/392575986189886/?type=3",
            "https://www.facebook.com/101665918614229/photos/pb.100064221756354.-2207520000/382776983836453/?type=3",
            "https://www.facebook.com/101665918614229/photos/pb.100064221756354.-2207520000/382731263841025/?type=3",
            "https://www.facebook.com/101665918614229/photos/pb.100064221756354.-2207520000/379727044141447/?type=3",
            "https://www.facebook.com/101665918614229/photos/pb.100064221756354.-2207520000/377196337727851/?type=3",
            "https://www.facebook.com/101665918614229/photos/pb.100064221756354.-2207520000/377195561061262/?type=3",
            "https://www.facebook.com/photo/?fbid=377195777727907&set=pb.100064221756354.-2207520000",
            "https://www.facebook.com/101665918614229/photos/pb.100064221756354.-2207520000/377192784394873/?type=3",
            "https://www.facebook.com/photo/?fbid=377192781061540&set=pb.100064221756354.-2207520000",
            "https://www.facebook.com/101665918614229/photos/pb.100064221756354.-2207520000/377158991064919/?type=3",
            "https://www.facebook.com/photo/?fbid=377158987731586&set=pb.100064221756354.-2207520000",
            "https://www.facebook.com/photo/?fbid=377158984398253&set=pb.100064221756354.-2207520000",
            "https://www.facebook.com/photo/?fbid=377153491065469&set=pb.100064221756354.-2207520000",
            "https://www.facebook.com/photo/?fbid=377153487732136&set=pb.100064221756354.-2207520000",
            "https://www.facebook.com/101665918614229/photos/pb.100064221756354.-2207520000/370996075014544/?type=3",
            "https://www.facebook.com/101665918614229/photos/pb.100064221756354.-2207520000/367466962034122/?type=3",
            "https://www.facebook.com/101665918614229/photos/pb.100064221756354.-2207520000/348641290583356/?type=3",
            "https://www.facebook.com/photo/?fbid=348641287250023&set=pb.100064221756354.-2207520000",
            "https://www.facebook.com/photo/?fbid=348641283916690&set=pb.100064221756354.-2207520000",
            "https://www.facebook.com/photo/?fbid=348641280583357&set=pb.100064221756354.-2207520000",
            "https://www.facebook.com/101665918614229/photos/pb.100064221756354.-2207520000/340236571423828/?type=3",
            "https://www.facebook.com/101665918614229/photos/pb.100064221756354.-2207520000/336194941827991/?type=3",
            "https://www.facebook.com/101665918614229/photos/pb.100064221756354.-2207520000/322127943234691/?type=3",
            "https://www.facebook.com/101665918614229/photos/pb.100064221756354.-2207520000/320124946768324/?type=3",
            "https://www.facebook.com/101665918614229/photos/pb.100064221756354.-2207520000/320124943434991/?type=3",
            "https://www.facebook.com/photo/?fbid=320124940101658&set=pb.100064221756354.-2207520000",
            "https://www.facebook.com/101665918614229/photos/pb.100064221756354.-2207520000/320122823435203/?type=3",
            "https://www.facebook.com/101665918614229/photos/pb.100064221756354.-2207520000/320122820101870/?type=3",
            "https://www.facebook.com/101665918614229/photos/pb.100064221756354.-2207520000/315412003906285/?type=3",
            "https://www.facebook.com/photo/?fbid=315412000572952&set=pb.100064221756354.-2207520000",
            "https://www.facebook.com/101665918614229/photos/pb.100064221756354.-2207520000/314300077350811/?type=3",
            "https://www.facebook.com/101665918614229/photos/pb.100064221756354.-2207520000/305216114925874/?type=3",
            "https://www.facebook.com/101665918614229/photos/pb.100064221756354.-2207520000/305216108259208/?type=3",
            "https://www.facebook.com/101665918614229/photos/pb.100064221756354.-2207520000/302707121843440/?type=3",
            "https://www.facebook.com/101665918614229/photos/pb.100064221756354.-2207520000/300995285347957/?type=3",
            "https://www.facebook.com/101665918614229/photos/pb.100064221756354.-2207520000/295734859207333/?type=3",
            "https://www.facebook.com/101665918614229/photos/pb.100064221756354.-2207520000/291180039662815/?type=3",
            "https://www.facebook.com/photo/?fbid=291180036329482&set=pb.100064221756354.-2207520000",
            "https://www.facebook.com/101665918614229/photos/pb.100064221756354.-2207520000/290249379755881/?type=3",
            "https://www.facebook.com/101665918614229/photos/pb.100064221756354.-2207520000/290249376422548/?type=3"
    ).toList();
}


