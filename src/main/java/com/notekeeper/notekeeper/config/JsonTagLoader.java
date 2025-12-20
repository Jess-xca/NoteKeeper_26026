package com.notekeeper.notekeeper.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.notekeeper.notekeeper.model.Tag;
import com.notekeeper.notekeeper.repository.TagRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.InputStream;
import java.util.List;

@Configuration
public class JsonTagLoader {

    private static final Logger logger = LoggerFactory.getLogger(JsonTagLoader.class);

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private ResourceLoader resourceLoader;

    @PostConstruct
    public void init() {
        try {
            logger.info("Checking for default tags from Tags.json...");
            Resource resource = resourceLoader.getResource("classpath:Tags.json");
            InputStream inputStream = resource.getInputStream();
            ObjectMapper mapper = new ObjectMapper();
            List<Tag> defaultTags = mapper.readValue(inputStream, new TypeReference<List<Tag>>() {});

            int addedCount = 0;
            for (Tag defaultTag : defaultTags) {
                if (!tagRepository.existsByName(defaultTag.getName())) {
                    tagRepository.save(defaultTag);
                    addedCount++;
                }
            }

            if (addedCount > 0) {
                logger.info("Successfully added {} new default tags.", addedCount);
            } else {
                logger.info("All default tags already exist.");
            }
        } catch (Exception e) {
            logger.error("Failed to load default tags: {}", e.getMessage());
        }
    }
}
