package com.notekeeper.notekeeper.repository;

import com.notekeeper.notekeeper.model.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, String> {
    
    List<Attachment> findByPageId(String pageId);
    
    List<Attachment> findByUploadedById(String userId);
    
    void deleteByPageId(String pageId);
}
