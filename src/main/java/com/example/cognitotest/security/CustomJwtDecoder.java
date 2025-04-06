package com.example.cognitotest.security;

import java.net.URL;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;

@Component
public class CustomJwtDecoder implements JwtDecoder {
	
	private static final ObjectMapper objectMapper = new ObjectMapper();

	@Autowired
	private CognitoPropertis cognitoPropertis;
	
	@Override
	public Jwt decode(String token) throws JwtException {

		String iss = cognitoPropertis.getIss();
	    String jwkUrl = cognitoPropertis.getJwkUrl();
		
		try {
			JWKSet jwkSet = JWKSet.load(new URL(jwkUrl));
			DecodedJWT decodedJWT = JWT.decode(token);
			String kid = decodedJWT.getKeyId();
			
			JWK jwk = jwkSet.getKeyByKeyId(kid);
			if (jwk == null) {
				throw new JwtException("No matching key found for kid: " + kid);
			}
			
			RSAPublicKey publicKey = (RSAPublicKey) jwk.toRSAKey().toPublicKey();
			Algorithm algorithm = Algorithm.RSA256(publicKey, null);
			
			algorithm.verify(decodedJWT);
			if (!decodedJWT.getIssuer().equals(iss)) {
				throw new JwtException("Invalid issuer");
			}
			
			String[] parts = token.split("\\.");
			String headerJson = new String(Base64.getUrlDecoder().decode(parts[0]));
			Map<String, Object> headers = new HashMap<>(parseJson(headerJson));
			
			Map<String, Object> claims = new HashMap<>();
			decodedJWT.getClaims().forEach((k, v) -> claims.put(k, v.as(Object.class)));
			
			return new Jwt(
				token,
				decodedJWT.getIssuedAt().toInstant(),
				decodedJWT.getExpiresAt().toInstant(),
				headers,
				claims
			);
		} catch (Exception e) {
			throw new JwtException("Failed to decode JWT", e);
		}
	}
	
	private Map<String, Object> parseJson(String json) throws Exception{
		return objectMapper.readValue(json,  Map.class);
	}
}