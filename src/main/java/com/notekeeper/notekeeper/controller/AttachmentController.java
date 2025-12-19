package com.notekeeper.notekeeper.controller;

import com.notekeeper.notekeeper.model.Attachment;
import com.notekeeper.notekeeper.model.Page;
import com.notekeeper.notekeeper.model.User;
import com.notekeeper.notekeeper.repository.AttachmentRepository;
import com.notekeeper.notekeeper.repository.PageRepository;
import com.notekeeper.notekeeper.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/attachments")
@CrossOrigin(origins = "*")
public class AttachmentController {

    @Autowired
    private AttachmentRepository attachmentRepository;

    @Autowired
    private PageRepository pageRepository;

    @Autowired
    private UserRepository userRepository;

    private final String uploadDir = "uploads/attachments/";

    // CREATE - Upload file
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("pageId") String pageId,
            @RequestParam("userId") String userId) {
        try {
            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("File is empty");
            }

            // Check file size (max 10MB)
            if (file.getSize() > 10 * 1024 * 1024) {
                return ResponseEntity.badRequest().body("File size exceeds 10MB limit");
            }

            // Get page and user
            Optional<Page> pageOpt = pageRepository.findById(pageId);
            if (!pageOpt.isPresent()) {
                return ResponseEntity.badRequest().body("Page not found");
            }

            Optional<User> userOpt = userRepository.findById(userId);
            if (!userOpt.isPresent()) {
                return ResponseEntity.badRequest().body("User not found");
            }

            // Create upload directory if not exists
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
            Path filePath = uploadPath.resolve(uniqueFilename);

            // Save file
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Create attachment record
            Attachment attachment = new Attachment(
                    originalFilename,
                    file.getContentType(),
                    file.getSize(),
                    filePath.toString(),
                    pageOpt.get(),
                    userOpt.get()
            );

            Attachment saved = attachmentRepository.save(attachment);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to upload file: " + e.getMessage());
        }
    }

    // READ - Get attachment by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getAttachment(@PathVariable String id) {
        try {
            Optional<Attachment> attachment = attachmentRepository.findById(id);
            if (attachment.isPresent()) {
                return ResponseEntity.ok(attachment.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to get attachment: " + e.getMessage());
        }
    }

    // READ - Get all attachments for a page
    @GetMapping("/page/{pageId}")
    public ResponseEntity<?> getPageAttachments(@PathVariable String pageId) {
        try {
            List<Attachment> attachments = attachmentRepository.findByPageId(pageId);
            return ResponseEntity.ok(attachments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to get attachments: " + e.getMessage());
        }
    }

    // READ - Download attachment file
    @GetMapping("/{id}/download")
    public ResponseEntity<?> downloadAttachment(@PathVariable String id) {
        try {
            Optional<Attachment> attachmentOpt = attachmentRepository.findById(id);
            if (!attachmentOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            Attachment attachment = attachmentOpt.get();
            Path filePath = Paths.get(attachment.getFilePath());
            
            if (!Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new UrlResource(filePath.toUri());

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(attachment.getFileType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + attachment.getFileName() + "\"")
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to download file: " + e.getMessage());
        }
    }

    // UPDATE - Rename attachment
    @PutMapping("/{id}")
    public ResponseEntity<?> updateAttachment(@PathVariable String id, @RequestParam String newFileName) {
        try {
            Optional<Attachment> attachmentOpt = attachmentRepository.findById(id);
            if (!attachmentOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            Attachment attachment = attachmentOpt.get();
            attachment.setFileName(newFileName);
            Attachment updated = attachmentRepository.save(attachment);

            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to update attachment: " + e.getMessage());
        }
    }

    // DELETE - Delete attachment
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAttachment(@PathVariable String id) {
        try {
            Optional<Attachment> attachmentOpt = attachmentRepository.findById(id);
            if (!attachmentOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            Attachment attachment = attachmentOpt.get();

            // Delete physical file
            try {
                Path filePath = Paths.get(attachment.getFilePath());
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                System.err.println("Failed to delete physical file: " + e.getMessage());
            }

            // Delete database record
            attachmentRepository.delete(attachment);

            return ResponseEntity.ok("Attachment deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to delete attachment: " + e.getMessage());
        }
    }
}
