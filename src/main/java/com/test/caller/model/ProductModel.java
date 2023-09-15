package com.test.caller.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@Setter
@NoArgsConstructor
@ToString
@JsonInclude(JsonInclude.Include.USE_DEFAULTS)
@Document(value = "product_model")
public class ProductModel {

    private String title;
    private String link;
    private String price;
    private String minPrice;
    private String maxPrice;
    private String description;
    private String currency;
    private String category;
    private String seller;
    private String sellerUrl;
    private String sellerId;
    private List<String> imageUrls;
    private String screenshotPath;

    public void setTitle(String text) {
        this.title = getFormatedString(text);
    }

    public void setDescription(String text) {
        this.description = getFormatedString(text);
    }

    private String getFormatedString(String text) {
        String regex = "[^\\p{L}\\p{N}\\p{P}\\p{Z}]";
        Pattern pattern = Pattern.compile(
                regex,
                Pattern.UNICODE_CHARACTER_CLASS);
        Matcher matcher = pattern.matcher(text);
        return matcher.replaceAll("");
    }
}
