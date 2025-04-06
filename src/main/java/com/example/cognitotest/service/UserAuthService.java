package com.example.cognitotest.service;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClientBuilder;
import com.amazonaws.services.cognitoidp.model.AuthFlowType;
import com.amazonaws.services.cognitoidp.model.InitiateAuthRequest;
import com.amazonaws.services.cognitoidp.model.InitiateAuthResult;
import com.example.cognitotest.dto.LoginRequestDto;
import com.example.cognitotest.dto.SignupRequestDto;
import com.example.cognitotest.dto.TokenDto;
import com.example.cognitotest.entity.AppUser;
import com.example.cognitotest.repo.UserRepository;
import com.example.cognitotest.security.CognitoPropertis;

@Service
public class UserAuthService {
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private CognitoPropertis cognitoPropertis;

	public TokenDto login(LoginRequestDto loginRequestDto) {
		String username = loginRequestDto.getUsername();
		String password = loginRequestDto.getPassword();
		
		AppUser user = userRepository.findByUsername(username).orElseThrow(()->new UsernameNotFoundException("User not found"));
		
		if(!passwordEncoder.matches(password, user.getPassword())) {
			throw new BadCredentialsException("Username or Password not correct");
		}
		
		InitiateAuthResult cognitoAuthRes = getTokenFromCognito();

		TokenDto res = new TokenDto();

		res.setAccessToken(cognitoAuthRes.getAuthenticationResult().getAccessToken());
		res.setIdToken(cognitoAuthRes.getAuthenticationResult().getIdToken());
		res.setRefreshToken(cognitoAuthRes.getAuthenticationResult().getRefreshToken());
		res.setExpIn(cognitoAuthRes.getAuthenticationResult().getExpiresIn());

		return res;
		
		
		
	}

	public void signup(SignupRequestDto signupRequestDto) {
		String username = signupRequestDto.getUsername();
		String password = signupRequestDto.getPassword();
		
		Optional<AppUser> exsitingUser = userRepository.findByUsername(username);
		
		if(exsitingUser.isPresent()) {
			throw new IllegalArgumentException("This username is used by other user");
		}
		
		AppUser newUser = new AppUser(username, passwordEncoder.encode(password));
		userRepository.save(newUser);
		
	}
	
	private InitiateAuthResult getTokenFromCognito() {
		
		AWSCognitoIdentityProvider client = AWSCognitoIdentityProviderClientBuilder.standard()
		        .withRegion(Regions.AP_NORTHEAST_1)
		        .build();

		String fixedUsername = cognitoPropertis.getAccountId();
		String fixedPassword = cognitoPropertis.getAccountPass();
		String clientId = cognitoPropertis.getClientId();
		String clientSecret = cognitoPropertis.getClientSecret();
		
		String secretHash = calcSecretHash(fixedUsername, clientId, clientSecret);
		
		Map<String, String> authParameters = new HashMap<>();
		authParameters.put("USERNAME", fixedUsername);
		authParameters.put("PASSWORD", fixedPassword);
		authParameters.put("SECRET_HASH", secretHash);

		InitiateAuthRequest request = new InitiateAuthRequest();
		request
		    .withAuthFlow(AuthFlowType.USER_PASSWORD_AUTH)
		    .withClientId(clientId)
		    .withAuthParameters(authParameters);
		
		try {

			InitiateAuthResult res = client.initiateAuth(request);
			if (res.getAuthenticationResult() == null) {
		        System.out.println("No authentication result received.");
		    }
			return res;
		} catch (Exception e) {
		    throw new RuntimeException("Authentication failed: " + e.getMessage());
		}
		

	}
	
	// クライアントシークレットを生成する場合はＳｅｃｒｅｔＨａｓｈが必要になる
//	private String calcSecretHash(String clientId, String clientSecret, String username) {
//		try {
//			String data = clientId + username;
//			Mac mac = Mac.getInstance("HmacSHA256");
//			SecretKeySpec secretKeySpec = new SecretKeySpec(clientSecret.getBytes(), "HmacSHA256");
//			mac.init(secretKeySpec);
//			byte[] hash = mac.doFinal(data.getBytes());
//			return Base64.getEncoder().encodeToString(hash);
//		} catch (Exception e) {
//			throw new RuntimeException("Error calculating SECRET_HASH", e);
//		}
//	}

	private String calcSecretHash(String username, String clientId, String clientSecret) {
        try {
            String message = username + clientId;
            SecretKeySpec keySpec = new SecretKeySpec(clientSecret.getBytes("UTF-8"), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(keySpec);
            byte[] hash = mac.doFinal(message.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Error while calculating SECRET_HASH", e);
        }
    }
}
