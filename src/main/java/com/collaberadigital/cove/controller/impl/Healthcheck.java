package com.collaberadigital.cove.controller.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
 
@RestController
@Slf4j
public class Healthcheck {
    private String testingValue;
 
    @Autowired
    public void fixStaticValueNull(
        @Value("${testing.text}") String secretKey1){
        testingValue = secretKey1;
    }
 
 
    @GetMapping("/healthcheck")
    public String demoController() {
 
        return ("V1: Application is up!: {}" + testingValue);
    }
}
