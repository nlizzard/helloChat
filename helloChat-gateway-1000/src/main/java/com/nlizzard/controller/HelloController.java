package com.nlizzard.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("gateway")
public class HelloController {

    @RequestMapping
    public String hello(){
        return "hello gateway-service 1000";
    }
}
