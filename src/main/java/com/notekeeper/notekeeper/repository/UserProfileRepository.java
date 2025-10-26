package com.notekeeper.notekeeper.repository;

import com.notekeeper.notekeeper.model.UserProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, String> {

    // findBy queries
    Optional<UserProfile> findByUserId(String userId);

    List<UserProfile> findByTheme(String theme);

    List<UserProfile> findByLanguage(String language);

    // existsBy queries
    boolean existsByUserId(String userId);

    // Pagination
    Page<UserProfile> findAll(Pageable pageable);

    // Custom queries
    @Query("SELECT p FROM UserProfile p WHERE p.bio IS NOT NULL")
    List<UserProfile> findProfilesWithBio();

    @Query("SELECT COUNT(p) FROM UserProfile p WHERE p.theme = :theme")
    long countByTheme(@Param("theme") String theme);
}