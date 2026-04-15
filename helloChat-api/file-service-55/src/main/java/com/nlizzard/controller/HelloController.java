package com.nlizzard.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("file")
public class HelloController {

    @RequestMapping
    public String hello(){
        return "hello file service 55";
    }
}
