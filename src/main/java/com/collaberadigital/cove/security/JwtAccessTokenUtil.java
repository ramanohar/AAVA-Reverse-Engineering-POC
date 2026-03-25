package com.collaberadigital.cove.security;

import java.security.Key;
import java.util.*;
import java.util.function.Function;

import com.collaberadigital.cove.dto.response.CheckAccessTokenResponse;
import com.collaberadigital.cove.model.entity.AccessToken;
import com.collaberadigital.cove.model.entity.TokenType;
import com.collaberadigital.cove.repository.AccessTokenRepo;
import com.collaberadigital.cove.repository.UserRepository;
import com.collaberadigital.cove.utils.JsonUtility;
import io.jsonwebtoken.Claims;
//import io.jsonwebtoken.io.Decoders;
//import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.collaberadigital.cove.configuration.BlackListToken;
import com.collaberadigital.cove.model.entity.UserEntity;


@Component
@Slf4j
public class JwtAccessTokenUtil {
	@Value("${jwt.secret}")
	private String jwtSecret;
    //30mins - 1800000
	@Value("${jwt.token.validity}")
	private long tokenValidity;

	private final AccessTokenRepo accessTokenRepo;
	private final UserRepository userRepository;

	JwtAccessTokenUtil(UserRepository userRepository,AccessTokenRepo accessTokenRepo){
		this.userRepository = userRepository;
		this.accessTokenRepo = accessTokenRepo;
	}


    public String generateToken(String userUniqueValue) {

		UserEntity authUser = userRepository.findByEmail(userUniqueValue).orElseThrow();
        Date now = new Date();
        Date validity = new Date(now.getTime() + this.tokenValidity);

		Map<String,Object> payload=new HashMap<>();

//		UserEntity authUser = doc.block(); // Blocking to wait for the Mono to complete
			payload.put("username", authUser.getFirstname());
			payload.put("email", authUser.getEmail());
			payload.put("sub", authUser.getEmail()); //need this to get the subject
			payload.put("scope_id", authUser.getRole());


		return Jwts.builder()
					.setSubject(authUser.getEmail())
					.setClaims(payload)
					.setIssuedAt(now)
					.setExpiration(validity)
					.signWith(getSignKeyToken(), SignatureAlgorithm.HS256).compact();

    }


	private Key getSignKeyToken(){
		byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
		return Keys.hmacShaKeyFor(keyBytes);
	}





	public String getUserNameFromJwtToken(String token) {
		return Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody().getSubject();
	}

	public Date getIssuedAtFromJwtToken(String token) {
		return Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody().getIssuedAt();
	}

	public CheckAccessTokenResponse validateAccessTokenV2(String token) {
		try {
			var checkToken = accessTokenRepo.findFirstByTokenOrderByCreatedAtDesc(token);

			if (checkToken.isPresent() && !checkToken.get().getRevoked() && !checkToken.get().getExpired()) {
				Claims claims = extractAllClaims(token);
				UserEntity user = userRepository.findByEmail(claims.getSubject()).orElseThrow();
				return CheckAccessTokenResponse.builder()
						.active(isTokenExpired(token))
						.sub(claims.getSubject())
						.aud(List.of(claims.getSubject()))
						.nbf(claims.getIssuedAt().getTime())
						.scope("read")
						.exp(claims.getExpiration().getTime())
						.iat(claims.getIssuedAt().getTime())
						.jti(claims.getId())
						.client_id(claims.getSubject())
						.token_type("Bearer")
						.authorities(List.of(user.getRole()))
						.build();

			}

			return CheckAccessTokenResponse.builder().active(false).build();
		} catch (Exception ex) {
			ex.printStackTrace();
			return CheckAccessTokenResponse.builder().active(false).build();
		}
	}

	public boolean validateAccessToken(String token) {
		try {
			var checkToken = accessTokenRepo.findFirstByTokenOrderByCreatedAtDesc(token);

			if (checkToken.isPresent() && !checkToken.get().getRevoked() && !checkToken.get().getExpired()) {
				return isTokenExpired(token);
			}

			return false;
		} catch (Exception ex) {
			return false;
		}
	}


//	public CheckTokenResponse validateToken(final String token) {
//
//
//	try{
//		Claims claims = extractAllClaims(token);
//
//		UserEntity user = userRepository.findByEmail(claims.getSubject()).orElseThrow();
//		return CheckTokenResponse.builder()
//				.active(isTokenExpired(token))
//				.sub(claims.getSubject())
//				.aud(List.of(claims.getSubject()))
//				.nbf(claims.getIssuedAt().getTime())
//				.scope("read")
//				.exp(claims.getExpiration().getTime())
//				.iat(claims.getIssuedAt().getTime())
//				.jti(claims.getId())
//				.client_id(claims.getSubject())
//				.token_type("Bearer")
//				.authorities(List.of(user.getRole()))
//				.build();
//	}catch (Exception err){
//		err.printStackTrace();
//		return CheckTokenResponse.builder().active(false).build();
//	}


//	}

	private Claims  extractAllClaims(String token){
		return Jwts.parserBuilder()
				.setSigningKey(jwtSecret)
				.build()
				.parseClaimsJws(token)
				.getBody();

	}

	public boolean isTokenExpired(String token) {
		if (isKeyValid(token)){
			return !extractExpiration(token).before(new Date());
		}else{
			return false;
		}

	}

	private boolean isKeyValid(String token){
		try {
			Jwts.parserBuilder()
					.setSigningKey(getSignKeyToken())
					.build()
					.parseClaimsJws(token);
			return true;
		} catch (Exception e) {
			return false;
		}
	}


	private Date extractExpiration(String token) {
		return extractClaim(token, Claims::getExpiration);
	}

	private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
		final Claims claims = extractAllClaims(token);
		return claimsResolver.apply(claims);
	}



	public Boolean validateTokenInterceptor(String token) {
		try {
			return isTokenExpired(token);

		} catch (Exception ex) {
			return false;
		}
	}

	public String extractUserUniqueValue(String token){
		return extractClaim(token, Claims::getSubject);
	}


public void revokeAccessToken(UserEntity user){
	var userTokens = accessTokenRepo.findAllByUserUserIdAndExpiredFalseAndRevokedFalse(Long.valueOf(user.getUserId()));

	if (userTokens.isEmpty())
		return;
	userTokens.forEach(token -> {
		token.setExpired(true);
		token.setRevoked(true);
	});
	accessTokenRepo.saveAll(userTokens);

}
public void storeAccessToken(UserEntity user,String token){
	var toBeSaveToken = AccessToken.builder()
			.user(user)
			.token(token)
			.expired(false)
			.revoked(false)
			.tokenType(TokenType.BEARER)
			.build();
	System.out.println("Test: "+ JsonUtility.toJson(toBeSaveToken));
	accessTokenRepo.save(toBeSaveToken);

}

}
