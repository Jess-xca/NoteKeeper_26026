package com.notekeeper.notekeeper.controller;

import com.notekeeper.notekeeper.model.Tag;
import com.notekeeper.notekeeper.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
@CrossOrigin(origins = "*")
public class TagController {

    @Autowired
    private TagService tagService;

    // CREATE
    @PostMapping
    public ResponseEntity<Tag> createTag(@RequestBody Tag tag) {
        try {
            Tag createdTag = tagService.createTag(tag);
            return new ResponseEntity<>(createdTag, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.CONFLICT);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // READ
    @GetMapping("/{id}")
    public ResponseEntity<Tag> getTagById(@PathVariable String id) {
        try {
            Tag tag = tagService.getTagById(id);
            return new ResponseEntity<>(tag, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<Tag> getTagByName(@PathVariable String name) {
        try {
            Tag tag = tagService.getTagByName(name);
            return new ResponseEntity<>(tag, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping
    public ResponseEntity<List<Tag>> getAllTags() {
        try {
            List<Tag> tags = tagService.getAllTags();
            return new ResponseEntity<>(tags, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Tag>> getTagsByUserId(@PathVariable String userId) {
        try {
            List<Tag> tags = tagService.getTagsByUserId(userId);
            return new ResponseEntity<>(tags, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/unused")
    public ResponseEntity<List<Tag>> getUnusedTags() {
        try {
            List<Tag> tags = tagService.getUnusedTags();
            return new ResponseEntity<>(tags, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<Tag>> searchTags(@RequestParam String keyword) {
        try {
            List<Tag> tags = tagService.searchTags(keyword);
            return new ResponseEntity<>(tags, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<Tag> updateTag(
            @PathVariable String id,
            @RequestBody Tag tagDetails) {
        try {
            Tag updatedTag = tagService.updateTag(id, tagDetails);
            return new ResponseEntity<>(updatedTag, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteTag(@PathVariable String id) {
        try {
            tagService.deleteTag(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // PAGINATION
    @GetMapping("/paginated")
    public ResponseEntity<Page<Tag>> getTagsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<Tag> tags = tagService.getTagsPaginated(page, size);
            return new ResponseEntity<>(tags, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // STATISTICS & UTILITIES
    @GetMapping("/count")
    public ResponseEntity<Long> countTags() {
        try {
            long count = tagService.countTags();
            return new ResponseEntity<>(count, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{tagId}/usage-count")
    public ResponseEntity<Long> countTagUsage(@PathVariable String tagId) {
        try {
            long count = tagService.countTagUsage(tagId);
            return new ResponseEntity<>(count, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/check-name")
    public ResponseEntity<Boolean> checkTagNameAvailability(@RequestParam String name) {
        try {
            boolean available = tagService.isTagNameAvailable(name);
            return new ResponseEntity<>(available, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}