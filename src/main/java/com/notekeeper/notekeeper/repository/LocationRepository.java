package com.notekeeper.notekeeper.repository;

import com.notekeeper.notekeeper.model.Location;
import com.notekeeper.notekeeper.model.LocationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LocationRepository extends JpaRepository<Location, String> {

    // findBy queries
    Optional<Location> findByCode(String code);

    List<Location> findByName(String name);

    List<Location> findByType(LocationType type);

    List<Location> findByParentId(String parentId);

    List<Location> findByParentIsNull(); // Get all provinces

    List<Location> findByNameContainingIgnoreCase(String name);

    // Hierarchy queries
    @Query("SELECT l FROM Location l WHERE l.parent.code = :parentCode")
    List<Location> findByParentCode(@Param("parentCode") String parentCode);

@Query("SELECT l FROM Location l WHERE l.parent.type = 'PROVINCE' AND
l.parent.code = :provinceCode AND l.type='DISTRICT'")

    List<Location> findDistrictsByProvinceCode(@Param("provinceCode") String provinceCode);

    // existsBy queries
    boolean existsByCode(String code);

    boolean existsByName(String name);

    boolean existsByParentId(String parentId);

    // Sorting
    List<Location> findAllByOrderByNameAsc();

    List<Location> findByType(LocationType type, Sort sort);

    // Pagination
    Page<Location> findAll(Pageable pageable);

    Page<Location> findByType(LocationType type, Pageable pageable);

    // Custom queries
    @Query("SELECT COUNT(l) FROM Location l WHERE l.parent.id = :parentId")
    long countChildrenByParentId(@Param("parentId") String parentId);

    @Query("SELECT COUNT(u) FROM User u WHERE u.location.id = :locationId")
    long countUsersByLocationId(@Param("locationId") String locationId);
}