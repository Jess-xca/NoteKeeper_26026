package com.notekeeper.notekeeper.controller;

import com.notekeeper.notekeeper.dto.TagDTO;
import com.notekeeper.notekeeper.mapper.DTOMapper;
import com.notekeeper.notekeeper.model.Tag;
import com.notekeeper.notekeeper.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tags")
@CrossOrigin(origins = "*")
public class TagController {

    @Autowired
    private TagService tagService;

    @Autowired
    private DTOMapper dtoMapper;

    @PostMapping
    public ResponseEntity<TagDTO> createTag(@RequestBody Tag tag) {
        String resultId = tagService.createTag(tag);
        Tag createdTag = tagService.getTagById(resultId);
        return ResponseEntity.status(HttpStatus.CREATED).body(dtoMapper.toTagDTO(createdTag));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TagDTO> getTagById(@PathVariable String id) {
        Tag tag = tagService.getTagById(id);
        return ResponseEntity.ok(dtoMapper.toTagDTO(tag));
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<TagDTO> getTagByName(@PathVariable String name) {
        Tag tag = tagService.getTagByName(name);
        return ResponseEntity.ok(dtoMapper.toTagDTO(tag));
    }

    @GetMapping
    public ResponseEntity<List<TagDTO>> getAllTags() {
        List<Tag> tags = tagService.getAllTags();
        List<TagDTO> tagDTOs = tags.stream()
                .map(dtoMapper::toTagDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(tagDTOs);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TagDTO>> getTagsByUserId(@PathVariable String userId) {
        List<Tag> tags = tagService.getTagsByUserId(userId);
        List<TagDTO> tagDTOs = tags.stream()
                .map(dtoMapper::toTagDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(tagDTOs);
    }

    @GetMapping("/unused")
    public ResponseEntity<List<TagDTO>> getUnusedTags() {
        List<Tag> tags = tagService.getUnusedTags();
        List<TagDTO> tagDTOs = tags.stream()
                .map(dtoMapper::toTagDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(tagDTOs);
    }

    @GetMapping("/search")
    public ResponseEntity<List<TagDTO>> searchTags(@RequestParam String keyword) {
        List<Tag> tags = tagService.searchTags(keyword);
        List<TagDTO> tagDTOs = tags.stream()
                .map(dtoMapper::toTagDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(tagDTOs);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TagDTO> updateTag(
            @PathVariable String id,
            @RequestBody Tag tagDetails) {
        tagService.updateTag(id, tagDetails);
        Tag updatedTag = tagService.getTagById(id);
        return ResponseEntity.ok(dtoMapper.toTagDTO(updatedTag));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteTag(@PathVariable String id) {
        tagService.deleteTag(id);
        return ResponseEntity.ok(Map.of("message", "Tag deleted successfully"));
    }

    @GetMapping("/paginated")
    public ResponseEntity<Page<TagDTO>> getTagsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Tag> tags = tagService.getTagsPaginated(page, size);
        Page<TagDTO> tagDTOs = tags.map(dtoMapper::toTagDTO);
        return ResponseEntity.ok(tagDTOs);
    }

    @GetMapping("/count")
    public ResponseEntity<Long> countTags() {
        return ResponseEntity.ok(tagService.countTags());
    }

    @GetMapping("/{tagId}/usage-count")
    public ResponseEntity<Long> countTagUsage(@PathVariable String tagId) {
        return ResponseEntity.ok(tagService.countTagUsage(tagId));
    }

    @GetMapping("/check-name")
    public ResponseEntity<Boolean> checkTagNameAvailability(@RequestParam String name) {
        return ResponseEntity.ok(tagService.isTagNameAvailable(name));
    }
}
