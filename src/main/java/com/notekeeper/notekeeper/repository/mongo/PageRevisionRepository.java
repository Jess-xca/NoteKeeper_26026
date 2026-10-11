package com.notekeeper.notekeeper.repository.mongo;

import com.notekeeper.notekeeper.document.PageRevision;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PageRevisionRepository extends MongoRepository<PageRevision, String> {

    Page<PageRevision> findByPageId(String pageId, Pageable pageable);
}
