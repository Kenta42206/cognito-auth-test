package com.example.cognitotest.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;


@Component
@ConfigurationProperties(prefix = "spring.aws.cognito")
@Getter
@Setter
public class CognitoPropertis {
	private String userPoolId;
	private String clientId;
	private String clientSecret;
	private String region;
	private String jwkUrl;
	private String iss;
	private String accountId;
	private String accountPass;
}
