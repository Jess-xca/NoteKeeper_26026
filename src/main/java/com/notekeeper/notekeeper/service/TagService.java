package com.notekeeper.notekeeper.service;

import com.notekeeper.notekeeper.model.Tag;
import com.notekeeper.notekeeper.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import com.notekeeper.notekeeper.exception.ResourceNotFoundException;
import com.notekeeper.notekeeper.exception.BadRequestException;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TagService {

    @Autowired
    private TagRepository tagRepository;

    // CREATE
    @Transactional
    public String createTag(Tag tag) {
        if (tagRepository.existsByName(tag.getName())) {
            throw new BadRequestException("Tag name already exists");
        }
        Tag saved = tagRepository.save(tag);
        return saved.getId();
    }

    // READ
    public Tag getTagById(String id) {
        return tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found"));
    }

    public Tag getTagByName(String name) {
        return tagRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found"));
    }

    public List<Tag> getAllTags() {
        return tagRepository.findAll();
    }

    public List<Tag> getTagsByUserId(String userId) {
        return tagRepository.findTagsByUserId(userId);
    }

    public List<Tag> searchTags(String keyword) {
        return tagRepository.findByNameContainingIgnoreCase(keyword);
    }

    public List<Tag> getUnusedTags() {
        return tagRepository.findUnusedTags();
    }

    // UPDATE
    @Transactional
    public void updateTag(String id, Tag tagDetails) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found"));

        tag.setName(tagDetails.getName());
        tag.setColor(tagDetails.getColor());
        tagRepository.save(tag);
    }

    // DELETE
    @Transactional
    public void deleteTag(String id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found"));

        tagRepository.delete(tag);
    }

    // PAGINATION
    public Page<Tag> getTagsPaginated(int page, int size) {
        return tagRepository.findAll(PageRequest.of(page, size));
    }

    // STATISTICS
    public long countTags() {
        return tagRepository.count();
    }

    public long countTagUsage(String tagId) {
        return tagRepository.countPagesByTagId(tagId);
    }

    public boolean isTagNameAvailable(String name) {
        return !tagRepository.existsByName(name);
    }
}
