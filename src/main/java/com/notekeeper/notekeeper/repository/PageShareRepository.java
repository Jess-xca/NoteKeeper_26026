package com.notekeeper.notekeeper.repository;

import com.notekeeper.notekeeper.model.PageShare;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PageShareRepository extends JpaRepository<PageShare, String> {
    
    List<PageShare> findByPageId(String pageId);
    
    List<PageShare> findBySharedWithId(String userId);
    
    List<PageShare> findBySharedById(String userId);
    
    Optional<PageShare> findByPageIdAndSharedWithId(String pageId, String userId);
    
    @Query("SELECT ps FROM PageShare ps WHERE ps.page.id = :pageId AND ps.sharedWith.email = :email")
    Optional<PageShare> findByPageIdAndSharedWithEmail(String pageId, String email);
    
    void deleteByPageId(String pageId);
}
