package com.notekeeper.notekeeper.service;

import com.notekeeper.notekeeper.model.Tag;
import com.notekeeper.notekeeper.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TagService {

    @Autowired
    private TagRepository tagRepository;

    // CREATE
    public String createTag(Tag tag) {
        if (tagRepository.existsByName(tag.getName())) {
            return "name exists";
        }
        Tag saved = tagRepository.save(tag);
        return saved.getId();
    }

    // READ
    public Tag getTagById(String id) {
        Optional<Tag> tag = tagRepository.findById(id);
        return tag.orElse(null);
    }

    public Tag getTagByName(String name) {
        Optional<Tag> tag = tagRepository.findByName(name);
        return tag.orElse(null);
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
    public String updateTag(String id, Tag tagDetails) {
        Optional<Tag> tagOpt = tagRepository.findById(id);

        if (!tagOpt.isPresent()) {
            return "not found";
        }

        Tag tag = tagOpt.get();
        tag.setName(tagDetails.getName());
        tag.setColor(tagDetails.getColor());
        tagRepository.save(tag);
        return "success";
    }

    // DELETE
    public String deleteTag(String id) {
        Optional<Tag> tagOpt = tagRepository.findById(id);

        if (!tagOpt.isPresent()) {
            return "not found";
        }

        tagRepository.delete(tagOpt.get());
        return "success";
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