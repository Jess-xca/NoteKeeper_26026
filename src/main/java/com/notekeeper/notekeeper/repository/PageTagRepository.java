package com.notekeeper.notekeeper.repository;

import com.notekeeper.notekeeper.model.PageTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PageTagRepository extends JpaRepository<PageTag, String> {
    List<PageTag> findByPageId(String pageId);
    void deleteByPageId(String pageId);
}
