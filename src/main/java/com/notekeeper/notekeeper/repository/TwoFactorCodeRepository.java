package com.notekeeper.notekeeper.repository;

import com.notekeeper.notekeeper.model.TwoFactorCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TwoFactorCodeRepository extends JpaRepository<TwoFactorCode, String> {
    
    // Find all codes for a user, ordered by expiry date (newest first)
    List<TwoFactorCode> findByUserIdOrderByExpiryDateDesc(String userId);
    
    Optional<TwoFactorCode> findByUserIdAndCodeAndUsedFalse(String userId, String code);
    
    void deleteByUserId(String userId);
}
