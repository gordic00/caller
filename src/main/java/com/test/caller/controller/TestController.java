package com.test.caller.controller;

import com.test.caller.helper.FileHelper;
import com.test.caller.model.SiteData;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@Validated
@RestController
@RequestMapping(path = "/api/v1/test")
@RequiredArgsConstructor
public class TestController {
    private final FileHelper fileHelper;

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
            @RequestParam(name = "xpath_expr", required = false)
            String xpathExpr,
            @RequestParam(name = "wait_for_element", required = false)
            String waitForElement,
            @RequestParam(name = "wait", required = false)
            Integer wait) {
        try {
            RestTemplate rest = new RestTemplate();
            ResponseEntity<byte[]> response = rest.getForEntity(getUrl(ip, url, scrollToBottom, chromeArg, hoverElement, xpathExpr, waitForElement, wait, "get/selenium/html"), byte[].class);

            if (response.getBody() != null) {
                try {
                    Document document = Jsoup.parse(new String(response.getBody(), StandardCharsets.UTF_8));

                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
            return ResponseEntity.ok(new String(response.getBody(), StandardCharsets.UTF_8));
        } catch (Exception exception) {
            System.out.println(exception.getMessage());
            return ResponseEntity.badRequest().body(exception.getMessage());
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
            @RequestParam(name = "xpath_expr", required = false)
            String xpathExpr,
            @RequestParam(name = "wait_for_element", required = false)
            String waitForElement,
            @RequestParam(name = "wait", required = false)
            Integer wait) {
        try {
            RestTemplate rest = new RestTemplate();
            ResponseEntity<SiteData> response = rest.getForEntity(getUrl(ip, url, scrollToBottom, chromeArg, hoverElement, xpathExpr, waitForElement, wait, "get/site-data"), SiteData.class);

            if (response.getBody() != null) {
                try {
                    SiteData sd = response.getBody();
                    Document document = Jsoup.parse(sd.getHtml());

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
            String xpathExpr,
            String waitForElement,
            Integer wait,
            String endpoint
    ) {
        chromeArg = chromeArg == null ? "" : "&chromeArg=" + fileHelper.base64Encode(chromeArg);
        hoverElement = hoverElement == null ? "" : "&hoverElement=" + fileHelper.base64UrlEncode(hoverElement);
        xpathExpr = xpathExpr == null ? "" : "&xpathExpr=" + fileHelper.base64Encode(xpathExpr);
        waitForElement = waitForElement == null ? "" : "&waitForElement=" + fileHelper.base64Encode(waitForElement);

        return "http://" + ip + ":8081" + "/api/v1/" + endpoint + "?url=" + fileHelper.base64UrlEncode(url)
                + (scrollToBottom ? "&scrollToBottom=true" : "&scrollToBottom=false")
                + chromeArg
                + hoverElement
                + xpathExpr
                + waitForElement
                + "&wait=" + (wait != null ? wait : "8000");
    }
}
