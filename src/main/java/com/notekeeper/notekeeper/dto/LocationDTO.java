package com.notekeeper.notekeeper.dto;

import com.notekeeper.notekeeper.model.LocationType;
import java.time.LocalDateTime;

public class LocationDTO {
    private String id;
    private String name;
    private String code;
    private LocationType type;
    private LocationDTO parent;
    private LocalDateTime createdAt;

    public LocationDTO() {
    }

    public LocationDTO(String id, String name, String code, LocationType type,
            LocationDTO parent, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.type = type;
        this.parent = parent;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public LocationType getType() {
        return type;
    }

    public void setType(LocationType type) {
        this.type = type;
    }

    public LocationDTO getParent() {
        return parent;
    }

    public void setParent(LocationDTO parent) {
        this.parent = parent;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}