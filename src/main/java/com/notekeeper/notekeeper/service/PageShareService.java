package com.notekeeper.notekeeper.service;

import com.notekeeper.notekeeper.exception.BadRequestException;
import com.notekeeper.notekeeper.exception.ResourceNotFoundException;
import com.notekeeper.notekeeper.model.Page;
import com.notekeeper.notekeeper.model.PageShare;
import com.notekeeper.notekeeper.model.User;
import com.notekeeper.notekeeper.repository.PageRepository;
import com.notekeeper.notekeeper.repository.PageShareRepository;
import com.notekeeper.notekeeper.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PageShareService {

    @Autowired
    private PageShareRepository pageShareRepository;

    @Autowired
    private PageRepository pageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private NotificationService notificationService;

    @Transactional
    public PageShare sharePage(String pageId, String sharedById, String email, String permission) {
        if (pageId == null || sharedById == null || email == null) {
            throw new BadRequestException("pageId, sharedById, and email are required");
        }

        Page page = pageRepository.findById(pageId)
                .orElseThrow(() -> new ResourceNotFoundException("Page not found"));

        User sharedBy = userRepository.findById(sharedById)
                .orElseThrow(() -> new ResourceNotFoundException("Sharing user not found"));

        User sharedWith = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User with email '" + email + "' not found"));

        if (sharedWith.getId().equals(sharedById)) {
            throw new BadRequestException("Cannot share with yourself");
        }

        pageShareRepository.findByPageIdAndSharedWithId(pageId, sharedWith.getId())
                .ifPresent(s -> {
                    throw new BadRequestException("Page already shared with this user");
                });

        PageShare share = new PageShare(page, sharedBy, sharedWith, permission);
        PageShare savedShare = pageShareRepository.save(share);

        // CREATE NOTIFICATION
        notificationService.createNotification(
                sharedWith.getId(),
                "New Page Shared",
                sharedBy.getFullName() + " shared a page with you: " + page.getTitle(),
                com.notekeeper.notekeeper.model.NotificationType.SHARE
        );

        // SEND EMAIL
        emailService.sendShareNotification(
                sharedWith.getEmail(),
                sharedBy.getFullName(),
                page.getTitle(),
                "Page"
        );

        return savedShare;
    }

    public List<PageShare> getPageShares(String pageId) {
        return pageShareRepository.findByPageId(pageId);
    }

    public List<PageShare> getSharedWithUser(String userId) {
        return pageShareRepository.findBySharedWithId(userId);
    }

    public List<PageShare> getSharedByUser(String userId) {
        return pageShareRepository.findBySharedById(userId);
    }

    @Transactional
    public PageShare updateSharePermission(String shareId, String permission) {
        if (permission == null || (!permission.equals("VIEW") && !permission.equals("EDIT"))) {
            throw new BadRequestException("Permission must be VIEW or EDIT");
        }

        PageShare share = pageShareRepository.findById(shareId)
                .orElseThrow(() -> new ResourceNotFoundException("Share not found"));

        share.setPermission(permission);
        return pageShareRepository.save(share);
    }

    @Transactional
    public void removeShare(String shareId) {
        PageShare share = pageShareRepository.findById(shareId)
                .orElseThrow(() -> new ResourceNotFoundException("Share not found"));
        pageShareRepository.delete(share);
    }
}
