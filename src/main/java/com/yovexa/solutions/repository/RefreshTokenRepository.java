package com.yovexa.solutions.repository;

import com.yovexa.solutions.model.RefreshToken;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends MongoRepository<RefreshToken, String> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    List<RefreshToken> findAllByFamilyId(String familyId);

    List<RefreshToken> findAllByAdminIdAndIsRevokedFalse(String adminId);

    void deleteAllByAdminId(String adminId);
}
