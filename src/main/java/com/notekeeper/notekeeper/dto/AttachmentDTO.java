package com.notekeeper.notekeeper.dto;

import com.notekeeper.notekeeper.model.Attachment;

import java.time.LocalDateTime;

/**
 * What the website needs to know about an uploaded file. The entity itself points at lazy
 * pages and users, which cannot be turned into JSON safely.
 */
public record AttachmentDTO(
        String id,
        String fileName,
        String fileType,
        Long fileSize,
        String filePath,
        LocalDateTime uploadedAt,
        String pageId,
        String uploadedById) {

    public static AttachmentDTO from(Attachment a) {
        return new AttachmentDTO(
                a.getId(),
                a.getFileName(),
                a.getFileType(),
                a.getFileSize(),
                a.getFilePath(),
                a.getUploadedAt(),
                a.getPage() == null ? null : a.getPage().getId(),
                a.getUploadedBy() == null ? null : a.getUploadedBy().getId());
    }
}
