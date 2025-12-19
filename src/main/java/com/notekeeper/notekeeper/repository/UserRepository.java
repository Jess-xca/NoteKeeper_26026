package com.notekeeper.notekeeper.repository;

import com.notekeeper.notekeeper.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    // findBy queries
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    List<User> findByFirstName(String firstName);

    List<User> findByLastName(String lastName);

    List<User> findByCreatedAtAfter(LocalDateTime date);

    List<User> findByUsernameContainingIgnoreCase(String username);

    // Location-based queries
    @Query("SELECT u FROM User u WHERE LOWER(u.location.name) LIKE LOWER(CONCAT('%', :locationName, '%'))")
    List<User> findByLocationName(@Param("locationName") String locationName);

    @Query("SELECT u FROM User u WHERE u.location.code LIKE :codePrefix%")
    List<User> findByLocationCodePrefix(@Param("codePrefix") String codePrefix);

    // existsBy queries
    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    // Sorting
    List<User> findAllByOrderByCreatedAtDesc();

    List<User> findAllByOrderByUsernameAsc();

    // Pagination
    Page<User> findAll(Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.location.name = :locationName")
    Page<User> findByLocationNamePaginated(@Param("locationName") String locationName, Pageable pageable);

    // Custom queries
    @Query("SELECT u FROM User u WHERE LOWER(u.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<User> searchUsers(@Param("keyword") String keyword);

    @Query("SELECT COUNT(u) FROM User u WHERE u.location.id = :locationId")
    long countByLocationId(@Param("locationId") String locationId);
}