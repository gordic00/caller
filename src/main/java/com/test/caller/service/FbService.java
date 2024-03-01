package com.test.caller.service;

import com.test.caller.helper.FileHelper;
import com.test.caller.model.FbData;
import com.test.caller.model.FbProfile;
import com.test.caller.model.SiteData;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class FbService {
    private final FileHelper fileHelper;
    public ResponseEntity<List<FbProfile>> getFbProfile() {
        List<FbProfile> resp = new ArrayList<>();
        for (String profile : profileUrls) {
            RestTemplate rest = new RestTemplate();
            ResponseEntity<SiteData> response = rest.getForEntity(getUrl("localhost", profile, false,
                    null, null, "null", "yes", 2,
                    null, 30000, "get/playwright/site-data",
                    null, null, null), SiteData.class);

            if (response.getBody() != null && response.getStatusCode().equals(HttpStatus.OK)) {
                try {
                    FbProfile fbProfile = new FbProfile();
                    fbProfile.setUrl(profile);

                    Document document = Jsoup.parse(response.getBody().getHtml());
                    Elements postUrls = document.select("div[class='x6ikm8r x10wlt62 x10l6tqk'] > a");
                    postUrls.forEach(p -> fbProfile.getPostUrls().add(p.attr("href")));

                    resp.add(fbProfile);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        }
        return ResponseEntity.ok(resp);
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
        hoverElement = hoverElement == null ? "" : "&hoverElement=" + fileHelper.base64UrlEncode("div[class='iva-item-title-py3i_'] > a");
        scrollAndClick = scrollAndClick == null ? "" : "&scrollAndClick=" + fileHelper.base64Encode("div[class='D_MM'] > div > button");
        pressAndHold = pressAndHold == null ? "" : "&pressAndHold=" + fileHelper.base64Encode("div[class='_3fKiu5wx _3zN5SumS _3tAK973O IYOfhWEs VGNGF1pA'] > div > span");
        String pressAndHoldWaitStr = pressAndHoldWait == null ? "" : "&pressAndHoldWait=" + pressAndHoldWait;
        waitForElement = waitForElement == null ? "" : "&waitForElement=" + fileHelper.base64Encode("div[class='box__image'] > a");
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
                + "&scrollAndClickCount=5"
//                + "&cookie=" + fileHelper.base64Encode(".amazon.com")
//                + "&chromeArg=" + fileHelper.base64Encode("shopee")
                + "&multipleClick=" + fileHelper.base64Encode("div[class='x78zum5 xdt5ytf xg6iff7 x1n2onr6']")
                + "&wait=" + (wait != null ? wait : "8000");
    }

    private List<String> profileUrls = Stream.of(
            "https://www.facebook.com/loro.piana.18",
            "https://www.facebook.com/loro.piana.54",
            "https://www.facebook.com/loro.piana.58",
            "https://www.facebook.com/Lorococo235",
            "https://www.facebook.com/profile.php?id=100063817912324",
            "https://www.facebook.com/profile.php?id=100075789252435",
            "https://www.facebook.com/profile.php?id=100077360951501",
            "https://www.facebook.com/profile.php?id=100078178436518",
            "https://www.facebook.com/profile.php?id=100078388891802",
            "https://www.facebook.com/profile.php?id=100080399229190",
            "https://www.facebook.com/profile.php?id=100081558635139",
            "https://www.facebook.com/profile.php?id=100081866757888",
            "https://www.facebook.com/profile.php?id=100084381480911",
            "https://www.facebook.com/profile.php?id=100084756163077",
            "https://www.facebook.com/profile.php?id=100087037636666",
            "https://www.facebook.com/profile.php?id=100088359214551",
            "https://www.facebook.com/profile.php?id=100089375272933",
            "https://www.facebook.com/profile.php?id=100089768033385",
            "https://www.facebook.com/profile.php?id=100089928347048",
            "https://www.facebook.com/profile.php?id=100090015852923",
            "https://www.facebook.com/profile.php?id=100090549195841",
            "https://www.facebook.com/profile.php?id=100091369651790",
            "https://www.facebook.com/profile.php?id=100094269435274"
    ).toList();
}
