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
    public ResponseEntity<?> createTag(@RequestBody Tag tag) {
        try {
            String result = tagService.createTag(tag);

            if (result.equals("name exists")) {
                return new ResponseEntity<>("Tag name already exists", HttpStatus.CONFLICT);
            } else {
                Tag createdTag = tagService.getTagById(result);
                return ResponseEntity.status(HttpStatus.CREATED).body(dtoMapper.toTagDTO(createdTag));
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to create tag: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTagById(@PathVariable String id) {
        try {
            Tag tag = tagService.getTagById(id);
            if (tag == null) {
                return new ResponseEntity<>("Tag not found", HttpStatus.NOT_FOUND);
            }
            return ResponseEntity.ok(dtoMapper.toTagDTO(tag));
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to fetch tag: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<?> getTagByName(@PathVariable String name) {
        try {
            Tag tag = tagService.getTagByName(name);
            if (tag == null) {
                return new ResponseEntity<>("Tag not found", HttpStatus.NOT_FOUND);
            }
            return ResponseEntity.ok(dtoMapper.toTagDTO(tag));
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to fetch tag: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllTags() {
        try {
            List<Tag> tags = tagService.getAllTags();
            List<TagDTO> tagDTOs = tags.stream()
                    .map(dtoMapper::toTagDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(tagDTOs);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to fetch tags: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getTagsByUserId(@PathVariable String userId) {
        try {
            List<Tag> tags = tagService.getTagsByUserId(userId);
            List<TagDTO> tagDTOs = tags.stream()
                    .map(dtoMapper::toTagDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(tagDTOs);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to fetch user tags: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/unused")
    public ResponseEntity<?> getUnusedTags() {
        try {
            List<Tag> tags = tagService.getUnusedTags();
            List<TagDTO> tagDTOs = tags.stream()
                    .map(dtoMapper::toTagDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(tagDTOs);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to fetch unused tags: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchTags(@RequestParam String keyword) {
        try {
            List<Tag> tags = tagService.searchTags(keyword);
            List<TagDTO> tagDTOs = tags.stream()
                    .map(dtoMapper::toTagDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(tagDTOs);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to search tags: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTag(
            @PathVariable String id,
            @RequestBody Tag tagDetails) {
        try {
            String result = tagService.updateTag(id, tagDetails);

            if (result.equals("not found")) {
                return new ResponseEntity<>("Tag not found", HttpStatus.NOT_FOUND);
            } else {
                Tag updatedTag = tagService.getTagById(id);
                return ResponseEntity.ok(dtoMapper.toTagDTO(updatedTag));
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to update tag: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTag(@PathVariable String id) {
        try {
            String result = tagService.deleteTag(id);

            if (result.equals("not found")) {
                return new ResponseEntity<>("Tag not found", HttpStatus.NOT_FOUND);
            } else {
                return new ResponseEntity<>("Tag deleted successfully", HttpStatus.OK);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to delete tag: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/paginated")
    public ResponseEntity<?> getTagsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<Tag> tags = tagService.getTagsPaginated(page, size);
            Page<TagDTO> tagDTOs = tags.map(dtoMapper::toTagDTO);
            return ResponseEntity.ok(tagDTOs);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to fetch paginated tags: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/count")
    public ResponseEntity<?> countTags() {
        try {
            long count = tagService.countTags();
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to count tags: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{tagId}/usage-count")
    public ResponseEntity<?> countTagUsage(@PathVariable String tagId) {
        try {
            long count = tagService.countTagUsage(tagId);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to count tag usage: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/check-name")
    public ResponseEntity<?> checkTagNameAvailability(@RequestParam String name) {
        try {
            boolean available = tagService.isTagNameAvailable(name);
            return ResponseEntity.ok(available);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to check tag name: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
