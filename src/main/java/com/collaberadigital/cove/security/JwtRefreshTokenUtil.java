package com.collaberadigital.cove.security;

import com.collaberadigital.cove.configuration.BlackListToken;
import com.collaberadigital.cove.dto.response.RefreshTokenResponse;
import com.collaberadigital.cove.model.entity.RefreshToken;
import com.collaberadigital.cove.model.entity.TokenType;
import com.collaberadigital.cove.model.entity.UserEntity;
import com.collaberadigital.cove.repository.RefreshTokenRepo;
import com.collaberadigital.cove.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.security.Key;
import java.util.*;
import java.util.function.Function;


@Component
@Slf4j
public class JwtRefreshTokenUtil {

	@Value("${jwt.secret.refresh.token}")
	private String jwtSecretRefreshToken;


    //1hour - 86400000
	@Value("${jwt.refresh.token.validity}")
	private long refreshTokenValidity;

	private RefreshTokenRepo refreshTokenRepo;
	private final UserRepository userRepository;
	private final JwtAccessTokenUtil jwtAccessTokenUtil;

	JwtRefreshTokenUtil(UserRepository userRepository,RefreshTokenRepo refreshTokenRepo,JwtAccessTokenUtil jwtAccessTokenUtil){
		this.userRepository = userRepository;
		this.refreshTokenRepo = refreshTokenRepo;
		this.jwtAccessTokenUtil = jwtAccessTokenUtil;
	}



	public String generateRefreshToken(String userUniqueValue) {
		UserEntity authUser = userRepository.findByEmail(userUniqueValue).orElseThrow();
		Date now = new Date();
		Date validity = new Date(now.getTime() + this.refreshTokenValidity);

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
				.signWith(getSignKeyRefreshToken(), SignatureAlgorithm.HS256).compact();
	}

	public String generateRefreshWithValidity(String userUniqueValue,String token) {
		UserEntity authUser = userRepository.findByEmail(userUniqueValue).orElseThrow();
		Date now = new Date();

		Date validity = extractExpiration(token);

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
				.signWith(getSignKeyRefreshToken(), SignatureAlgorithm.HS256).compact();
	}

	private Key getSignKeyRefreshToken(){
		byte[] keyBytes = Decoders.BASE64.decode(jwtSecretRefreshToken);
		return Keys.hmacShaKeyFor(keyBytes);
	}

	public Mono<RefreshTokenResponse> generateNewRefreshToken(String token) {
		try {
			var checkToken = refreshTokenRepo.findFirstByTokenOrderByCreatedAtDesc(token);

			if (checkToken.isPresent() && !checkToken.get().getRevoked() && !checkToken.get().getExpired()) {
				Claims claims = extractAllClaims(token);
				UserEntity user = userRepository.findByEmail(claims.getSubject()).orElseThrow();


				jwtAccessTokenUtil.revokeAccessToken(user);
				String newAccessToken = jwtAccessTokenUtil.generateToken(user.getEmail());
				jwtAccessTokenUtil.storeAccessToken(user,newAccessToken);

				revokeRefreshToken(user);
				String newRefreshToken = generateRefreshWithValidity(user.getEmail(),token);
				storeRefreshToken(user,newRefreshToken);

				return Mono.just(RefreshTokenResponse
						.builder()
						.active(isTokenExpired(token))
						.refreshToken(newRefreshToken)
						.accessToken(newAccessToken)
						.tokenType("Bearer")
						.build());

			}

			return Mono.just(RefreshTokenResponse
					.builder()
					.active(false)
					.build());
		} catch (Exception ex) {
			ex.printStackTrace();
			return Mono.just(RefreshTokenResponse
					.builder()
					.active(false)
					.build());
		}
	}





	public String getUserNameFromJwtToken(String token) {
		return Jwts.parser().setSigningKey(jwtSecretRefreshToken).parseClaimsJws(token).getBody().getSubject();
	}

	public Date getIssuedAtFromJwtToken(String token) {
		return Jwts.parser().setSigningKey(jwtSecretRefreshToken).parseClaimsJws(token).getBody().getIssuedAt();
	}


	public boolean validateToken(final String token) {


		try {
			var checkToken = refreshTokenRepo.findFirstByTokenOrderByCreatedAtDesc(token);

			if (checkToken.isPresent() && !checkToken.get().getRevoked() && !checkToken.get().getExpired()) {
				return isTokenExpired(token);
			}

			return false;
		} catch (Exception ex) {
			return false;
		}

	}

	private Claims  extractAllClaims(String token){
		return Jwts.parserBuilder()
				.setSigningKey(jwtSecretRefreshToken)
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
					.setSigningKey(getSignKeyRefreshToken())
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
	public boolean checkBlackList(String token) {
		if (BlackListToken.blackListTokens.contains(token)) {
			return false;
		}
		return true;
	}



	public String extractUserUniqueValue(String token){
		return extractClaim(token, Claims::getSubject);
	}

	public void revokeRefreshToken(UserEntity user){
		var userTokens = refreshTokenRepo.findAllByUserUserIdAndExpiredFalseAndRevokedFalse(Long.valueOf(user.getUserId()));

		if (userTokens.isEmpty())
			return;
		userTokens.forEach(token -> {
			token.setExpired(true);
			token.setRevoked(true);
		});
		refreshTokenRepo.saveAll(userTokens);


	}
	public void storeRefreshToken(UserEntity user,String token){
		var toBeSaveToken = RefreshToken.builder()
				.user(user)
				.token(token)
				.expired(false)
				.revoked(false)
				.tokenType(TokenType.BEARER)
				.build();
		refreshTokenRepo.save(toBeSaveToken);

	}
	    
}
