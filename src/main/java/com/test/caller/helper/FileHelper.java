package com.test.caller.helper;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Date;

@Component
public class FileHelper {

    /**
     * Get resource from path.
     *
     * @param path String
     * @return String
     */
    public static String readResource(String path) {
        File resource;
        try {
            resource = new ClassPathResource(path).getFile();
            return Files.readString(resource.toPath());
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Validate that String is not null.
     *
     * @param str String
     * @return String
     */
    public static String nullR(String str) {
        return str == null ? "" : str;
    }

    /**
     * Base64 encode.
     *
     * @param txt - text for encoding
     * @return String
     */
    public String base64UrlEncode(String txt) {
        return Base64.getUrlEncoder().encodeToString(txt.getBytes());
    }

    public String base64Encode(String txt) {
        return Base64.getEncoder().encodeToString(txt.getBytes());
    }

    /**
     * Get substring between two characters if they exist in string.
     *
     * @param one  char
     * @param two  char
     * @param text String
     * @return String
     */
    public String getSubstringBetween(char one, char two, String text) {
        if (text.contains(String.valueOf(one)) && text.contains(String.valueOf(two))) {
            text = text.substring(text.indexOf(one) + 1);
            return text.substring(0, text.indexOf(two));
        }
        return text;
    }

    /**
     * Generate new file name that start with long milliseconds of current time.
     *
     * @param name String
     * @return String
     */
    public String generateFileName(String name) {
        return new Date().getTime() + "_" + name.replace(" ", "_");
    }
}
