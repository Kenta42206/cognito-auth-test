package com.example.cognitotest.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.cognitotest.dto.LoginRequestDto;
import com.example.cognitotest.dto.SignupRequestDto;
import com.example.cognitotest.dto.TokenDto;
import com.example.cognitotest.service.UserAuthService;

@RestController
public class UserAuthController {
	
	@Autowired
	private UserAuthService userAuthService;

	@PostMapping("/api/public/login")
	public ResponseEntity<TokenDto> login(@RequestBody LoginRequestDto loginRequestDto){
		
		TokenDto res = userAuthService.login(loginRequestDto);

		return ResponseEntity.ok(res);
	}
	
	@PostMapping("/api/public/signup")
	public void signup(@RequestBody SignupRequestDto signupRequestDto) {
		userAuthService.signup(signupRequestDto);
	}
}
