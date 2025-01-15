package com.neo.dev.demo.conditionalexecutiondemo.REST;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile("typetwo")
@RestController
@RequestMapping("/typetwo")
class TypeTwoController {

    @GetMapping("/message")
    public String getMessage() {
        return "Hello from TypeTwo Controller!";
    }

    @GetMapping("/info")
    public String getInfo() {
        return "TypeTwo Controller provides general information.";
    }
}
