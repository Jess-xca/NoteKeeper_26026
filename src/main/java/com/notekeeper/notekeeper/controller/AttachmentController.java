package com.notekeeper.notekeeper.controller;

import com.notekeeper.notekeeper.model.Attachment;
import com.notekeeper.notekeeper.service.AttachmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.notekeeper.notekeeper.exception.ResourceNotFoundException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/attachments")
@CrossOrigin(origins = "*")
public class AttachmentController {

    @Autowired
    private AttachmentService attachmentService;

    // CREATE - Upload file
    @PostMapping("/upload")
    public ResponseEntity<Attachment> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("pageId") String pageId,
            @RequestParam("userId") String userId) {
        Attachment saved = attachmentService.uploadFile(file, pageId, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // READ - Get attachment by ID
    @GetMapping("/{id}")
    public ResponseEntity<Attachment> getAttachment(@PathVariable String id) {
        Attachment attachment = attachmentService.getAttachmentById(id);
        return ResponseEntity.ok(attachment);
    }

    // READ - Get all attachments for a page
    @GetMapping("/page/{pageId}")
    public ResponseEntity<List<Attachment>> getPageAttachments(@PathVariable String pageId) {
        List<Attachment> attachments = attachmentService.getAttachmentsByPageId(pageId);
        return ResponseEntity.ok(attachments);
    }

    // READ - Download attachment file
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadAttachment(@PathVariable String id) {
        try {
            Attachment attachment = attachmentService.getAttachmentById(id);
            Path filePath = Paths.get(attachment.getFilePath());
            
            if (!Files.exists(filePath)) {
                throw new ResourceNotFoundException("File not found on server");
            }

            Resource resource = new UrlResource(filePath.toUri());

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(attachment.getFileType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + attachment.getFileName() + "\"")
                    .body(resource);

        } catch (IOException e) {
            throw new RuntimeException("Failed to download file: " + e.getMessage());
        }
    }

    // UPDATE - Rename attachment
    @PutMapping("/{id}")
    public ResponseEntity<Attachment> updateAttachment(@PathVariable String id, @RequestParam String newFileName) {
        Attachment updated = attachmentService.updateAttachmentName(id, newFileName);
        return ResponseEntity.ok(updated);
    }

    // DELETE - Delete attachment
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteAttachment(@PathVariable String id) {
        attachmentService.deleteAttachment(id);
        return ResponseEntity.ok(Map.of("message", "Attachment deleted successfully"));
    }
}
