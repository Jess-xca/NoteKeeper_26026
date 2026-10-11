package com.notekeeper.notekeeper.controller;

import com.notekeeper.notekeeper.dto.AttachmentDTO;
import com.notekeeper.notekeeper.exception.BadRequestException;
import com.notekeeper.notekeeper.exception.ResourceNotFoundException;
import com.notekeeper.notekeeper.model.Attachment;
import com.notekeeper.notekeeper.security.UserPrincipal;
import com.notekeeper.notekeeper.service.AttachmentService;
import com.notekeeper.notekeeper.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
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

    @Autowired
    private PermissionService permissionService;

    // CREATE - Upload file. The uploader is the signed-in person; anyone who may edit the page can attach.
    @PostMapping("/upload")
    public ResponseEntity<AttachmentDTO> uploadFile(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("file") MultipartFile file,
            @RequestParam("pageId") String pageId,
            @RequestParam(value = "userId", required = false) String ignoredUserId) {
        if (pageId == null || pageId.isBlank()) {
            throw new BadRequestException("Save the page first, then attach the file");
        }
        permissionService.validatePageAccess(pageId, principal.getId(), "EDIT");
        Attachment saved = attachmentService.uploadFile(file, pageId, principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(AttachmentDTO.from(saved));
    }

    // READ - Get attachment by ID
    @GetMapping("/{id}")
    public ResponseEntity<AttachmentDTO> getAttachment(
            @AuthenticationPrincipal UserPrincipal principal, @PathVariable String id) {
        Attachment attachment = readable(principal, id);
        return ResponseEntity.ok(AttachmentDTO.from(attachment));
    }

    // READ - Get all attachments for a page
    @GetMapping("/page/{pageId}")
    public ResponseEntity<List<AttachmentDTO>> getPageAttachments(
            @AuthenticationPrincipal UserPrincipal principal, @PathVariable String pageId) {
        permissionService.validatePageAccess(pageId, principal.getId(), "READ");
        return ResponseEntity.ok(attachmentService.getAttachmentsByPageId(pageId).stream()
                .map(AttachmentDTO::from).toList());
    }

    // READ - Download attachment file by ID
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadAttachment(
            @AuthenticationPrincipal UserPrincipal principal, @PathVariable String id) {
        try {
            Attachment attachment = readable(principal, id);
            return serveFile(Paths.get(attachment.getFilePath()), attachment.getFileType(), attachment.getFileName());
        } catch (IOException e) {
            throw new RuntimeException("Failed to download file: " + e.getMessage());
        }
    }

    // READ - Download by path (for cover images in <img> tags, which cannot send a token).
    // Only files inside the uploads folder are ever served; their names are random UUIDs.
    @GetMapping("/download-by-path")
    public ResponseEntity<Resource> downloadByPath(@RequestParam String path) {
        try {
            Path uploads = AttachmentService.UPLOAD_ROOT.toAbsolutePath().normalize();
            Path requested = Paths.get(path).toAbsolutePath().normalize();
            if (!requested.startsWith(uploads)) {
                // Same answer as for a missing file, so nobody can probe the disk
                throw new ResourceNotFoundException("File not found on server");
            }
            String contentType = Files.probeContentType(requested);
            if (contentType == null) contentType = "image/jpeg";

            return serveFile(requested, contentType, requested.getFileName().toString());
        } catch (java.nio.file.InvalidPathException e) {
            throw new ResourceNotFoundException("File not found on server");
        } catch (IOException e) {
            throw new RuntimeException("Failed to download file: " + e.getMessage());
        }
    }

    private ResponseEntity<Resource> serveFile(Path filePath, String contentType, String fileName)
            throws MalformedURLException, IOException {
        if (!Files.exists(filePath)) {
            throw new ResourceNotFoundException("File not found on server");
        }

        Resource resource = new UrlResource(filePath.toUri());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + fileName.replace("\"", "") + "\"")
                .body(resource);
    }

    // UPDATE - Rename attachment (page editors only)
    @PutMapping("/{id}")
    public ResponseEntity<AttachmentDTO> updateAttachment(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String id, @RequestParam String newFileName) {
        Attachment existing = attachmentService.getAttachmentById(id);
        permissionService.validatePageAccess(existing.getPage().getId(), principal.getId(), "EDIT");
        return ResponseEntity.ok(AttachmentDTO.from(attachmentService.updateAttachmentName(id, newFileName)));
    }

    // DELETE - Delete attachment (page editors only)
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteAttachment(
            @AuthenticationPrincipal UserPrincipal principal, @PathVariable String id) {
        Attachment existing = attachmentService.getAttachmentById(id);
        permissionService.validatePageAccess(existing.getPage().getId(), principal.getId(), "EDIT");
        attachmentService.deleteAttachment(id);
        return ResponseEntity.ok(Map.of("message", "Attachment deleted successfully"));
    }

    private Attachment readable(UserPrincipal principal, String id) {
        Attachment attachment = attachmentService.getAttachmentById(id);
        permissionService.validatePageAccess(attachment.getPage().getId(), principal.getId(), "READ");
        return attachment;
    }
}
