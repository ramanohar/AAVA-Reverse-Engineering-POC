package com.collaberadigital.cove.service;

import com.collaberadigital.cove.dto.response.CheckAccessTokenResponse;
import com.collaberadigital.cove.dto.response.RefreshTokenResponse;
import reactor.core.publisher.Mono;

public interface AuthService {

    Mono<CheckAccessTokenResponse> checkAccessToken(String token);

    Mono<RefreshTokenResponse> refreshToken(String token);

    Mono<Void> revokeAccessToken(String token);

    Mono<Void> revokeRefreshToken(String token);
}
