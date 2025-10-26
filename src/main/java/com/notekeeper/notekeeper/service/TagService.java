package com.notekeeper.notekeeper.service;

import com.notekeeper.notekeeper.model.Tag;
import com.notekeeper.notekeeper.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TagService {

    @Autowired
    private TagRepository tagRepository;

    // CREATE
    public Tag createTag(Tag tag) {
        if (tagRepository.existsByName(tag.getName())) {
            throw new RuntimeException("Tag name already exists");
        }
        return tagRepository.save(tag);
    }

    // READ
    public Tag getTagById(String id) {
        return tagRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tag not found with id: " + id));
    }

    public Tag getTagByName(String name) {
        return tagRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Tag not found with name: " + name));
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
    public Tag updateTag(String id, Tag tagDetails) {
        Tag tag = getTagById(id);
        tag.setName(tagDetails.getName());
        tag.setColor(tagDetails.getColor());
        return tagRepository.save(tag);
    }

    // DELETE
    public void deleteTag(String id) {
        Tag tag = getTagById(id);
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