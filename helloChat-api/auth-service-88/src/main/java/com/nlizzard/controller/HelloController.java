package com.nlizzard.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("auth")
public class HelloController {

    @RequestMapping
    public String hello(){
        return "hello auth-service 88";
    }
}
