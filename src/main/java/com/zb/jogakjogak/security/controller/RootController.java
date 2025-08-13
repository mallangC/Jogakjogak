package com.zb.jogakjogak.security.controller;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Hidden
@RestController
public class RootController {
    @GetMapping("/")
    public String root() {
        return "OK!";
    }
}
