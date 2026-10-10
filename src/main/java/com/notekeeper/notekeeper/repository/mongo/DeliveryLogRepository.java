package com.notekeeper.notekeeper.repository.mongo;

import com.notekeeper.notekeeper.document.DeliveryLog;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DeliveryLogRepository extends MongoRepository<DeliveryLog, String> {
}
