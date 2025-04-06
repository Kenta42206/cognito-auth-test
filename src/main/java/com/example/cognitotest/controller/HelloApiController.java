package com.example.cognitotest.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloApiController {

	@GetMapping("/api/public")
	public String publicHello() {
		return "This is a URI for all users";
	}
	
	@GetMapping("/api/private")
    public String privateHello() {
        return "This is a URI for authenticatedd users";
    }
}
