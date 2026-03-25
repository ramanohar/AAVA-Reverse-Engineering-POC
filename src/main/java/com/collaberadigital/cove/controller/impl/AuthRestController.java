package com.collaberadigital.cove.controller.impl;

import java.util.Map;

import javax.validation.Valid;

import com.collaberadigital.cove.dto.response.CheckAccessTokenResponse;
import com.collaberadigital.cove.dto.response.RefreshTokenResponse;
import com.collaberadigital.cove.model.*;
import com.collaberadigital.cove.service.AuthService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import com.collaberadigital.cove.exception.CoveCustomException;
import com.collaberadigital.cove.repository.UserRepository;
import com.collaberadigital.cove.security.JwtAccessTokenUtil;
import com.collaberadigital.cove.service.UserService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import reactor.core.publisher.Mono;


@RestController
//@RequiredArgsConstructor
public class AuthRestController {

	private static final Logger logger = LogManager.getLogger(AuthRestController.class);

	@Autowired
	private JwtAccessTokenUtil jwtUtil;

	private final ReactiveAuthenticationManager authenticationManager;


	
	@Autowired
	private final WebClient.Builder webClientBuilder;
	
	@Autowired
    private UserRepository userRepository;
	

	private UserService userService;

	private final AuthService authService;

	
	@Value("${jwt.secret}")
	private String jwtSecret;


	@Autowired
	private Environment env;

	public AuthRestController(
			ReactiveAuthenticationManager authenticationManager,
			WebClient.Builder webClientBuilder,
			AuthService authService,

			UserService userService) {
		this.authenticationManager = authenticationManager;
		this.webClientBuilder = webClientBuilder;
		this.authService = authService;
		this.userService = userService;

	}




@PostMapping("/login")
public Mono<Token> login(@Valid @RequestBody AuthenticationRequest authRequest, @RequestParam Map<String, String> requestParams) throws CoveCustomException {
     return userService.loginUser(authRequest,requestParams);
////	if(requestParams.size()!=0)
////		throw new CoveCustomException("Invalid Url", HttpStatus.NOT_FOUND, "Please provide a valid Url");
////
//	return authRequest.flatMap(login -> this.authenticationManager
//			.authenticate(new UsernamePasswordAuthenticationToken(login.getEmail(), login.getPassword()))
//			.map(authentication -> {
//				return this.jwtUtil.generateToken(authentication.getName());
//			})).map(jwt -> {
//				HttpHeaders httpHeaders = new HttpHeaders();
//				httpHeaders.add(HttpHeaders.AUTHORIZATION, "Bearer " + jwt.getToken());
//				logger.info("login has been inititaed for user "+ jwt.getFirstname());
//				return new ResponseEntity<>(jwt, httpHeaders, HttpStatus.OK);
//			});

}


@PostMapping("/register")
public Mono<SuccessResponse> register(@RequestBody AuthUser user){
	return  userService.registerUser(user);
	
}




@PostMapping("/api/login")
public Mono<ResponseEntity<String>> login(@RequestBody Map<String, String> credentials) {
    String username = credentials.get("username");
    String password = credentials.get("password");

    return webClientBuilder.build()
            .post()
            .uri("https://app.avaoneview.io/")
            .bodyValue(Map.of("username", username, "password", password))
            .retrieve()
            .bodyToMono(String.class)
            .map(response -> ResponseEntity.ok().body(response))
            .onErrorResume(error -> Mono.just(ResponseEntity.status(500).body("Login failed"+error.getMessage())));
}

   @PostMapping("/check-access-token")
	public Mono<CheckAccessTokenResponse> checkTokenController(@RequestParam String token){
	   return authService.checkAccessToken(token);
   }

   @PostMapping("/refresh-token")
	public Mono<RefreshTokenResponse> refreshTokenController(@RequestParam String token){
		return authService.refreshToken(token);

   }

   @PostMapping("/revoke-access-token")
   public Mono<Void> revokeAccessTokenController(@RequestParam String token){
		return authService.revokeAccessToken(token);
   }
	@PostMapping("/revoke-refresh-token")
	public Mono<Void> revokeRefreshTokenController(@RequestParam String token){
		return authService.revokeRefreshToken(token);
	}


}

