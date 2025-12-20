package com.notekeeper.notekeeper.service;

import com.notekeeper.notekeeper.model.Location;
import com.notekeeper.notekeeper.model.LocationType;
import com.notekeeper.notekeeper.repository.LocationRepository;
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
public class LocationService {

    @Autowired
    private LocationRepository locationRepository;

    // CREATE
    @Transactional
    public String createLocation(Location location) {
        if (locationRepository.existsByCode(location.getCode())) {
            throw new BadRequestException("Location code already exists");
        }
        Location saved = locationRepository.save(location);
        return saved.getId();
    }

    @Transactional
    public String createLocationWithParent(String parentCode, Location childLocation) {
        if (parentCode != null) {
            Location parent = locationRepository.findByCode(parentCode)
                    .orElseThrow(() -> new ResourceNotFoundException("Parent location not found"));
            childLocation.setParent(parent);
        }

        if (locationRepository.existsByCode(childLocation.getCode())) {
            throw new BadRequestException("Location code already exists");
        }

        Location saved = locationRepository.save(childLocation);
        return saved.getId();
    }

    // READ
    public Location getLocationById(String id) {
        return locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found"));
    }

    public Location getLocationByCode(String code) {
        return locationRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found"));
    }

    public List<Location> getAllLocations() {
        return locationRepository.findAll();
    }

    public List<Location> getAllProvinces() {
        return locationRepository.findByParentIsNull();
    }

    public List<Location> getLocationsByType(LocationType type) {
        return locationRepository.findByType(type);
    }

    public List<Location> getChildrenByParentId(String parentId) {
        return locationRepository.findByParentId(parentId);
    }

    public List<Location> getDistrictsByProvinceCode(String provinceCode) {
        return locationRepository.findDistrictsByProvinceCode(provinceCode);
    }

    public List<Location> searchLocations(String keyword) {
        return locationRepository.findByNameContainingIgnoreCase(keyword);
    }

    // UPDATE
    @Transactional
    public void updateLocation(String id, Location locationDetails) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found"));

        location.setName(locationDetails.getName());
        location.setCode(locationDetails.getCode());
        location.setType(locationDetails.getType());
        locationRepository.save(location);
    }

    // DELETE
    @Transactional
    public void deleteLocation(String id) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found"));

        if (!location.getChildren().isEmpty()) {
            throw new BadRequestException("Cannot delete location with children");
        }
        if (!location.getUsers().isEmpty()) {
            throw new BadRequestException("Cannot delete location with users");
        }

        locationRepository.delete(location);
    }

    // PAGINATION
    public Page<Location> getLocationsPaginated(int page, int size) {
        return locationRepository.findAll(PageRequest.of(page, size));
    }

    public Page<Location> getLocationsByTypePaginated(LocationType type, int page, int size) {
        return locationRepository.findByType(type, PageRequest.of(page, size));
    }

    // STATISTICS
    public long countChildren(String parentId) {
        return locationRepository.countChildrenByParentId(parentId);
    }

    public long countUsersByLocation(String locationId) {
        return locationRepository.countUsersByLocationId(locationId);
    }
}
