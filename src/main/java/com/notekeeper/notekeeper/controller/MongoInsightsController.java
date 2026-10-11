package com.notekeeper.notekeeper.controller;

import com.notekeeper.notekeeper.document.DeliveryLog;
import com.notekeeper.notekeeper.document.PageRevision;
import com.notekeeper.notekeeper.exception.ResourceNotFoundException;
import com.notekeeper.notekeeper.repository.mongo.DeliveryLogRepository;
import com.notekeeper.notekeeper.repository.mongo.PageRevisionRepository;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Read-only window onto MongoDB for administrators ("/api/admin/**" is ADMIN-only in SecurityConfig).
 *
 * The Mongo repositories only exist in the Docker stack. In a local run they are absent, so every
 * call answers {"available": false} instead of failing; the screen shows an explanation.
 */
@RestController
@RequestMapping("/api/admin/mongo")
public class MongoInsightsController {

    private static final int MAX_LIMIT = 200;
    private static final int PREVIEW_LENGTH = 200;

    private final ObjectProvider<PageRevisionRepository> revisions;
    private final ObjectProvider<DeliveryLogRepository> deliveryLogs;

    public MongoInsightsController(ObjectProvider<PageRevisionRepository> revisions,
                                   ObjectProvider<DeliveryLogRepository> deliveryLogs) {
        this.revisions = revisions;
        this.deliveryLogs = deliveryLogs;
    }

    @GetMapping("/status")
    public Map<String, Object> status() {
        PageRevisionRepository revisionRepo = revisions.getIfAvailable();
        DeliveryLogRepository logRepo = deliveryLogs.getIfAvailable();
        Map<String, Object> result = new LinkedHashMap<>();
        if (revisionRepo == null || logRepo == null) {
            result.put("available", false);
            result.put("message", "MongoDB is only connected when the app runs with the docker profile "
                    + "(docker compose up). A local mvn spring-boot:run does not use it.");
            return result;
        }
        try {
            result.put("available", true);
            result.put("pageRevisions", revisionRepo.count());
            result.put("deliveryLogs", logRepo.count());
        } catch (Exception e) {
            result.put("available", false);
            result.put("message", "MongoDB is configured but not reachable: " + e.getMessage());
        }
        return result;
    }

    @GetMapping("/page-revisions")
    public ResponseEntity<Map<String, Object>> pageRevisions(
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(required = false) String pageId) {
        PageRevisionRepository repo = revisions.getIfAvailable();
        if (repo == null) {
            return ResponseEntity.ok(unavailable());
        }
        Sort newestFirst = Sort.by(Sort.Direction.DESC, "savedAt");
        PageRequest paging = PageRequest.of(0, clamp(limit), newestFirst);
        boolean filtered = pageId != null && !pageId.isBlank();
        org.springframework.data.domain.Page<PageRevision> found =
                filtered ? repo.findByPageId(pageId.trim(), paging) : repo.findAll(paging);
        List<Map<String, Object>> rows = found.getContent().stream().map(this::revisionRow).collect(Collectors.toList());
        return ResponseEntity.ok(available(rows, found.getTotalElements()));
    }

    @GetMapping("/page-revisions/{id}")
    public Map<String, Object> pageRevision(@PathVariable String id) {
        PageRevisionRepository repo = revisions.getIfAvailable();
        if (repo == null) {
            return unavailable();
        }
        PageRevision revision = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Revision not found"));
        Map<String, Object> row = revisionRow(revision);
        row.put("content", revision.getContent());
        return row;
    }

    @GetMapping("/delivery-logs")
    public ResponseEntity<Map<String, Object>> deliveryLogs(
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(required = false) String channel) {
        DeliveryLogRepository repo = deliveryLogs.getIfAvailable();
        if (repo == null) {
            return ResponseEntity.ok(unavailable());
        }
        Sort newestFirst = Sort.by(Sort.Direction.DESC, "recordedAt");
        PageRequest paging = PageRequest.of(0, clamp(limit), newestFirst);
        boolean filtered = channel != null && !channel.isBlank();
        org.springframework.data.domain.Page<DeliveryLog> found =
                filtered ? repo.findByChannel(channel.trim().toLowerCase(), paging) : repo.findAll(paging);
        List<Map<String, Object>> rows = found.getContent().stream().map(this::logRow).collect(Collectors.toList());
        return ResponseEntity.ok(available(rows, found.getTotalElements()));
    }

    private Map<String, Object> revisionRow(PageRevision r) {
        String content = r.getContent() == null ? "" : r.getContent();
        String text = content.replaceAll("<[^>]*>", " ").replaceAll("\\s+", " ").trim();
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", r.getId());
        row.put("pageId", r.getPageId());
        row.put("title", r.getTitle());
        row.put("preview", text.length() > PREVIEW_LENGTH ? text.substring(0, PREVIEW_LENGTH) + "..." : text);
        row.put("contentLength", content.length());
        row.put("workspaceId", r.getWorkspaceId());
        row.put("authorId", r.getAuthorId());
        row.put("savedAt", r.getSavedAt());
        return row;
    }

    private Map<String, Object> logRow(DeliveryLog l) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", l.getId());
        row.put("channel", l.getChannel());
        row.put("recipient", l.getRecipient());
        row.put("body", l.getBody());
        row.put("recordedAt", l.getRecordedAt());
        return row;
    }

    private Map<String, Object> available(List<Map<String, Object>> items, long total) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("available", true);
        result.put("total", total);
        result.put("items", items);
        return result;
    }

    private Map<String, Object> unavailable() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("available", false);
        result.put("total", 0);
        result.put("items", List.of());
        return result;
    }

    private int clamp(int limit) {
        return Math.max(1, Math.min(limit, MAX_LIMIT));
    }
}
