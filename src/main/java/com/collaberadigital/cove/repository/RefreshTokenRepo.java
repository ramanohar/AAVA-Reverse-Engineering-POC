package com.collaberadigital.cove.repository;


import com.collaberadigital.cove.model.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepo extends JpaRepository<RefreshToken,Long> {

    List<RefreshToken> findAllByUserUserIdAndExpiredFalseAndRevokedFalse(Long userId);
    Optional<RefreshToken> findFirstByTokenOrderByCreatedAtDesc(String token);

    Optional<RefreshToken> findByToken(String token);

}
