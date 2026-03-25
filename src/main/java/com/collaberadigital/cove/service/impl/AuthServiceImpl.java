package com.collaberadigital.cove.service.impl;

import com.collaberadigital.cove.dto.response.CheckAccessTokenResponse;
import com.collaberadigital.cove.dto.response.RefreshTokenResponse;
import com.collaberadigital.cove.model.entity.AccessToken;
import com.collaberadigital.cove.model.entity.UserEntity;
import com.collaberadigital.cove.repository.AccessTokenRepo;
import com.collaberadigital.cove.repository.UserRepository;
import com.collaberadigital.cove.security.JwtRefreshTokenUtil;
import com.collaberadigital.cove.security.JwtAccessTokenUtil;
import com.collaberadigital.cove.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final JwtAccessTokenUtil jwtAccessTokenUtil;
    private final JwtRefreshTokenUtil jwtRefreshTokenUtil;
    private final UserRepository userRepository;



    AuthServiceImpl(JwtAccessTokenUtil jwtAccessTokenUtil,
                    JwtRefreshTokenUtil jwtRefreshTokenUtil,
                    UserRepository userRepository){
      this.jwtAccessTokenUtil = jwtAccessTokenUtil;
      this.jwtRefreshTokenUtil = jwtRefreshTokenUtil;
      this.userRepository = userRepository;
    }

    @Override
    public Mono<CheckAccessTokenResponse> checkAccessToken(String token) {
        return Mono.just(jwtAccessTokenUtil.validateAccessTokenV2(token));
    }

    @Override
    public Mono<RefreshTokenResponse> refreshToken(String token) {
      return jwtRefreshTokenUtil.generateNewRefreshToken(token);
    }

    @Override
    public Mono<Void> revokeAccessToken(String token) {
        String userEmail = jwtAccessTokenUtil.extractUserUniqueValue(token);
       UserEntity user = userRepository.findByEmail(userEmail).orElseThrow();
        jwtAccessTokenUtil.revokeAccessToken(user);
        return Mono.empty();

    }

    @Override
    public Mono<Void> revokeRefreshToken(String token) {
        String userEmail = jwtRefreshTokenUtil.extractUserUniqueValue(token);
        UserEntity user = userRepository.findByEmail(userEmail).orElseThrow();
        jwtRefreshTokenUtil.revokeRefreshToken(user);
        jwtAccessTokenUtil.revokeAccessToken(user);
        return Mono.empty();
    }
}
