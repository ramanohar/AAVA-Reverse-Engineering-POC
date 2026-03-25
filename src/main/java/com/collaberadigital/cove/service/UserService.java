package com.collaberadigital.cove.service;

import com.collaberadigital.cove.model.AuthUser;

import com.collaberadigital.cove.model.AuthenticationRequest;
import com.collaberadigital.cove.model.SuccessResponse;
import com.collaberadigital.cove.model.Token;
import reactor.core.publisher.Mono;

import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Map;

public interface UserService {
//    Mono<AuthUser> createUser(AuthUser user);
    List<AuthUser> getUsers(PageRequest pageRequest);
    public void approveUser(Integer userId,  String userRole);

    Mono <SuccessResponse> registerUser(AuthUser user);
    Mono <Token> loginUser(AuthenticationRequest request, Map<String, String> requestParams);



}
