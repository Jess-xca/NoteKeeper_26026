package com.notekeeper.notekeeper.repository;

import com.notekeeper.notekeeper.model.PageView;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface PageViewRepository extends JpaRepository<PageView, String> {

    Optional<PageView> findByUserIdAndPageId(String userId, String pageId);

    List<PageView> findByUserIdOrderByLastViewedAtDesc(String userId, Pageable pageable);

    @Modifying
    @Transactional
    @Query("DELETE FROM PageView pv WHERE pv.pageId = :pageId")
    void deleteByPageId(@Param("pageId") String pageId);

    @Modifying
    @Transactional
    @Query("DELETE FROM PageView pv WHERE pv.userId = :userId")
    void deleteByUserId(@Param("userId") String userId);
}
