package com.collaberadigital.cove.repository;


import com.collaberadigital.cove.model.entity.AccessToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccessTokenRepo extends JpaRepository<AccessToken,Long> {

    List<AccessToken> findAllByUserUserIdAndExpiredFalseAndRevokedFalse(Long userId);
    Optional<AccessToken> findFirstByTokenOrderByCreatedAtDesc(String token);

    Optional<AccessToken> findByToken(String token);

}
