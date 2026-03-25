package com.collaberadigital.cove.repository;

import com.collaberadigital.cove.model.entity.UserEntity;

import reactor.core.publisher.Mono;

public interface CustomerDao {

    Mono<UserEntity> findByEmailReactive(String email);


}
