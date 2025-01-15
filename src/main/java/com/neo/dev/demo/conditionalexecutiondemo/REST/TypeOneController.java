package com.neo.dev.demo.conditionalexecutiondemo.REST;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile("typeone")
@RestController
@RequestMapping("/typeone")
class TypeOneController {

    @GetMapping("/message")
    public String getMessage() {
        return "Hello from TypeOne Controller!";
    }

    @GetMapping("/info")
    public String getInfo() {
        return "TypeOne Controller provides general information.";
    }
}
