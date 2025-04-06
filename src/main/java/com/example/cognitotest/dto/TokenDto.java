package com.example.cognitotest.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TokenDto {
	private String idToken;
	private String accessToken;
	private Integer expIn;
	private String refreshToken;
}
