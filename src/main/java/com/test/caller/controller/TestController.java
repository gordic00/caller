package com.test.caller.controller;

import com.test.caller.model.PageRequest;
import com.test.caller.service.CallerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@Validated
@RestController
@RequestMapping(path = "/api/v1/test")
@RequiredArgsConstructor
public class TestController {
    private final CallerService callerService;

    @GetMapping(path = "/get/html")
    public ResponseEntity<String> getHtmlFromCollector(
            PageRequest pageRequest) {
        try {
            return ResponseEntity.ok(callerService.getHtml(pageRequest));
        } catch (Exception exception) {
            System.out.println(exception.getMessage());
            return ResponseEntity.badRequest().body(exception.getMessage());
        }
    }
}


