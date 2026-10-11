package com.notekeeper.notekeeper.repository.mongo;

import com.notekeeper.notekeeper.document.DeliveryLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DeliveryLogRepository extends MongoRepository<DeliveryLog, String> {

    Page<DeliveryLog> findByChannel(String channel, Pageable pageable);
}
