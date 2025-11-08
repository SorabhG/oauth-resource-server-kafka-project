package com.example.resourceserver.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.security.Principal;

@RestController
public class HelloController {

    @GetMapping("/secure")
    public String secureEndpoint(Principal principal) {
        return "Welcome, " + principal.getName() + "! You accessed a protected resource.";
    }
}
